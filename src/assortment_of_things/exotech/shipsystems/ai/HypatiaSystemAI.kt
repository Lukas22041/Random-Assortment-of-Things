package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.combat.isVisibleToSide
import org.lwjgl.util.vector.Vector2f

class HypatiaSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    //Remember last assignment. Do not warp again if its still the same assignment that was warped to before.
    var previousAssignment: AssignmentInfo? = null

    //However clear the assignment after a while, just in case
    var clearAssignmentTimeMax = 30f
    var clearAssignmentTime = clearAssignmentTimeMax

    //Need to check if the advance in phase stays correct
    var maximumWarpTime = 35f
    var warpTime = maximumWarpTime

    //Actual target to keep track off for the disabling of the system
    var targetEntity: CombatEntityAPI? = null

    //Decent difference to make ships do actions less synchronosely
    var inactiveInterval = IntervalUtil(1f, 3f)
    var activeInterval = IntervalUtil(0.1f, 0.2f)

    var minDistanceNonAssignmentWarp = 3500
    var minDistanceAssignmentWarp = 3500
    var distanceForUnwarp = 1500f

    var reachedTarget = false

    var previousTarget: CombatEntityAPI? = null

    //Randomise a position around the target to fly to each time, avoids placing all ships in the same end location
   /* var radialOffset = MathUtils.getRandomNumberInRange(-35f, 35f)
    var distanceOffset = MathUtils.getRandomNumberInRange(200f, 800f)
    var angleAtStart = 0f*/

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return

        if (ship!!.system.isActive) {
            warpTime -= 1f * amount
        } else if (previousAssignment != null){
            clearAssignmentTime -= 1f * amount
        }

        if (clearAssignmentTime <= 0f) {
            clearAssignmentTime = clearAssignmentTimeMax
            //previousAssignment = null

            previousTarget = null
        }

        //Reset previous target if its really far away
        if (previousTarget != null) {
            var distance = MathUtils.getDistance(ship, previousTarget)
            if (distance >= minDistanceAssignmentWarp * 1.5f) {
                previousTarget = null
                clearAssignmentTime = clearAssignmentTimeMax
            }
        }

        //Dont need to do any more if within those states
        if (ship!!.system.isCoolingDown || ship!!.system.isChargedown || ship!!.system.isChargeup) return

        inactiveInterval.advance(amount)
        if (inactiveInterval.intervalElapsed()) {

            var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)
            var taskManager = manager.getTaskManager(ship!!.isAlly)
            var assignment = taskManager.getAssignmentFor(ship!!)

            var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 2800f, 2800f)
            var nearbyShips = ArrayList<ShipAPI>()
            nearbyShipsIterator.forEach { if((it as ShipAPI).isVisibleToSide(ship!!.owner)) {nearbyShips.add(it as ShipAPI) } }

            var hasNearbyAlly = nearbyShips.any { it.owner == ship!!.owner && it.isAlive && it != ship}
            var hasNearbyHostile = nearbyShips.any { it.owner != ship!!.owner && it.isAlive && it != ship }

            var allShips = Global.getCombatEngine().ships

            //Do not repeat assignments
            //Doesnt work, it might re-generate the assignment each call or something, idk
            /*if (assignment != null && assignment == previousAssignment) {
                return
            }*/

            if (!ship!!.system.isActive) {

                var shouldWarp = false




                //Check for assignments
                if (assignment != null) {
                    var type = assignment.type
                    var entity = assignment.target

                    var assignmentTarget: CombatEntityAPI? = null

                    if (entity != null) {
                        if (entity is CombatEntityAPI) {
                            assignmentTarget = entity
                        }
                        if (entity is BattleObjectiveAPI) {
                            assignmentTarget = entity
                        }
                        if (entity is DeployedFleetMemberAPI) {
                            assignmentTarget = entity.ship
                        }
                    }

                    if (assignmentTarget != null) {
                        var distance = MathUtils.getDistance(ship, assignmentTarget)

                        if (distance >= minDistanceAssignmentWarp) {
                            targetEntity = assignmentTarget
                        }
                    }


                }
                //If no assignments, select a new target to go to if no allies or opponents are near by
                else if (!hasNearbyAlly && !hasNearbyHostile){


                    for (other in allShips) {
                        var distance = MathUtils.getDistance(ship!!, other)
                        if (distance <= minDistanceAssignmentWarp) continue

                        //Continue if not visible
                        if (!other.isVisibleToSide(ship!!.owner)) continue

                        //Continue if no member
                        var otherMember = other.fleetMember ?: continue

                        var shipStrength = Misc.getMemberStrength(ship!!.fleetMember)

                        //Check for opposing ships with either no allies and at lower strength, or opposing ships with allies near them.
                        if (ship!!.owner != other.owner) {


                            var othersNearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(other.location, 2500f, 2500f)
                            var othersNearbyShips = ArrayList<ShipAPI>()
                            othersNearbyShipsIterator.forEach { othersNearbyShips.add(it as ShipAPI) }

                            var otherHasNearbyAlly = othersNearbyShips.any { it.owner != other!!.owner && it.isAlive && it != other }
                            var otherHasNearbyOpponent = othersNearbyShips.any { it.owner == other!!.owner && it.isAlive && it != other }

                            //If the opposing ship has allies from this AIs ship, warp towards it
                            if (otherHasNearbyAlly) {
                                targetEntity = other
                            }
                            //If the opposing ship doesnt have any ships of the same side
                            else if (!otherHasNearbyOpponent){
                                //If the opposing ship is weaker than this ship, warp to it.
                                var otherStrength = Misc.getMemberStrength(otherMember)

                                if (otherStrength < shipStrength) {
                                    targetEntity = other
                                }
                            }
                        }
                    }



                    //Make sure to set the maneuver target to the new target
                }




                //Clear Target just in case
                if (targetEntity != null) {
                    if (targetEntity!! is ShipAPI) {
                        var asShip = targetEntity as ShipAPI
                        if (!asShip.isAlive) {
                            targetEntity = null
                        }
                    }
                }

                if (targetEntity != null) {
                    /*var offset = getOffset(targetEntity!!)*/
                    ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1f, targetEntity!!.location)
                    //ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1.25f, targetEntity!!.location)

                    //otherwise those fuckers dont wanna turn early enough
                    //var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                    var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                    ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, angle)

                    //ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST, 5f, targetEntity!!.location)
                    var isInArc = Misc.isInArc(ship!!.facing, 45f, ship!!.location, targetEntity!!.location)
                        if (isInArc) {
                        shouldWarp = true
                    }
                }

                //Do not warp to recent things again
                if (previousTarget != null && previousTarget == targetEntity) {
                    shouldWarp = false
                }

                if (shouldWarp) {

                    //In some cases the system didnt activate at the start, may be because of travel drive
                    if (!ship!!.travelDrive.isActive) {
                        ship!!.useSystem()
                        previousAssignment = assignment
                        clearAssignmentTime = clearAssignmentTimeMax
                        warpTime = maximumWarpTime
                        previousTarget = targetEntity

                       // angleAtStart = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)

                    }

                }

            }
        }



        activeInterval.advance(amount)
        if (activeInterval.intervalElapsed()) {

        }

        //Can not interval this part, certain flags get overwritten every frame (specificly while near an opponent), which breaks the movement code
        if (ship!!.system.isActive) {

            var shouldStop = false

            if (warpTime <= 0) {
                shouldStop = true
            }

            if (targetEntity != null) {

                //var offset = getOffset(targetEntity!!)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1f, targetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST, 1f, targetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST_WHILE_SIDETRACKED, 1f, targetEntity!!.location)

                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.BIGGEST_THREAT, 1f, null)


                //ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1.25f, targetEntity!!.location)

                //otherwise those fuckers dont wanna turn early enough
                //var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, angle)

                var distance = MathUtils.getDistance(ship, targetEntity!!.location)
                if (distance <= distanceForUnwarp) {
                    reachedTarget = true
                }

                    if (distance > distanceForUnwarp * 1.2f) {
                        var isInArc = Misc.isInArc(ship!!.facing, 10f, ship!!.location, targetEntity!!.location)
                        if (ship!!.shipAI != null && isInArc) {
                            ship!!.blockCommandForOneFrame(ShipCommand.TURN_LEFT)
                            ship!!.blockCommandForOneFrame(ShipCommand.TURN_RIGHT)
                        }
                    }

                if (reachedTarget && distance >= distanceForUnwarp * 1.3f) {
                    shouldStop = true
                }

                if (reachedTarget) {
                    shouldStop = true
                }
            }

            //Add some code to prevent unwarps if a hostile ship is in the immediate surrounding
            var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 1000f, 1000f)
            var nearbyShips = ArrayList<ShipAPI>()
            nearbyShipsIterator.forEach { nearbyShips.add(it as ShipAPI) }

            if (nearbyShips.any { MathUtils.getDistance(ship, it) <= 50 && it != ship}) {
                shouldStop = false
            }

            if (shouldStop) {
                warpTime = maximumWarpTime
                targetEntity = null
                reachedTarget = false
                /* radialOffset = MathUtils.getRandomNumberInRange(-35f, 35f)
                 distanceOffset = MathUtils.getRandomNumberInRange(200f, 800f)*/
                ship!!.useSystem()
            }
        }
    }

   /* fun getOffset(target: CombatEntityAPI) : Vector2f {
        var point = MathUtils.getPointOnCircumference(target.location, distanceOffset, angleAtStart+radialOffset)
        return point
    }*/
}