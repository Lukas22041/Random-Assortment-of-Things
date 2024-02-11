package assortment_of_things.abyss.weapons

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class GenesisMoteOnHit : OnHitEffectPlugin {
    override fun onHit(projectile: DamagingProjectileAPI, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
     /*   var spec = DamagingExplosionSpec(1f,
            40f, 30f,
            20f, 5f,
            CollisionClass.HITS_SHIPS_AND_ASTEROIDS, CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
            2f, 20f,
            1f,12,
            AbyssUtils.GENESIS_COLOR.setAlpha(25), Color(20, 0, 20, 0)
        )

        spec.isShowGraphic = false

        engine!!.spawnDamagingExplosion(spec, projectile.source, Vector2f(projectile.location), false)*/
    }

}