package assortment_of_things.abyss.shipsystem.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class TestShipSystemAI : ShipSystemAIScript {

    lateinit var ship: ShipAPI
    lateinit var system: ShipSystemAPI
    lateinit var engine: CombatEngineAPI

    var interval = IntervalUtil(5f, 10f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship!!
        this.system = system!!
        this.engine = Global.getCombatEngine()
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        var flags = ship!!.aiFlags
        var state = system.state
        if (state == ShipSystemAPI.SystemState.IN || state == ShipSystemAPI.SystemState.OUT) return

        var alwaysDeactivate = false
        if (ship.fluxLevel > 0.7f) alwaysDeactivate = true

        interval.advance(amount)

        if (!interval.intervalElapsed() && !alwaysDeactivate) return

        var useSystem = shouldHaveActiveSystem(target)




        if (useSystem && !system.isActive && !alwaysDeactivate) {
            ship.useSystem()
        }

        if ((!useSystem || alwaysDeactivate) && system.isActive) {
            ship.useSystem()
        }

    }

    fun shouldHaveActiveSystem(target: ShipAPI?) : Boolean {
        var flags = ship!!.aiFlags
        var state = system.state

        var range = Math.exp(7.5).toFloat() + 100f

        if (target == null) return false

        var iter = engine.shipGrid.getCheckIterator(ship.location, range + 500, range + 500)
        var count = 0f
        for (it in iter)
        {
            if (it !is ShipAPI) continue
            if (ship == it) continue
            if (ship.owner != it.owner) continue
            if (ship.hullSize == ShipAPI.HullSize.FIGHTER) continue
            if (MathUtils.getDistance(ship.location, it.location) > range + it.collisionRadius) continue

            if (it.hullSize == ShipAPI.HullSize.FRIGATE) {
                count += 0.5f
            }
            else {
                count += 1f
            }
        }

        if (count >= 1) {
            return true
        }


        return false
    }
}