package assortment_of_things.modular_weapons.scripts

import assortment_of_things.modular_weapons.effects.ModularTooltipCreator
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI

class ModularOnFireEffectPlugin : OnFireEffectPlugin {
    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        var effects = ModularWeaponLoader.getWeaponEffects(weapon!!.spec.weaponId)

        for (effect in effects)
        {
            effect.onFire(projectile, weapon, engine)
        }
    }
}