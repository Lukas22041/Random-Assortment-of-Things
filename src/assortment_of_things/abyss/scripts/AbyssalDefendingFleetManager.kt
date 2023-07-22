package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssDifficulty
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.intel.event.AbyssalDepthsEventIntel
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater
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
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.ArrayList

//Spawns a single defensive fleet that attempts to orbit around a single location.

//If the player isnt around the fleet will despawn, and if the fleet is ever destroyed, the script is removed to prevent respawns.
//Will despawn if the player isnt in the abyss, or if the player is in the abyss but 2 connections away.
//Will always use the same seed for respawn.

//Its set to not to long pursuits, so once it looses the player it should go back to its spawn relatively soon.

class AbyssalDefendingFleetManager(source: SectorEntityToken, var tier: AbyssProcgen.Tier, var type: String) : SourceBasedFleetManager(source, 1f, 0, 1, 999f) {



    //Generates a single seed so that when it reconstructs the fleet, its basicly the same one.
    var randomSeed = Random().nextLong()


    override fun advance(amount: Float) {
        super.advance(amount)

        if (!AbyssUtils.playerInNeighbourOrSystem(source.starSystem))
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

        //Prevents the game spawning fleets when the player isnt in the system or a neighbour
        if (!AbyssUtils.playerInNeighbourOrSystem(source.starSystem)) return null

        var difficulty = AbyssUtils.getDifficulty()
        val random = Random(randomSeed)

        var minPoints = 10f
        var maxPoints = 10f

        when(tier) {
            AbyssProcgen.Tier.Low -> {
                when(type) {
                    FleetTypes.PATROL_SMALL -> {
                        minPoints = 32f
                        maxPoints = 48f
                    }
                    FleetTypes.PATROL_MEDIUM -> {
                        minPoints = 48f
                        maxPoints = 64f
                    }
                    FleetTypes.PATROL_LARGE -> {
                        minPoints = 64f
                        maxPoints = 96f
                    }
                }
            }

            AbyssProcgen.Tier.Mid -> {
                when(type) {
                    FleetTypes.PATROL_SMALL -> {
                        minPoints = 48f
                        maxPoints = 64f
                    }
                    FleetTypes.PATROL_MEDIUM -> {
                        minPoints = 64f
                        maxPoints = 96f
                    }
                    FleetTypes.PATROL_LARGE -> {
                        minPoints = 96f
                        maxPoints = 160f
                    }
                }
            }

            AbyssProcgen.Tier.High -> {
                when(type) {
                    FleetTypes.PATROL_SMALL -> {
                        minPoints = 64f
                        maxPoints = 96f
                    }
                    FleetTypes.PATROL_MEDIUM -> {
                        minPoints = 112f
                        maxPoints = 144f
                    }
                    FleetTypes.PATROL_LARGE -> {
                        minPoints = 144f
                        maxPoints = 228f
                    }
                }
            }
        }

        if (difficulty == AbyssDifficulty.Hard)
        {
            minPoints += 20f
            maxPoints += 30f

        }

        var totalBonus = 10
        minPoints += totalBonus
        maxPoints += totalBonus

        var points = MathUtils.getRandomNumberInRange(minPoints, maxPoints)

        /*var type = FleetTypes.PATROL_SMALL
        if (points > 64) type = FleetTypes.PATROL_MEDIUM
        if (points > 128) type = FleetTypes.PATROL_LARGE*/


        var qualityOverride = when(tier) {
            AbyssProcgen.Tier.Low -> 3f
            AbyssProcgen.Tier.Mid -> 4f
            AbyssProcgen.Tier.High -> 5f
        }

        if (difficulty == AbyssDifficulty.Hard) {
             qualityOverride = 5f
        }

        val params = FleetParamsV3(null,
            source.locationInHyperspace,
            AbyssUtils.FACTION_ID,
            qualityOverride,
            type,
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

        for (member in fleet.fleetData.membersListCopy) {
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)
        }


        addAICores(fleet, type, difficulty, random)

        val location = source.containingLocation
        location.addEntity(fleet)

       // RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, false)
        initAbyssalBehaviour(fleet, random)


        fleet.addScript(RemnantAssignmentAI(fleet, source.containingLocation as StarSystemAPI, source))
        fleet.memoryWithoutUpdate["\$sourceId"] = source.id

        fleet.clearAssignments()
        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
        fleet.setLocation(source.location.x, source.location.y)
        fleet.facing = random.nextFloat() * 360f

        addFollowThroughFractureScript(fleet)

        var alterationChancePerShip = 0.0f
        if (difficulty == AbyssDifficulty.Hard) alterationChancePerShip += 0.4f
        if (tier == AbyssProcgen.Tier.Low) alterationChancePerShip += 0.2f
        if (tier == AbyssProcgen.Tier.Mid) alterationChancePerShip += 0.4f
        if (tier == AbyssProcgen.Tier.High) alterationChancePerShip += 0.5f

        AbyssUtils.addAlterationsToFleet(fleet, alterationChancePerShip, random)

        return fleet
    }

