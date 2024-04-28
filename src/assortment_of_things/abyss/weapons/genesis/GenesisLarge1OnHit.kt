package assortment_of_things.abyss.weapons.genesis

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.entities.Missile
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class GenesisLarge1OnHit : OnHitEffectPlugin {

    var range = 700f

    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {

        if (target is ShipAPI && target.isFighter) return
        if (target is Missile) return

        var explosion = DamagingExplosionSpec(6f, 1000f, 100f, 100f, 10f,
            CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
            CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
            5f, 10f, 1f, 100,
            AbyssUtils.GENESIS_COLOR, Color(15, 30, 60))

        Global.getCombatEngine().spawnDamagingExplosion(explosion, projectile!!.weapon.ship, projectile.location, false)


        val emp = projectile.empAmount * 0.05f
        val dam = projectile.damageAmount * 0.05f

        var targetsNearby = Global.getCombatEngine().ships.filter { it.owner != projectile.weapon.ship.owner && MathUtils.getDistance(it, projectile) <= range }

        for (otherTarget in targetsNearby) {

            for (i in 0 until 3) {
                var color = Misc.interpolateColor(AbyssUtils.GENESIS_COLOR, Color(47, 111, 237), MathUtils.getRandomNumberInRange(0f, 1f))
                color = color.setAlpha(220)

                engine!!.spawnEmpArcPierceShields(projectile.source, point, otherTarget, otherTarget, DamageType.ENERGY, dam, emp,  // emp
                    100000f,  // max range
                    "tachyon_lance_emp_impact", 25f,  // thickness
                    color, color)
            }
        }
    }

}

