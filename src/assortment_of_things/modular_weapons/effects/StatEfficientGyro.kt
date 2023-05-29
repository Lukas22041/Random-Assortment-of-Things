package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc


class StatEfficientGyro : ModularWeaponEffect() {
    override fun getName(): String {
        return "Efficient Gyros"
    }

    override fun getCost(): Int {
        return 10
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Doubles the weapons turn rate.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Doubles", "turn rate")
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

        stats.turnrate.modifyMult(getName(), 2f)
    }

}