package assortment_of_things.exotech.shipsystems.ai

import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.combat.isVisibleToSide
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class HypatiaSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    //Remember last assignment. Do not warp again if its still the same assignment that was warped to before.
    var previousAssignment: AssignmentInfo? = null

    //However clear the assignment after a while, just in case
    var clearAssignmentTimeMax = 40f
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


    var distanceForUnwarp = 850f
    var distanceForOffset = 450f


    var reachedTarget = false

    var previousTarget: CombatEntityAPI? = null

    var activated = false

    //Entity placed on destination, apply a force code to space them out from eachother
    var landingPoint: CombatEntityAPI? = null

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

            var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 3000f, 3000f)
            var nearbyShips = ArrayList<ShipAPI>()
            nearbyShipsIterator.forEach { if((it as ShipAPI).isVisibleToSide(ship!!.owner)) {nearbyShips.add(it as ShipAPI) } }

            var hasNearbyAlly = nearbyShips.any { it.owner == ship!!.owner && it.isAlive && it != ship && MathUtils.getDistance(ship, it) <= 1800}
            var hasNearbyHostile = nearbyShips.any { it.owner != ship!!.owner && it.isAlive && it != ship && MathUtils.getDistance(ship, it) <= minDistanceNonAssignmentWarp }

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
                            var other = entity.ship
                            assignmentTarget = other

                            //Jump together with an ally if it is currently warping and this ship is assigned to follow
                          /*  if (other.baseOrModSpec().hullId == ship!!.baseOrModSpec().hullId) {
                                if (other.system.isActive) {
                                    //Skip Distance Calculation, just start a warp
                                    targetEntity = other
                                }
                            }*/
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
                    var isInArc = Misc.isInArc(ship!!.facing, 55f, ship!!.location, targetEntity!!.location)
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

                        //Place point slightly in front of destination instead
                        var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                        var loc = targetEntity!!.location
                        var offset = MathUtils.getPointOnCircumference(loc, distanceForOffset, angle-180)

                        landingPoint = SimpleEntity(offset)

                        landingPoint!!.collisionRadius = targetEntity!!.collisionRadius
                        landingPoint!!.mass = ship!!.mass
                        landingPoint!!.owner = ship!!.owner

                        //Global.getCombatEngine().addLayeredRenderingPlugin(LandingPointRenderer(landingPoint!!))


                        //Get & Create List if it doesnt exist
                        //Even if added to the engine, those simple entities do not appear on the object grid, so this list is required
                        var landings = Global.getCombatEngine().customData.get("rat_hypatia_landings") as MutableList<CombatEntityAPI>?
                        if (landings == null) {
                            landings = mutableListOf()
                            Global.getCombatEngine().customData.set("rat_hypatia_landings", landings)

                        }

                        landings.add(landingPoint!!)

                    }

                }

            }
        }



        activeInterval.advance(amount)
        if (activeInterval.intervalElapsed()) {

        }

        //Can not interval this part, certain flags get overwritten every frame (specificly while near an opponent), which breaks the movement code
        if (ship!!.system.isActive) {

            ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS, 0.5f)
            var shouldStop = false

            if (warpTime <= 0) {
                shouldStop = true
            }

            //Recalculate position once more after system finished charging
            //This is to prevent an issue where the location of the real target has already moved during the 5 seconds of chargeup
            if (!activated && ship!!.system.state == ShipSystemAPI.SystemState.ACTIVE) {
                activated = true

                var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                var loc = targetEntity!!.location
                var offset = MathUtils.getPointOnCircumference(loc, distanceForOffset, angle-180)

                landingPoint!!.location.set(offset)
            }



            var landings = Global.getCombatEngine().customData.get("rat_hypatia_landings") as MutableList<CombatEntityAPI>?


            if (targetEntity != null && landingPoint != null) {


                //Move Points

                var distanceFromPoint = MathUtils.getDistance(ship!!, landingPoint)

                // Stop Updating the point once the ship gets close
                //Hopefully this avoids suttering, and ships missing their landing
                if (distanceFromPoint >= distanceForUnwarp * 1.8) {

                    if (landings != null) {
                        var shipsIter = Global.getCombatEngine().shipGrid.getCheckIterator(landingPoint!!.location, 3000f, 3000f)
                        var ships = ArrayList<ShipAPI>()

                        //Add ships, ignore phased ships
                        shipsIter.forEach { if (it is ShipAPI && !it.isPhased ) {ships.add(it) } }

                        var objects = ArrayList<CombatEntityAPI>()
                        objects.addAll(ships)
                        objects.addAll(landings)

                        for (other in objects) {
                            if (other == landingPoint) continue
                            if (other == ship) continue

                            var angle = Misc.getAngleInDegrees(landingPoint!!.location, other.location)
                            var distance = MathUtils.getDistance(landingPoint!!, other)

                            if (distance <= 700f) {
                                var force = 20f
                                if (other.owner != landingPoint!!.owner) {
                                    force = 60f
                                }
                                var dir = MathUtils.getPointOnCircumference(Vector2f(), force, angle-180f)
                                landingPoint!!.location.set(landingPoint!!.location.plus(dir))
                            }
                        }
                    }

                }





                //Using offset entity
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET, 1f, landingPoint!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST, 1f, landingPoint!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.MOVEMENT_DEST_WHILE_SIDETRACKED, 1f, landingPoint!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.BIGGEST_THREAT, 1f, null)

                var angle = Misc.getAngleInDegrees(ship!!.location, landingPoint!!.location)
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 1f, angle)

                var distance = MathUtils.getDistance(ship, landingPoint!!.location)



                if (distance <= distanceForUnwarp) {
                    reachedTarget = true
                }

                //Fixes a weird bug
              /*  if (distance > distanceForUnwarp * 2) {
                    var isInArc = Misc.isInArc(ship!!.facing, 10f, ship!!.location, offsetEntity!!.location)
                    if (ship!!.shipAI != null && isInArc) {
                        ship!!.blockCommandForOneFrame(ShipCommand.TURN_LEFT)
                        ship!!.blockCommandForOneFrame(ShipCommand.TURN_RIGHT)
                    }
                }*/

                if (reachedTarget && distance >= distanceForUnwarp * 1.3f) {
                    shouldStop = true
                }

                if (reachedTarget) {
                    shouldStop = true
                }
            }




            if (!Global.getCombatEngine().isPaused) {
                //Add some code to prevent unwarps if a hostile ship is in the immediate surrounding
               /* var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 1200f, 1200f)
                var nearbyShips = ArrayList<ShipAPI>()
                nearbyShipsIterator.forEach { nearbyShips.add(it as ShipAPI) }

                if (nearbyShips.any { MathUtils.getDistance(ship, it) <= 10 && it != ship}) {
                    shouldStop = false
                }*/
            }



            if (shouldStop) {
                warpTime = maximumWarpTime
                targetEntity = null
                reachedTarget = false

                //Remove Landing Point
                landings?.remove(landingPoint)

                var activated = false

                ship!!.useSystem()
            }
        }
    }


    //From Lazylib
    fun applyForce(entity: CombatEntityAPI, direction: Vector2f, force: Float) : Vector2f {
        // Filter out forces without a direction
        var force = force
        if (VectorUtils.isZeroVector(direction)) {
            return Vector2f()
        }

        // Force is far too weak otherwise
        force *= 100f

        // Avoid divide-by-zero errors...
        val mass = Math.max(1f, entity.mass)
        // Calculate the velocity change and its resulting vector
        // Don't bother going over Starsector's speed cap
        val velChange = Math.min(1250f, force / mass)
        val dir = Vector2f()
        direction.normalise(dir)
        dir.scale(velChange)
        // Apply our velocity change
        return dir
    }

    fun applyForce(entity: CombatEntityAPI, direction: Float, force: Float) : Vector2f {
        return applyForce(entity, MathUtils.getPointOnCircumference(Vector2f(0f, 0f), 1f, direction), force)
    }



}



class LandingPointRenderer(var point: CombatEntityAPI) : BaseCombatLayeredRenderingPlugin() {

    var sprite: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")

    override fun getRenderRadius(): Float {
        return 1000000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS)
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        sprite!!.renderAtCenter(point.location.x, point.location.y)
        sprite!!.color = Color(200, 0, 50)

    }

}