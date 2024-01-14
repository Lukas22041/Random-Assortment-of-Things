package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class AbyssalSkimmerSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    var wasActivated = false
    var cooldownInterval = IntervalUtil(2f, 2f)

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

        if (ship!!.velocity.length() < 40f) return
        if ((flags.hasFlag(AIFlags.BACKING_OFF) )) {

            wasActivated = true
            ship!!.useSystem()
        }

        if (system.ammo <= 1) return
        if (flags.hasFlag(AIFlags.PURSUING) || flags.hasFlag(AIFlags.RUN_QUICKLY) || flags.hasFlag(AIFlags.MANEUVER_TARGET)) {
            wasActivated = true
            ship!!.useSystem()
        }
    }
}