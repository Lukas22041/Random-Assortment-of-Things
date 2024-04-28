package assortment_of_things.abyss.weapons.genesis

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class GenesisLarge1OnFire : OnFireEffectPlugin {

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {

        var velocity1 = MathUtils.getPointOnCircumference(Vector2f(), 30f, weapon!!.currAngle).plus(weapon.ship.velocity)
        var velocity2 = MathUtils.getPointOnCircumference(Vector2f(), 25f, weapon!!.currAngle).plus(weapon.ship.velocity)

        engine!!.spawnExplosion(projectile!!.location, velocity1, Color(47, 111, 237), 45f, 3f)
        engine!!.spawnExplosion(projectile!!.location, velocity2, AbyssUtils.GENESIS_COLOR, 35f, 2.5f)
    }


}

