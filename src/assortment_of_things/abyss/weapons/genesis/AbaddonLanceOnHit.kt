package assortment_of_things.abyss.weapons.genesis

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.combat.entities.Missile
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class AbaddonLanceOnHit : OnHitEffectPlugin {

    companion object {
        fun spawnExplosion(projectile: DamagingProjectileAPI?, point: Vector2f?) {
            if (projectile!!.customData.containsKey("rat_charge_exploded")) return
            projectile!!.setCustomData("rat_charge_exploded", true)

            val dam = projectile!!.damageAmount * 0.15f

            var explosion = DamagingExplosionSpec(1f, 1200f, 600f, dam, dam * 0.5f,
                // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass,
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter,
                5f, 10f, 5f, 100,
                AbyssUtils.GENESIS_COLOR.setAlpha(150), Color(20, 45, 90))

            Global.getCombatEngine().spawnDamagingExplosion(explosion, projectile!!.weapon.ship, projectile.location, false)

            Global.getSoundPlayer().playSound("rat_abyss_genesis_large1_explosion", 0.8f, 0.9f, point, Vector2f())
        }
    }

    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {

        if (target is Missile) return

        spawnExplosion(projectile, point)


       /* val emp = projectile.empAmount * 0.05f
        val dam = projectile.damageAmount * 0.05f

        var targetsNearby = Global.getCombatEngine().ships.filter { it.owner != projectile.weapon.ship.owner && MathUtils.getDistance(it, projectile) <= range && !it.isHulk}

        for (otherTarget in targetsNearby) {

            var count = 3
            if (otherTarget.isFighter) {
                if (Random().nextFloat() >= 0.8f) continue
                count = 2
            }

            for (i in 0 until 1) {
                var color = Misc.interpolateColor(AbyssUtils.GENESIS_COLOR, Color(47, 111, 237), MathUtils.getRandomNumberInRange(0f, 1f))
                color = color.setAlpha(220)

                engine!!.spawnEmpArcPierceShields(projectile.source, point, otherTarget, otherTarget, DamageType.ENERGY, dam, emp,  // emp
                    100000f,  // max range
                    "tachyon_lance_emp_impact", 25f,  // thickness
                    color, color)
            }
        }*/
    }

}

