package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.util.vector.Vector2f

class AbyssalChargeSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null
    var wasActivated = false
    var interval = IntervalUtil(7f, 9f)

    var range = 1500f

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system

        if (system.isCoolingDown) return
        if (system.isActive)  return

        if (target != null) {
            var allies = AIUtils.getNearbyAllies(ship, range).filter { !it.isFighter && it.isAlive }
            if (allies.size >= 2) {
                ship!!.useSystem()
                return
            }
            if (MathUtils.getDistance(ship, target) >= 1500) {
                ship!!.useSystem()
                return
            }
            if (flags.hasFlag(AIFlags.BACK_OFF)) {
                ship!!.useSystem()
                return
            }
        }
        else {
            if (flags.hasFlag(AIFlags.MANEUVER_TARGET) || flags.hasFlag(AIFlags.PURSUING)) {
                ship!!.useSystem()
                return
            }

        }
    }
}