package assortment_of_things.exotech.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class HyperspatialJavelinOnFire : OnFireEffectPlugin {
    override fun onFire(projectile: DamagingProjectileAPI, weapon: WeaponAPI?, engine: CombatEngineAPI) {
        var target = weapon?.ship?.shipTarget
        engine.addPlugin(HyperspatialJavelinHomingEffect(projectile, target))
       // engine.spawnExplosion(Vector2f(projectile.location), Vector2f(weapon!!.ship.velocity), Color(252,143,0, 25), 100f, 2f)


        if (weapon!!.spec.weaponId == "rat_hyper_dart") {
            for (i in 0..5) {
                engine.addNebulaParticle(MathUtils.getRandomPointOnCircumference(Vector2f(projectile.location), MathUtils.getRandomNumberInRange(0f, 10f)),
                    MathUtils.getRandomPointOnCircumference(Vector2f(weapon!!.ship.velocity), MathUtils.getRandomNumberInRange(10f, 30f)),
                    MathUtils.getRandomNumberInRange(1f, 20f), 0f, 1f, 1f, 1f, Color(252,143,0, 100))
            }
        }
        else {
            for (i in 0..20) {
                engine.addNebulaParticle(MathUtils.getRandomPointOnCircumference(Vector2f(projectile.location), MathUtils.getRandomNumberInRange(0f, 10f)),
                    MathUtils.getRandomPointOnCircumference(Vector2f(weapon!!.ship.velocity), MathUtils.getRandomNumberInRange(20f, 40f)),
                    MathUtils.getRandomNumberInRange(2f, 20f), 0f, 1f, 1f, 1f, Color(252,143,0, 100))
            }
        }


    }
}