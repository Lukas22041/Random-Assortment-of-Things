package assortment_of_things.modular_weapons.effects

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import kotlin.random.Random


class PassiveOvervolt : ModularWeaponEffect() {
    override fun getName(): String {
        return "Overvolt"
    }

    override fun getCost(): Int {
        return 10
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Unleashes weak EMP Strikes on to nearby fighters and ships as the projectile travels along its path.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Passive
    }


    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        super.onFire(projectile, weapon, engine)

        if (Random.nextFloat() > 0.5f) return

        var data = engine!!.customData.get("rat_modular_overvolt_projectiles") as MutableList<DamagingProjectileAPI>?
        if (data == null)
        {
            data = ArrayList()
        }
        data.add(projectile!!)
        engine!!.customData.set("rat_modular_overvolt_projectiles", data)
        projectile!!.setCustomData("rat_modular_overvolt_interval", IntervalUtil(0.1f, 0.4f))
    }

}