package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc


class StatAutoloader : ModularWeaponEffect() {
    override fun getName(): String {
        return "Autoloader"
    }

    override fun getCost(): Int {
        return 30
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the weapons firerate by 25%%. If the weapon uses ammo, increases the maximum ammo count by 20 and increases the ammo recharge rate by 1.", 0f
            ,
            Misc.getTextColor(), Misc.getHighlightColor(), "25%", "uses ammo", "20", "1")
    }

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.SUPPLIES, 3f, this)
        data.addCraftingCost(RATItems.SALVAGED_WEAPON_COMPONENTS, 3f, this)
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.ammoPerSecond.modifyFlat(getName(), 1f)
        stats.maxAmmo.modifyFlat(getName(), 20)
        stats.reloadSize.modifyFlat(getName(), 1f)

        stats.burstDelay.modifyMult(getName(), 0.75f)
        stats.chargeDown.modifyMult(getName(), 0.75f)
        stats.chargeUp.modifyMult(getName(), 0.75f)
    }
}