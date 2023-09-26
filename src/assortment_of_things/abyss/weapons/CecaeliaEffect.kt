package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class CecaeliaEffect : OnFireEffectPlugin {

    var shots = 8
    var maxSpread = 8f

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {

        for (i in 0 until shots) {
            var loc = projectile!!.location
            var vel = Vector2f(projectile.velocity)

            val randomVel: Vector2f = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(0f, 75f))
            randomVel.x += vel.x
            randomVel.y += vel.y

            var facing = projectile.facing

            var newProj = engine!!.spawnProjectile(projectile!!.source, projectile.weapon, projectile!!.weapon.spec.weaponId, loc,
                MathUtils.getRandomNumberInRange(facing - maxSpread, facing + maxSpread),
                randomVel) as DamagingProjectileAPI

            newProj.damageAmount /= shots
            newProj.damage.fluxComponent /= shots
        }

        engine!!.removeEntity(projectile)
    }

}