package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class MorkothSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null
    var wasActivated = false
    var interval = IntervalUtil(7f, 9f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system

        if (system.isCoolingDown) return
        if (system.isActive)  return

        if (wasActivated)
        {
            interval.advance(amount)
            if (interval.intervalElapsed())
            {
                wasActivated = false
            }
            return
        }

        if (AbyssalsCoreHullmod.isChronosCore(ship!!)) {
            if (target == null) return
            if (!ship!!.areAnyEnemiesInRange()) return

            if (flags.hasFlag(AIFlags.BACK_OFF) || flags.hasFlag(AIFlags.BACKING_OFF)) {
                ship!!.useSystem()
                wasActivated = true
            }

            if (ship!!.system.ammo <= 1f) return

            if (!flags.hasFlag(AIFlags.MANEUVER_TARGET) && !flags.hasFlag(AIFlags.PURSUING)) return

            ship!!.useSystem()
            wasActivated = true

        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) {
            if (ship!!.shield == null || ship!!.shield.isOff) return
            if (ship!!.fluxLevel > 0.3f) {
                ship!!.useSystem()
                wasActivated = true
            }

            var projectiles = Global.getCombatEngine().allObjectGrid.getCheckIterator(ship!!.location, 1000f, 1000f)

            var enable = false
            for (proj in projectiles)
            {
                if (proj !is DamagingProjectileAPI) continue
                if (proj is MissileAPI)
                {
                    var ai = proj.ai
                    if (ai is GuidedMissileAI)
                    {
                        var projTarget = ai.target
                        if (projTarget == ship)
                        {
                            enable = true
                        }
                    }
                }
                if (proj.damage.baseDamage > 150f)
                {
                    enable = true
                }
            }

            if (enable)
            {
                ship!!.useSystem()
                wasActivated = true
            }
        }
    }
}