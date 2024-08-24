package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.combat.isVisibleToSide
import org.lwjgl.util.vector.Vector2f
import kotlin.collections.ArrayList

class SupernovaSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    var clearAssignmentTimeMax = 40f
    var clearAssignmentTime = clearAssignmentTimeMax

    //Actual target to keep track off for the disabling of the system
    var targetEntity: CombatEntityAPI? = null

    //Decent difference to make ships do actions less synchronosely
    var inactiveInterval = IntervalUtil(1f, 2f)

    var minDistanceFromHostileForWarp = 2800


    companion object {
        var distanceForUnwarp = 800f
        var distanceForOffset = 300f

    }



    var ai: ShipAIPlugin? = null

    var previousTarget: CombatEntityAPI? = null


    //Entity placed on destination, apply a force code to space them out from eachother


    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return



        if (ship!!.system.isActive) {

        } else if (previousTarget != null){
            clearAssignmentTime -= 1f * amount
        }

        if (clearAssignmentTime <= 0f) {
            previousTarget = null
        }

        //Reset previous target if its really far away
        if (previousTarget != null) {
            var distance = MathUtils.getDistance(ship, previousTarget)
            if (distance >= minDistanceFromHostileForWarp * 1.5f) {
                previousTarget = null
                clearAssignmentTime = clearAssignmentTimeMax
            }
        }

        //Dont need to do any more if within those states
        if (ship!!.system.isCoolingDown || ship!!.system.isChargedown || ship!!.system.isChargeup) return

        inactiveInterval.advance(amount)
        if (inactiveInterval.intervalElapsed()) {

            var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 3200f, 3200f)
            var nearbyShips = ArrayList<ShipAPI>()
            nearbyShipsIterator.forEach { if((it as ShipAPI).isVisibleToSide(ship!!.owner)) {nearbyShips.add(it as ShipAPI) } }
            nearbyShips.remove(ship)

            var hasNearbyHostile = nearbyShips.any { it.owner != ship!!.owner && it.isAlive && it != ship && MathUtils.getDistance(ship, it) <= minDistanceFromHostileForWarp }
            //var hasNearbyAlly = nearbyShips.any { it.owner == ship!!.owner && it.isAlive && it != ship && MathUtils.getDistance(ship, it) <= 1800}

            var allShips = Global.getCombatEngine().ships

            if (!ship!!.system.isActive) {

                var shouldWarp = false

                if (!hasNearbyHostile) {
                    var allOpponents = allShips.filter { it.owner != ship!!.owner }
                    allOpponents = allOpponents.filter { it.isAlive && !it.isFighter }
                    allOpponents = allOpponents.filter { it.isVisibleToSide(ship!!.owner) } //Make sure only visible opponents are considered

                    var availableTargets = WeightedRandomPicker<ShipAPI>()

                    for (opponent in allOpponents) {
                        var weight = 0.1f

                        var othersNearOpponentIterator = Global.getCombatEngine().shipGrid.getCheckIterator(opponent!!.location, 2200f, 2200f)
                        var othersNearOpponent = ArrayList<ShipAPI>()
                        othersNearOpponentIterator.forEach { othersNearOpponent.add(it as ShipAPI) }
                        othersNearOpponent.remove(opponent)

                        var otherHasAllies = othersNearOpponent.any { it.owner == ship!!.owner}
                        var otherHasOpponents = othersNearOpponent.any { it.owner == ship!!.owner}

                        if (opponent.isFrigate) weight += 1
                        if (opponent.isDestroyer) weight += 0.5f

                        //Only make opposing cruisers and capitals warp targets if they have an ally fighting them.
                        if (opponent.isCruiser || opponent.isCapital) {
                            if (otherHasAllies) {
                                weight += 0.5f
                            }
                        }

                        if (otherHasAllies) weight += 0.5f
                        if (otherHasOpponents) weight * 0.75f

                        availableTargets.add(opponent, weight)
                    }

                    if (!availableTargets.isEmpty) {
                        var pick = availableTargets.pick()
                        targetEntity = pick
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
                        clearAssignmentTime = clearAssignmentTimeMax
                        previousTarget = targetEntity

                        //Real destination

                        //Place point slightly in front of destination instead
                        var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                        var loc = targetEntity!!.location
                        var offset = MathUtils.getPointOnCircumference(loc, distanceForOffset, angle-180)

                        var landingPoint = SimpleEntity(offset)

                        landingPoint!!.collisionRadius = targetEntity!!.collisionRadius
                        landingPoint!!.mass = ship!!.mass
                        landingPoint!!.owner = ship!!.owner

                        //ToggleAI.setAIEnabled(ship!!, false)
                        ai = ship!!.shipAI
                        ship!!.shipAI = null

                        var script = SupernovaInWarpScript(ship!!, targetEntity!!, landingPoint!!, this)

                        ship!!.addListener(script)
                        script.init()

                        ship!!.useSystem()

                    }

                }

            }
        }
    }

    fun finishedWarp() {
        targetEntity = null
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

//Warp behaviour moved to another script as the shipai needs to be set to null to avoid other bugs, but that stops executing the base script.
class SupernovaInWarpScript(var ship: ShipAPI, var targetEntity: CombatEntityAPI, var landingPoint: CombatEntityAPI, var aiScript: SupernovaSystemAI) : AdvanceableListener {

    //Need to check if the advance in phase stays correct
    var maximumWarpTime = 35f
    var warpTime = maximumWarpTime

    var reachedTarget = false
    var activated = false


    fun init() {
       /* renderer = LandingPointRenderer(landingPoint!!)
        Global.getCombatEngine().addLayeredRenderingPlugin(renderer)*/
    }

    override fun advance(amount: Float) {
        ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS, 0.5f)
        var shouldStop = false

        /*if (ship.equals(Global.getCombatEngine().playerShip) && Global.getCombatEngine().isUIAutopilotOn()) {
            aiScript.finishedWarp()
            ship!!.removeListener(this)
        }*/


        if (ship.system.isChargedown) {
            ship!!.removeListener(this)

            aiScript.finishedWarp()
            return
        }

        if (!Global.getCombatEngine().isPaused && ship.system.state == ShipSystemAPI.SystemState.ACTIVE) {
            warpTime -= 1f * amount
        }

        if (warpTime <= 0) {
            shouldStop = true
        }

        if (targetEntity != null && landingPoint != null) {

            //Recalculate position once more after system finished charging
            //This is to prevent an issue where the location of the real target has already moved during the 5 seconds of chargeup
            if (!activated && ship!!.system.state == ShipSystemAPI.SystemState.ACTIVE) {
                activated = true

                var angle = Misc.getAngleInDegrees(ship!!.location, targetEntity!!.location)
                var loc = targetEntity!!.location
                var offset = MathUtils.getPointOnCircumference(loc, SupernovaSystemAI.distanceForOffset, angle-180)

                landingPoint!!.location.set(offset)
            }

            turnTowardsPointV2(ship!!, landingPoint!!.location, 0f)

            var distance = MathUtils.getDistance(ship, landingPoint!!.location)



            if (distance <= SupernovaSystemAI.distanceForUnwarp) {
                reachedTarget = true
            }

            if (reachedTarget && distance >= SupernovaSystemAI.distanceForUnwarp * 1.3f) {
                shouldStop = true
            }

            if (reachedTarget) {
                shouldStop = true
            }

        }


        //Prevent stop while being in the "IN" state as that ignores the useSystem call
        if (shouldStop && ship.system.state != ShipSystemAPI.SystemState.IN) {

            ship.shipAI = aiScript.ai

            aiScript.finishedWarp()

            ship.removeListener(this)

            ship.useSystem()


        }
    }


    fun turnTowardsPointV2(ship: ShipAPI, point: Vector2f?, angVel: Float): Boolean {
        val desiredFacing = Misc.getAngleInDegrees(ship.location, point)
        return turnTowardsFacingV2(ship, desiredFacing, angVel)
    }

    fun turnTowardsFacingV2(ship: ShipAPI, desiredFacing: Float, relativeAngVel: Float): Boolean {
        val turnVel = ship.angularVelocity - relativeAngVel
        val absTurnVel = Math.abs(turnVel)
        val turnDecel = ship.engineController.turnDeceleration
        // v t - 0.5 a t t = dist
        // dv = a t;  t = v / a
        val decelTime = absTurnVel / turnDecel
        val decelDistance = absTurnVel * decelTime - 0.5f * turnDecel * decelTime * decelTime
        val facingAfterNaturalDecel = ship.facing + Math.signum(turnVel) * decelDistance
        val diffWithEventualFacing = Misc.getAngleDiff(facingAfterNaturalDecel, desiredFacing)
        val diffWithCurrFacing = Misc.getAngleDiff(ship.facing, desiredFacing)
        if (diffWithEventualFacing > 1f) {
            var turnDir = Misc.getClosestTurnDirection(ship.facing, desiredFacing)
            if (Math.signum(turnVel) == Math.signum(turnDir)) {
                if (decelDistance > diffWithCurrFacing) {
                    turnDir = -turnDir
                }
            }
            if (turnDir < 0) {
                ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0)
                ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT)
            } else if (turnDir >= 0) {
                ship.giveCommand(ShipCommand.TURN_LEFT, null, 0)
                ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT)
            } else {

                //Prevent player from messing around
                ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT)
                ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT)

                return false
            }
        }
        return false
    }

}


