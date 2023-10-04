package assortment_of_things.relics.conditions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class EngineeredUtopia : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {
        market.hazard.modifyFlat(id, -0.25f, condition.name)
        market.accessibilityMod.modifyPercent(id, 20f, condition.name)
        market.stats.dynamic.getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, -1f)

        var industry = market?.industries?.find { it.spec.hasTag("farming") } ?: return

        if (industry.isFunctional) {
            industry.supply(id, Commodities.FOOD, 2, condition.name)
        }
        else {
            industry.getSupply(Commodities.FOOD).quantity.unmodifyFlat(id)
        }


    }

    override fun unapply(id: String?) {
        market.stats.dynamic.getMod(Stats.MAX_INDUSTRIES).unmodify(id)
    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        var base = 0 + market.size

        tooltip!!.addSpacer(10f)
        tooltip.addPara("This planet was once heavily terraformed to get it in to a better state. Much of the effort hasnt been able to survive past the collapse, but the mechanisms that are still active improve the conditons of this planet drasticly compared to less fortunate planets." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "")
        tooltip.addSpacer(10f)

        tooltip.addPara("Due to this however, industrial efforts on this planet are limited, as they risk to debalance the ecosystem created by those factors.", 0f)

        tooltip.addSpacer(10f)
        tooltip.addPara("-1 max industries", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "-1")
        tooltip.addPara("+20%% accessibility", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+20%")
        tooltip.addPara("-25%% hazard rating", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "-25%")
        tooltip.addPara("+2 food production (Farming)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+2")
        tooltip.addSpacer(5f)

    }

}