package assortment_of_things.exotech.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lazywizard.lazylib.MathUtils

class HypersatialJavelinEffect : EveryFrameWeaponEffectPlugin {



    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI) {
        weapon.ensureClonedSpec()
        var spec = weapon.spec
        var ship = weapon.ship
        if (spec is ProjectileWeaponSpecAPI) {
            spec.maxAmmo = 5
            weapon.maxAmmo = 5
            weapon.ammo = MathUtils.clamp(weapon.ammo, 0, 5)

            if (ship.isPhased) {
                weapon.ammoTracker.reloadProgress += spec.ammoPerSecond / 5f * amount
            }
            else {

            }
        }
    }

}