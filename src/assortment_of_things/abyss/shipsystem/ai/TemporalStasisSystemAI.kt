package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.shipsystem.TemporalStasisShipsystem
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class TemporalStasisSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    var wasActivated = false
    var cooldownInterval = IntervalUtil(4f, 7f)

    var interval = IntervalUtil(0.15f, 0.15f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system
        if (system.isActive) return

        interval.advance(amount)
        if (!interval.intervalElapsed()) return

        var maxRange = TemporalStasisShipsystem.getMaxRange()

        var nearbyTargets = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, maxRange * 2, maxRange * 2)
        var targetsInRange = ArrayList<ShipAPI>()

        ship!!.aiFlags.unsetFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)

        if (ship!!.fluxLevel >= 0.85f && target != null) {
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

            targetsInRange.add(nearbyTargetShip)
        }

        if (targetsInRange.size <= 1) return
        if (target != null) {
            targetsInRange.remove(target)
        }

        var actualTarget = targetsInRange.random()

        ship!!.aiFlags.setFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM, 1f, actualTarget)
        ship!!.useSystem()


    }
}