package assortment_of_things.abyss.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI

class SarielLauncherEveryframe : EveryFrameWeaponEffectPlugin {
    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI?) {

        if (!weapon!!.ship.hasListenerOfClass(SarielLauncherListener::class.java)) {
            weapon.ship.addListener(SarielLauncherListener(weapon.ship))
        }

    }

}