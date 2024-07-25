package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f

class HypatiaSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    //Remember last assignment. Do not warp again if its still the same assignment that was warped to before.
    var previousAssignment: AssignmentInfo? = null


    //Need to check if the advance in phase stays correct
    var maximumWarpTime = 30f
    var warpTime = maximumWarpTime

    //Actual target to keep track off for the disabling of the system
    var targetEntity: CombatEntityAPI? = null

    var interval = IntervalUtil(0.2f, 0.4f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return

        if (ship!!.system.isActive) {
            maximumWarpTime -= 1 * amount
        }

        interval.advance(amount)
        if (interval.intervalElapsed()) {

            var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)
            var taskManager = manager.getTaskManager(ship!!.isAlly)
            var assignment = taskManager.getAssignmentFor(ship!!)

            var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 2500f, 2500f)
            var nearbyShips = ArrayList<ShipAPI>()
            nearbyShipsIterator.forEach { nearbyShips.add(it as ShipAPI) }

            var hasNearbyAlly = nearbyShips.any { it.owner == ship!!.owner }
            var hasNearbyHostile = nearbyShips.any { it.owner == ship!!.owner }

            //Do not repeat assignments
            if (assignment == previousAssignment) {
                return
            }

            if (ship!!.system.isActive) {

                var warp = false




                //Check for assignments
                if (assignment != null) {
                    var type = assignment.type
                    var entity = assignment.target






                }
                //If no assignments, select a new target to go to if no allies or opponents are near by
                else if (!hasNearbyAlly && !hasNearbyHostile){




                    //Make sure to set the maneuver target to the new target
                }




                if (warp) {
                    ship!!.useSystem()
                    previousAssignment = assignment
                    warpTime = maximumWarpTime
                }

            } else {

                var shouldStop = false

                if (maximumWarpTime <= 0) {
                    shouldStop = true
                }




                if (shouldStop) {
                    warpTime = maximumWarpTime
                    targetEntity = null
                    ship!!.useSystem()
                }

            }
        }
    }
}