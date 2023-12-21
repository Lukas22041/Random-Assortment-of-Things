package assortment_of_things.exotech.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lazywizard.lazylib.MathUtils

class PWaveMissileEffect : EveryFrameWeaponEffectPlugin {



    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI) {
        weapon.ensureClonedSpec()
        var spec = weapon.spec
        var ship = weapon.ship
        weapon.ammoTracker.ammoPerSecond = 0.000000001f
        if (spec is ProjectileWeaponSpecAPI) {
            if (ship.isPhased) {
                weapon.ammoTracker.reloadProgress += spec.ammoPerSecond / spec.reloadSize * amount
            }
        }
    }
}