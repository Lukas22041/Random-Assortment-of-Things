package assortment_of_things.abyss.weapons.genesis

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbaddonLanceOnFire : OnFireEffectPlugin {

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {

        var velocity1 = MathUtils.getPointOnCircumference(Vector2f(), 30f, weapon!!.currAngle).plus(weapon.ship.velocity)
        var velocity2 = MathUtils.getPointOnCircumference(Vector2f(), 25f, weapon!!.currAngle).plus(weapon.ship.velocity)

        engine!!.spawnExplosion(projectile!!.location, velocity1, Color(47, 111, 237), 45f, 3f)
        engine!!.spawnExplosion(projectile!!.location, velocity2, AbyssUtils.GENESIS_COLOR, 35f, 2.5f)
    }


}

