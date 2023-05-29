package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc


class StatEscapeVelocity : ModularWeaponEffect() {
    override fun getName(): String {
        return "Escape Velocity"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the weapons range by 100 units.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "range" ,"100")
    }

    override fun getResourceCost(data: SectorWeaponData) {
        data.addCraftingCost(Commodities.SUPPLIES, 5f, this)
        data.addCraftingCost(RATItems.SALVAGED_WEAPON_COMPONENTS, 5f, this)
    }

    override fun getType(): ModularEffectModifier {
        return ModularEffectModifier.Stat
    }

    override fun addStats(stats: SectorWeaponData) {
        super.addStats(stats)

        stats.range.modifyFlat(getName(), 100f)
    }

}