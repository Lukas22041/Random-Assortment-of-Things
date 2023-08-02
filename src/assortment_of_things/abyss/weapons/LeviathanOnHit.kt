package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class LeviathanOnHit : OnHitEffectPlugin {
    /* override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        var script = object : BaseEveryFrameCombatPlugin() {

            var interval = IntervalUtil(0.2f, 0.2f)

            override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
                super.advance(amount, events)

                if (projectile == null || projectile.isExpired) {
                    Global.getCombatEngine().removePlugin(this)
                }

                if (engine!!.isPaused) return

                *//* Global.getCombatEngine().addNebulaParticle(projectile!!.location, Vector2f(), 20f, 0f, 1f, 1f, 1f,
                     Color(255, 0, 100))*//*

                var point = MathUtils.getRandomPointInCircle(projectile!!.location, 200f)

                interval.advance(amount)
                if (interval.intervalElapsed()) {
                    Global.getCombatEngine().spawnEmpArc(projectile!!.source, Vector2f(projectile.location), projectile, SimpleEntity(point), DamageType.ENERGY,
                        10f, 10f, 200f, "tachyon_lance_emp_impact", 20f,
                        Color(255, 0, 100), Color(255, 0, 100))
                }
            }
        }

        Global.getCombatEngine().addPlugin(script)
    }*/
    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?,point: Vector2f?, shieldHit: Boolean,damageResult: ApplyDamageResultAPI?,engine: CombatEngineAPI?) {
        if (target !is ShipAPI) return



        var max = MathUtils.getRandomNumberInRange(3,5)

        var color = Color(255, 0, 100)
        var explosionColor = Color(100, 0, 0)

        if (!shieldHit) {

            val emp = projectile!!.empAmount * 0.2f
            val dam = projectile.damageAmount * 0.1f
            for (i in 0 until max)
            {
                engine!!.spawnEmpArc(projectile.source, point, target, target, DamageType.ENERGY, dam, emp,
                    100000f,
                    "tachyon_lance_emp_impact", 20f,  // thickness
                    color, Color(255, 255, 255, 255))
            }

        }

        if (shieldHit) {
            var exploSpec = DamagingExplosionSpec(1f, 100f, 40f, projectile!!.damageAmount * 0.25f, 0f, CollisionClass.HITS_SHIPS_AND_ASTEROIDS
                , CollisionClass.HITS_SHIPS_AND_ASTEROIDS, 15f, 10f, 3f, 15, color.setAlpha(75), explosionColor)

            Global.getCombatEngine().spawnDamagingExplosion(exploSpec, projectile.source, projectile.location, true)
        }
    }
}