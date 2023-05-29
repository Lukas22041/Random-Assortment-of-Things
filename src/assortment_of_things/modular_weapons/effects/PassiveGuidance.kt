package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc


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
        tooltip.addPara("Causes the projectile to move towards nearby targets.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "move towards")
    }

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.GAMMA_CORE, 1f, this)
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Passive
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

        projectile.setCustomData("rat_modular_isPD", ModularWeaponLoader.getData(projectile!!.weapon.id).isPD)
    }

}