package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class MerrowSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system

        if (system.isCoolingDown) return
        if (system.isActive)  return

        if (AbyssalsCoreHullmod.isChronosCore(ship!!)) {
            if (target == null) return
            if (!ship!!.areAnyEnemiesInRange()) return

            var range = 0f
            for (weapon in ship!!.allWeapons) {
                if (weapon.range > range)
                {
                    range = weapon.range
                }
            }
            if (MathUtils.getDistance(ship!!, target) > range + 50) return

            if (!flags.hasFlag(AIFlags.MANEUVER_TARGET)) return

            ship!!.useSystem()
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) {
            if (ship!!.fluxLevel < 0.35f) return
            if (ship!!.shield != null && ship!!.shield.isOff) return

            ship!!.useSystem()
        }
    }
}