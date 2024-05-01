package assortment_of_things.abyss.weapons.genesis

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AzazelsBladeOnFire : OnFireEffectPlugin {
    override fun onFire(projectile: DamagingProjectileAPI, weapon: WeaponAPI?, engine: CombatEngineAPI) {


        var launchSlots = weapon!!.spec.turretFireOffsets
        if (weapon.slot.isHardpoint) launchSlots = weapon.spec.hardpointFireOffsets

        for (slot in launchSlots) {
            var slotRotated = slot.rotate(weapon.currAngle)
            var loc = weapon.location.plus(slotRotated)
            var vel = Vector2f(projectile.velocity)

            var facing = projectile.facing

            var newProj = engine!!.spawnProjectile(projectile!!.source, projectile.weapon, projectile!!.weapon.spec.weaponId, loc,
                MathUtils.getRandomNumberInRange(facing, facing),
                vel) as DamagingProjectileAPI

            newProj.damageAmount /= launchSlots.size
            newProj.damage.fluxComponent /= launchSlots.size

            var target = weapon?.ship?.shipTarget
            engine.addPlugin(AzazelsBladeHomingEffect(newProj,target))
        }

        engine!!.removeEntity(projectile)
    }
}