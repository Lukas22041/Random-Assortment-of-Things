package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f

class LeaniraSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null
    var interval = IntervalUtil(10f, 10f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship!!.mouseTarget == null) return

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            ship!!.useSystem()
        }
    }

}