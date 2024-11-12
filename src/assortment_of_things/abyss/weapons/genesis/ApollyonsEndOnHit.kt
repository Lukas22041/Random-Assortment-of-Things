package assortment_of_things.abyss.weapons.genesis

import assortment_of_things.combat.VFXRenderer
import assortment_of_things.misc.GraphicLibEffects
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnHitEffectPlugin
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ApollyonsEndOnHit : OnHitEffectPlugin {
    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean,  damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {

        GraphicLibEffects.CustomRippleDistortion(point,
            Vector2f(),
            300f,
            2f,
            false,
            0f,
            360f,
            1f,
            0f,0f,0.3f,
            0.3f,0f
        )

        if (!shieldHit) {
            VFXRenderer.addExplosion(MathUtils.getRandomNumberInRange(0.8f, 1.2f), Vector2f(point), Color(250,102,102), Color(252, 76, 123),150f, MathUtils.getRandomNumberInRange(0f, 360f), 15f)
            Global.getSoundPlayer().playSound("hit_hull_solid", 1.1f, 0.6f, point, Vector2f())
        } else {
            VFXRenderer.addExplosion(MathUtils.getRandomNumberInRange(0.8f, 1.2f), Vector2f(point), Color(250,102,102), Color(252, 76, 123),100f, MathUtils.getRandomNumberInRange(0f, 360f), 5f)
        }



    }
}