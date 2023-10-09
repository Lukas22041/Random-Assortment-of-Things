package assortment_of_things.relics.conditions

import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class WarscapeCondition : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {

        var industry = market?.industries?.find { it.spec.hasTag("techmining") } ?: return
        market.hazard.modifyFlat(id, 0.10f, condition.name)

        var base = 0 + market.size

        if (industry.isFunctional) {
            industry.supply("${id}_0", Commodities.SHIPS, base, condition.name)
        }
        else {
            industry.getSupply(Commodities.SHIPS).quantity.unmodifyFlat(id + "_0")
        }
    }

    override fun unapply(id: String?) {

    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
      /*  var color = market.textColorForFactionOrPlanet
        tooltip!!.addTitle(condition.getName(), color)*/
        var base = 0 + market.size

        tooltip!!.addSpacer(10f)
        tooltip.addPara("The surface is litered by the remains of a long forgotten war. Tech-Mining on this planet is able to recover ship hulls and weapons, however the scarred surface proves difficult to live on." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Tech-Mining")
        tooltip.addSpacer(10f)


        tooltip.addPara("+10%% hazard rating", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+10%")
        tooltip.addPara("+$base ship hulls and weapons (scales with market size)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+$base")

    }

}