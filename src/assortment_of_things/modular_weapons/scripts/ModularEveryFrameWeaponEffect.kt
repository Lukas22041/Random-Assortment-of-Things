package assortment_of_things.modular_weapons.scripts

import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import java.awt.Color

class ModularEveryFrameWeaponEffect : EveryFrameWeaponEffectPlugin {

    var current = 0f

    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI?) {
        var effects = ModularWeaponLoader.getWeaponEffects(weapon!!.spec.weaponId)

        for (effect in effects)
        {
            effect.advance(amount, engine, weapon)
        }

    }
}