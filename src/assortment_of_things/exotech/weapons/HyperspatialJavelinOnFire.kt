package assortment_of_things.exotech.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI

class HyperspatialJavelinOnFire : OnFireEffectPlugin {
    override fun onFire(projectile: DamagingProjectileAPI, weapon: WeaponAPI?, engine: CombatEngineAPI) {
        var target = weapon?.ship?.shipTarget
        engine.addPlugin(HyperspatialJavelinHomingEffect(projectile, target))
    }
}