    fun addAICores(fleet: CampaignFleetAPI, type: String, difficulty: AbyssDifficulty, random: Random)
    {

        var corePercentage = 0f
        var bonus = 0f

        if (type == FleetTypes.PATROL_SMALL) bonus = 0.1f
        if (type == FleetTypes.PATROL_MEDIUM) bonus = 0.2f
        if (type == FleetTypes.PATROL_LARGE) bonus = 0.3f

        if (tier == AbyssProcgen.Tier.Mid) corePercentage = 0.2f
        if (tier == AbyssProcgen.Tier.High) corePercentage = 0.4f

        corePercentage += bonus
        if (difficulty == AbyssDifficulty.Hard) corePercentage += 0.3f

        var members = fleet.fleetData.membersListCopy

        corePercentage = MathUtils.clamp(corePercentage, 0f, 1f)

        var picker = WeightedRandomPicker<FleetMemberAPI>()
        picker.random = random
        for (member in members)
        {
            var weight = when(member.hullSpec.hullSize) {
                HullSize.FRIGATE -> 1f
                HullSize.DESTROYER -> 3f
                HullSize.CRUISER -> 6f
                HullSize.CAPITAL_SHIP -> 15f
                else -> 0f
            }
            picker.add(member, weight)
        }

        var count = (members.size * corePercentage).toInt()
        for (i in 0 until count)
        {
            var pick = picker.pickAndRemove() ?: continue

            var chronos = ChronosCore()
            var cosmos = CosmosCore()

            var core: PersonAPI? = null

            var variantID = pick.variant.hullVariantId.lowercase()

            if (variantID.contains("temporal"))
            {
                core = chronos.createPerson(RATItems.CHRONOS_CORE, AbyssUtils.FACTION_ID, random)
            }
            else if (variantID.contains("cosmal"))
            {
                core = cosmos.createPerson(RATItems.COSMOS_CORE, AbyssUtils.FACTION_ID, random)
            }
            else if (random.nextFloat() > 0.5f)
            {
                core = chronos.createPerson(RATItems.CHRONOS_CORE, AbyssUtils.FACTION_ID, random)
            }
            else
            {
                core = cosmos.createPerson(RATItems.COSMOS_CORE, AbyssUtils.FACTION_ID, random)
            }

            if (core != null)
            {
                pick.captain = core
            }
        }

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
                        source.starSystem.removeScript(this)
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
                else if (target == null && fleet.containingLocation != source.containingLocation && !fleet.isCurrentAssignment(FleetAssignment.GO_TO_LOCATION) && !fleet.isCurrentAssignment(FleetAssignment.INTERCEPT))
                {
                    var fractures = fleet.containingLocation.customEntities.filter { it.customPlugin is AbyssalFracture }
                    var fracture: CustomCampaignEntityAPI? = null
                    for (frac in fractures)
                    {
                        var plug = frac.customPlugin as AbyssalFracture
                        if (plug.connectedEntity!!.containingLocation == source.containingLocation)
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
            }

        }
        fleet.addScript(script)
    }
}