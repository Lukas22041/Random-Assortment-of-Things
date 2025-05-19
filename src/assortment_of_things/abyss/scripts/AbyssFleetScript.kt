package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.BiomeDepth
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.clearTarget

/* Has to be added to the system, not the fleet.
This ensures it doesnt run often outside of the system and allows it to continue to run while the fleet is despawned.


Goal of this script is to cause fleets to stop following the player if they move to far away, and to prevent them following you deeper in to other biomes.
If the fleet is near its home location, and the player is far away, it should also despawn.
*/
class AbyssFleetScript(var fleet: CampaignFleetAPI, var source: SectorEntityToken, var biome: BaseAbyssBiome) : EveryFrameScript, FleetEventListener {

    init {
        fleet.addEventListener(this)
    }

    var interval = IntervalUtil(0.20f, 0.25f)

    override fun advance(amount: Float) {

        //Try adding/removing from system when it should, if the fleets defense target is despawned and the player isnt near the fleet, despawn it entirely.

        var isPlayerInAbyss = AbyssUtils.isPlayerInAbyss()
        if (isPlayerInAbyss) {
            interval.advance(amount)
        } else{
            interval.advance(amount * 0.2f) //Do this stuff less often if the player is not around.
        }
        if (interval.intervalElapsed()) {

            //Ensure certain memflags stay active
            //fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
            fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true
            fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true

            var data = AbyssUtils.getData()
            var manager = data.biomeManager
            var player = Global.getSector().playerFleet
            var system = AbyssUtils.getSystem()

            var distanceToPlayer = MathUtils.getDistance(Global.getSector().playerFleet, fleet)
            var distanceToSource = MathUtils.getDistance(source, fleet)
            var isAlive = fleet.isAlive
            var isSourceAlive = source.isAlive

            var tactical = (fleet.ai as ModularFleetAIAPI).tacticalModule
            var target = tactical.target

            var minDistToSource = 5000f
            var maxDistToPlayer = 16000f



            //Despawn if the player is not close, the fleet isnt targeting the player, and the fleet is close to its source.
            if (isAlive && target == null && distanceToSource <= minDistToSource && (distanceToPlayer >= maxDistToPlayer || !isPlayerInAbyss)) {

                //If the source despawned, permanently remove.
                if (!isSourceAlive) {
                    system.removeEntity(fleet)
                    isAlive = false
                    finished = true
                    fleet.removeEventListener(this)
                    return
                } else {
                    system.removeEntity(fleet)
                    isAlive = false
                }


            }

            if (!isAlive && distanceToPlayer < maxDistToPlayer && isPlayerInAbyss) {
                system.addEntity(fleet)
                isAlive = true
            }


            if (isAlive) {
                var currentCell = manager.getCell(fleet)
                if (target != null && target != source && currentCell.depth != BiomeDepth.BORDER && currentCell.getBiome() != biome) {

                    fleet.memoryWithoutUpdate[MemFlags.FLEET_DO_NOT_IGNORE_PLAYER] = false
                    fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORES_OTHER_FLEETS] = true
                    fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS] = true

                    //tactical.forceTargetReEval()

                    fleet.interactionTarget = null
                    tactical.setPriorityTarget(null, 5f, false)
                    tactical.target = null


                    //fleet.addAssignment(FleetAssignment.INTERCEPT, target, 2f)
                } else if (currentCell.depth != BiomeDepth.BORDER && currentCell.getBiome() == biome){
                    fleet.memoryWithoutUpdate[MemFlags.FLEET_DO_NOT_IGNORE_PLAYER] = true
                    fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORES_OTHER_FLEETS] = false
                    fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS] = false
                }

                //Patrol if it has no source, before it despawns
                if (!isSourceAlive && !fleet.isCurrentAssignment(FleetAssignment.PATROL_SYSTEM)) {
                    fleet.clearAssignments()
                    fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, source, 9999999f)
                }
                //Ensure the location keeps being defended.
                else if (!fleet.isCurrentAssignment(FleetAssignment.DEFEND_LOCATION)) {
                    fleet.clearAssignments()
                    fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, source, 9999999f)
                }
            }
        }

    }








    var finished = false
    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?, param: Any?) {
        finished = true
        fleet?.removeEventListener(this)
    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

    }

    override fun isDone(): Boolean {
        return finished
    }


    override fun runWhilePaused(): Boolean {
        return false
    }
}