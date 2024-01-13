package assortment_of_things.abyss.shipsystem.ai

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f

class RaguelSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    var wasActivated = false
    var cooldownInterval = IntervalUtil(4f, 7f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system

        if (wasActivated) {

            cooldownInterval.advance(amount)
            if (cooldownInterval.intervalElapsed()) {
                wasActivated = false
            }
            else
            {
                return
            }
        }

        if ((flags.hasFlag(AIFlags.BACKING_OFF) || flags.hasFlag(AIFlags.BACK_OFF) || ship!!.fluxLevel >= 0.8f)) {

            wasActivated = true
            ship!!.useSystem()
        }

        if (system.ammo <= 2) return
        if (target != null || flags.hasFlag(AIFlags.PURSUING) ||  flags.hasFlag(AIFlags.RUN_QUICKLY)) {
            wasActivated = true
            ship!!.useSystem()
        }


    }
}