package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.ArrayList

//Spawns a single defensive fleet that attempts to orbit around a single location.

//If the player isnt around the fleet will despawn, and if the fleet is ever destroyed, the script is removed to prevent respawns.
//Will despawn if the player isnt in the abyss, or if the player is in the abyss but 2 connections away.
//Will always use the same seed for respawn.

//Its set to not to long pursuits, so once it looses the player it should go back to its spawn relatively soon.

class AbyssalDefendingFleetManager(source: SectorEntityToken, var minPoints: Float, var maxPoints: Float, quality: AbyssProcgen.Tier) : SourceBasedFleetManager(source, 1f, 0, 1, 999f) {




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

    override fun spawnFleet(): CampaignFleetAPI? {

        //Prevents the game spawning fleets when the player isnt in the system or a neighbour
        if (!AbyssUtils.playerInNeighbourOrSystem(source.starSystem)) return null

        val random = Random(randomSeed)

        var points = MathUtils.getRandomNumberInRange(minPoints, maxPoints)

        var type = FleetTypes.PATROL_SMALL
        if (points > 60) type = FleetTypes.PATROL_MEDIUM
        if (points > 120) type = FleetTypes.PATROL_LARGE

        val params = FleetParamsV3(null,
            source.locationInHyperspace,
            Factions.REMNANTS,
            1f,
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

        val fleet = FleetFactoryV3.createFleet(params)

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