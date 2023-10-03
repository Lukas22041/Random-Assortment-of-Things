package assortment_of_things.relics.conditions

import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class EngineeredUtopia : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {
        market.hazard.modifyFlat(id, -0.20f, condition.name)
        market.accessibilityMod.modifyPercent(id, 25f, condition.name)
        market.stats.dynamic.getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, -1f)
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
        tooltip.addPara("+25%% accessibility", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+25%")
        tooltip.addPara("-20%% hazard rating", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "-20%")
        tooltip.addSpacer(5f)

    }

}