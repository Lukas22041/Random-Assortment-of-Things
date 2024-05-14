package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.shipsystem.TemporalStasisShipsystem
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class TemporalStasisSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    var interval = IntervalUtil(0.5f, 0.5f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system
        if (system.isActive || system.isCoolingDown) return

        interval.advance(amount)
        if (!interval.intervalElapsed()) return

        var maxRange = TemporalStasisShipsystem.getMaxRange()

        var nearbyTargets = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, maxRange * 2, maxRange * 2)
        var targetsInRange = ArrayList<ShipAPI>()

       /* var alliesTargets = ArrayList<ShipAPI>()
        for (nearbyTarget in nearbyTargets) {
            var nearbyAllyShip = nearbyTarget as ShipAPI
            if (nearbyAllyShip == ship) continue
            if (nearbyAllyShip.owner != ship!!.owner) continue
            if (nearbyAllyShip.isFighter) continue
            if (nearbyAllyShip.isFrigate) continue
            if (nearbyAllyShip.fluxLevel >= 0.85f) continue
            if (nearbyAllyShip.shipTarget == null) continue

            var allyTarget = nearbyAllyShip.shipTarget
            if (allyTarget == target) continue

            alliesTargets.add(allyTarget)
        }*/

        ship!!.aiFlags.unsetFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)

        if (ship!!.fluxLevel >= 0.90f && target != null && !ship!!.phaseCloak.isActive) {
            var distance = MathUtils.getDistance(ship, target)
            if (distance <= maxRange) {
                ship!!.aiFlags.setFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM, 1f, target)
                ship!!.useSystem()
                return
            }
        }

        for (nearbyTarget in nearbyTargets) {
            var nearbyTargetShip = nearbyTarget as ShipAPI
            if (nearbyTargetShip.owner == ship!!.owner) continue
            if (!nearbyTargetShip.isAlive) continue
            if (nearbyTargetShip.isFighter) continue

            var distance = MathUtils.getDistance(ship, nearbyTargetShip)
            if (distance >= maxRange) continue

            if (nearbyTarget.fluxLevel >= 0.85f || nearbyTarget.fluxTracker.isOverloaded) continue

            targetsInRange.add(nearbyTargetShip)
        }

        if (targetsInRange.size <= 1) return
        if (target != null) {
            targetsInRange.remove(target)
        }

        var threats = mutableMapOf<ShipAPI, Float>()

        for (remainingTarget in targetsInRange) {
            var threat = 0f
            var fleetPoints = remainingTarget.baseOrModSpec().fleetPoints
            if (fleetPoints <= 10) threat +=1
            else if (fleetPoints <= 20) threat +=2
            else if (fleetPoints <= 25) threat +=3
            else if (fleetPoints <= 30) threat +=4
            else threat +=5

            if (remainingTarget.shipTarget == ship) threat += 3

           // if (alliesTargets.contains(remainingTarget)) threat -= 3

            threats.put(remainingTarget, threat)
        }

        var sortedThreats = threats.toList().sortedByDescending { it.second }

        var actualTarget = sortedThreats.first().first

        ship!!.aiFlags.setFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM, 1f, actualTarget)
        ship!!.useSystem()


    }
}