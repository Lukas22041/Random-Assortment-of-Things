package assortment_of_things.modular_weapons.scripts

import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnHitEffectPlugin
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import org.lwjgl.util.vector.Vector2f

class ModularOnHitEffect : OnHitEffectPlugin {

    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?,shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        var effects = ModularWeaponLoader.getWeaponEffects(projectile!!.weapon!!.spec.weaponId)

        for (effect in effects)
        {
            effect.onHit(projectile, target, point, shieldHit, damageResult, engine)
        }
    }
}