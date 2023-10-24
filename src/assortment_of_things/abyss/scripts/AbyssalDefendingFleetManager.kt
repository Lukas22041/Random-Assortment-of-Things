package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssDifficulty
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.intel.event.AbyssalDepthsEventIntel
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.abyss.procgen.AbyssalSeraphSpawner
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.ArrayList

//Spawns a single defensive fleet that attempts to orbit around a single location.

//If the player isnt around the fleet will despawn, and if the fleet is ever destroyed, the script is removed to prevent respawns.
//Will despawn if the player isnt in the abyss, or if the player is in the abyss but 2 connections away.
//Will always use the same seed for respawn.

//Its set to not to long pursuits, so once it looses the player it should go back to its spawn relatively soon.

class AbyssalDefendingFleetManager(source: SectorEntityToken, var depth: AbyssDepth) : SourceBasedFleetManager(source, 1f, 0, 1, 999f) {

    var starsystem = source.starSystem

    //Generates a single seed so that when it reconstructs the fleet, its basicly the same one.
    var randomSeed = Random().nextLong()


    override fun advance(amount: Float) {
        super.advance(amount)




        if (!AbyssUtils.playerInNeighbourOrSystem(starsystem))
        {
            for (fleet in ArrayList(fleets))
            {
                fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null)
            }
        }
    }


    /*Remnant Fleet Points
    *
    * Remnant Medium Seeded Points (Randomly spawn in to the system)
    * Min: 32
    * Max: 96
    *
    * Remnant Medium Station Points (Spawned From Station)
    * Min: 48
    * Max: 96
    *
    * Remnant High Station Points (Spawned From Station)
    * Min: 64
    * Max: 192
    *
    * */

    override fun spawnFleet(): CampaignFleetAPI? {

        if (source.isExpired) return null

        //Prevents the game spawning fleets when the player isnt in the system or a neighbour
        if (!AbyssUtils.playerInNeighbourOrSystem(starsystem)) return null

        var difficulty = AbyssUtils.getDifficulty()
        val random = Random(randomSeed)

        var minPoints = 0f
        var maxPoints = 0f

        when(depth) {

            AbyssDepth.Shallow -> {
                minPoints += 84f
                maxPoints += 128f
            }
            AbyssDepth.Deep -> {
                minPoints += 118f
                maxPoints += 182f
            }
        }

        if (difficulty == AbyssDifficulty.Hard) {
            minPoints += 50
            maxPoints += 70
        }

        var points = MathUtils.getRandomNumberInRange(minPoints, maxPoints)

        //Mild Fleet scaling
        if (difficulty == AbyssDifficulty.Hard)
        {
            var playerFleet = Global.getSector().playerFleet
            var playerFP = playerFleet.fleetPoints.toFloat()
            var scalingPointsCap = 250f

            if (points < playerFP) {

                var difference = playerFP - points

                var scalingMult = when(depth) {
                    AbyssDepth.Shallow -> 0.45f
                    AbyssDepth.Deep -> 0.5f
                }

                var pointsForScaling = difference * scalingMult
                pointsForScaling = MathUtils.clamp(pointsForScaling, 0f, playerFP)
                pointsForScaling = MathUtils.clamp(pointsForScaling, 0f, scalingPointsCap)

                points += pointsForScaling
            }
        }

        var qualityOverride = when(depth) {
            AbyssDepth.Shallow -> 4f
            AbyssDepth.Deep -> 5f
        }

        if (difficulty == AbyssDifficulty.Hard) {
             qualityOverride = 5f
        }

        var factionID = "rat_abyssals"
        if (depth == AbyssDepth.Deep) factionID = "rat_abyssals_deep"

        val params = FleetParamsV3(null,
            source.locationInHyperspace,
            factionID,
            qualityOverride,
            FleetTypes.PATROL_MEDIUM,
            points,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )
        params.random = random
        params.withOfficers = false

        val fleet = FleetFactoryV3.createFleet(params)

        var minSeraphs = 0
        var maxSeraphs = 0
        if (depth == AbyssDepth.Deep) minSeraphs += 2
        if (depth == AbyssDepth.Deep) maxSeraphs += 3
        if (difficulty == AbyssDifficulty.Hard) minSeraphs += 1
        if (difficulty == AbyssDifficulty.Hard) maxSeraphs += 2

        AbyssalSeraphSpawner.addSeraphsToFleet(fleet, random, minSeraphs, maxSeraphs)
        AbyssalSeraphSpawner.sortWithSeraphs(fleet)

        for (member in fleet.fleetData.membersListCopy) {
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }


       // addAICores(fleet, type, difficulty, random)

        val location = starsystem
        location.addEntity(fleet)

       // RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, false)
        initAbyssalBehaviour(fleet, random)


        //fleet.addScript(RemnantAssignmentAI(fleet, source.containingLocation as StarSystemAPI, source))
        fleet.memoryWithoutUpdate["\$sourceId"] = source.id

        fleet.clearAssignments()
        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
        fleet.setLocation(source.location.x, source.location.y)
        fleet.facing = random.nextFloat() * 360f

        addFollowThroughFractureScript(fleet)
        addMemKeyApplierScript(fleet)

        var alterationChancePerShip = 0.0f
        if (difficulty == AbyssDifficulty.Hard) alterationChancePerShip += 0.4f
        if (depth == AbyssDepth.Shallow) alterationChancePerShip += 0.4f
        if (depth == AbyssDepth.Deep) alterationChancePerShip += 0.5f

        AbyssUtils.addAlterationsToFleet(fleet, alterationChancePerShip, random)

        return fleet
    }



    fun initAbyssalBehaviour(fleet: CampaignFleetAPI, random: Random)
    {
        fleet.removeAbility(Abilities.EMERGENCY_BURN)
        fleet.removeAbility(Abilities.SENSOR_BURST)
        fleet.removeAbility(Abilities.GO_DARK)

        // to make sure they attack the player on sight when player's transponder is off

        // to make sure they attack the player on sight when player's transponder is off
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
        //fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_PATROL_FLEET] = true
       // fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true

        RemnantSeededFleetManager.addRemnantInteractionConfig(fleet)

        val salvageSeed: Long = random.nextLong()
        fleet.memoryWithoutUpdate[MemFlags.SALVAGE_SEED] = salvageSeed
    }

    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?,  param: Any?) {
        super.reportFleetDespawnedToListener(fleet, reason, param)
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            val sid = fleet!!.memoryWithoutUpdate.getString("\$sourceId")

            if (sid != null)
            {
                if (source != null)
                {
                    if (sid == source.id)
                    {
                        if (param is BattleAPI)
                        {
                            if (param.isPlayerInvolved)
                            {
                                AbyssalDepthsEventIntel.addFactorCreateIfNecessary(object: BaseOneTimeFactor(5) {
                                    override fun getDesc(intel: BaseEventIntel?): String {
                                        return "Defeated a defending fleet"
                                    }

                                    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
                                        return object : BaseFactorTooltip() {
                                            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                                                tooltip.addPara("The fleet defeated a fleet that was defending a location within the abyss.",
                                                    0f,
                                                    Misc.getHighlightColor(),
                                                    "")
                                            }
                                        }
                                    }
                                }, null)
                            }
                        }
                        starsystem.removeScript(this)
                    }
                }
            }
        }
    }


    //Allows the fleet to go through fractures
    //Only works one step away from its source
    fun addFollowThroughFractureScript(fleet: CampaignFleetAPI)
    {
        var script = object : EveryFrameScript {
            override fun isDone(): Boolean {
                return fleet.isDespawning
            }

            override fun runWhilePaused(): Boolean {
                return false
            }

            override fun advance(amount: Float) {
                if (source == null) return

                var player = Global.getSector().playerFleet

                var tactical = (fleet.ai as ModularFleetAIAPI).tacticalModule
                var target = tactical.target

                //If the fleet was targeting the player and they dissapeared in to another system, try finding a connecting fracture and move towards it, then jump
                if (target != null && fleet.containingLocation != target.containingLocation)
                {
                    var fractures = fleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalFracture }
                    var fracture: CustomCampaignEntityAPI? = null
                    for (frac in fractures)
                    {
                        var plug = frac.customPlugin as AbyssalFracture
                        if (plug.connectedEntity!!.containingLocation == target.containingLocation)
                        {
                            fracture = frac
                        }
                    }

                    if (fracture != null)
                    {
                        tactical.forceTargetReEval()
                        fleet.clearAssignments()
                        tactical.setPriorityTarget(fracture, 1f, true)
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, fracture, 2f) {
                            Global.getSector().doHyperspaceTransition(fleet, fracture,
                                JumpPointAPI.JumpDestination(((fracture!!.customPlugin as AbyssalFracture).connectedEntity), ""))
                        }
                        fleet.addAssignment(FleetAssignment.INTERCEPT, target, 2f)
                    }

                }
                //If the fleet is in another system than its source, isnt targeting anything and has no assignments, move back towards its source location.
                else if (target == null && fleet.containingLocation != starsystem && !fleet.isCurrentAssignment(FleetAssignment.GO_TO_LOCATION) && !fleet.isCurrentAssignment(FleetAssignment.INTERCEPT))
                {
                    var fractures = fleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalFracture }
                    var fracture: CustomCampaignEntityAPI? = null
                    for (frac in fractures)
                    {
                        var plug = frac.customPlugin as AbyssalFracture
                        if (plug.connectedEntity!!.containingLocation == starsystem)
                        {
                            fracture = frac
                        }
                    }

                    if (fracture != null)
                    {
                        tactical.forceTargetReEval()
                        fleet.clearAssignments()
                        tactical.setPriorityTarget(fracture, 0.25f, true)
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, fracture, 0.25f) {
                            Global.getSector().doHyperspaceTransition(fleet, fracture,
                                JumpPointAPI.JumpDestination(((fracture.customPlugin as AbyssalFracture).connectedEntity), ""))
                        }
                        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
                    }
                }
                else if (source.isExpired && fleet.starSystem == starsystem) {
                    fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, source, 9999999f)
                }
            }

        }
        fleet.addScript(script)
    }

    fun addMemKeyApplierScript(fleet: CampaignFleetAPI) {
        var script = object : EveryFrameScript {


            override fun isDone(): Boolean {
                return fleet.isDespawning
            }

            override fun runWhilePaused(): Boolean {
                return false
            }

            override fun advance(amount: Float) {
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true
                fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
            }
        }
        fleet.addScript(script)
    }
}