package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.util.ModularWeaponLoader
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
        tooltip.addPara("20% chance for a projectile to be overcharged, causing it to unleash weak EMP Strikes on to nearby fighters and ships as the projectile travels along its path.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Passive
    }


    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        super.onFire(projectile, weapon, engine)

        if (ModularWeaponLoader.getData(projectile!!.weapon.id).rngCheck(0.20f, 0))
        {
            var data = engine!!.customData.get("rat_modular_overvolt_projectiles") as MutableList<DamagingProjectileAPI>?
            if (data == null)
            {
                data = ArrayList()
            }
            data.add(projectile!!)
            engine!!.customData.set("rat_modular_overvolt_projectiles", data)
            projectile!!.setCustomData("rat_modular_overvolt_interval", IntervalUtil(0.2f, 0.5f))


        }
    }

}