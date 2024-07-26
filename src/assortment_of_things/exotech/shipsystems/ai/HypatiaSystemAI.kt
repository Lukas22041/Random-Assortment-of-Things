package assortment_of_things.exotech.shipsystems.ai

import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
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
    var inactiveInterval = IntervalUtil(1f, 4f)
    var activeInterval = IntervalUtil(0.1f, 0.2f)

    var minDistanceNonAssignmentWarp = 3500
    var minDistanceAssignmentWarp = 3500
    var distanceForUnwarp = 1400f


    var reachedTarget = false

    var previousTarget: CombatEntityAPI? = null

    //Randomise a position around the target to fly to each time, avoids placing all ships in the same end location
    var offsetEntity: CombatEntityAPI? = null

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

                        //Real destination

                        var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                        var loc = targetEntity!!.location
                        var randAngle = MathUtils.getRandomNumberInRange(-55f, 55f)

                        var behind = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(500f, 800f), angle-180-randAngle)

                        offsetEntity = SimpleEntity(behind)
                        offsetEntity!!.collisionRadius = targetEntity!!.collisionRadius


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

            if (targetEntity != null && offsetEntity != null) {

                /*ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1f, targetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST, 1f, targetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST_WHILE_SIDETRACKED, 1f, targetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.BIGGEST_THREAT, 1f, null)

                var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, angle)

                var distance = MathUtils.getDistance(ship, targetEntity!!.location)*/


                //Using offset entity
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1f, offsetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST, 1f, offsetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST_WHILE_SIDETRACKED, 1f, offsetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.BIGGEST_THREAT, 1f, null)

                var angle = Misc.getAngleInDegrees(ship!!.location, offsetEntity!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, angle)

                var distance = MathUtils.getDistance(ship, offsetEntity!!.location)



                if (distance <= distanceForUnwarp) {
                    reachedTarget = true
                }

                //Fixes a weird bug
                if (distance > distanceForUnwarp * 2) {
                    var isInArc = Misc.isInArc(ship!!.facing, 10f, ship!!.location, offsetEntity!!.location)
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

            if (!Global.getCombatEngine().isPaused) {
                //Add some code to prevent unwarps if a hostile ship is in the immediate surrounding
                var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 1200f, 1200f)
                var nearbyShips = ArrayList<ShipAPI>()
                nearbyShipsIterator.forEach { nearbyShips.add(it as ShipAPI) }

              /*  //Push
                for (other in nearbyShips) {
                    if (ship == other) continue
                    if (ship!!.baseOrModSpec().hullId != other.baseOrModSpec().hullId) continue
                    //if (!other.system.isActive) continue

                    var angle = Misc.getAngleInDegrees(ship!!.location, other.location)
                    var distance = MathUtils.getDistance(ship!!, other)
                    var level = distance.levelBetween(ship!!.collisionRadius+other.collisionRadius, (ship!!.collisionRadius+other.collisionRadius) * 2)
                    level = 1-level
                    level *= level * level

                    CombatUtils.applyForce(other, angle, 5f * level)
                }*/

                if (nearbyShips.any { MathUtils.getDistance(ship, it) <= 10 && it != ship}) {
                    shouldStop = false
                }
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