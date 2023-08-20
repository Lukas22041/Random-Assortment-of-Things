package assortment_of_things.abyss.weapons

import assortment_of_things.combat.ParallaxParticleRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class LeviathanEffect : OnHitEffectPlugin, OnFireEffectPlugin {

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






      /*  for (i in 0..100) {

            var x = MathUtils.getRandomNumberInRange(point!!.x + -25f, point.x + 25f)
            var y = MathUtils.getRandomNumberInRange(point!!.y + -25f, point.y + 25f)

            var velocity = MathUtils.getRandomPointInCircle(Vector2f(), 50f)

            ParallaxParticleRenderer.createParticle(MathUtils.getRandomNumberInRange(3f, 6f), Vector2f(x, y), velocity, MathUtils.getRandomNumberInRange(3f, 25f), 1f, 15)
        }*/


    }

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        engine!!.spawnExplosion(projectile!!.location, weapon!!.ship.velocity, Color(255, 0, 100), 35f, 0.8f)


    }
}