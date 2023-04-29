package assortment_of_things.modular_weapons.effects

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI


class PassiveGuidance : ModularWeaponEffect() {
    override fun getName(): String {
        return "Guidance"
    }

    override fun getCost(): Int {
        return 30
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Causes the projectile to move towards nearby targets.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Passive
    }


    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        super.onFire(projectile, weapon, engine)

        var data = engine!!.customData.get("rat_modular_homing_projectiles") as MutableList<DamagingProjectileAPI>?
        if (data == null)
        {
            data = ArrayList()
        }
        data.add(projectile!!)
        engine!!.customData.set("rat_modular_homing_projectiles", data)
    }

}