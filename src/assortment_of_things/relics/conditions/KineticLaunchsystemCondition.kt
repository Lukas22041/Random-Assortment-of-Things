package assortment_of_things.relics.conditions

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class KineticLaunchsystemCondition : BaseMarketConditionPlugin() {


    private var volatileDemand = 2f

    override fun apply(id: String?) {
        var industry = market?.industries?.find { it.spec.hasTag("spaceport") } ?: return

        if (industry.isFunctional) {
            market.accessibilityMod.modifyFlat(id, 0.25f, condition.name)
            industry.getDemand(Commodities.VOLATILES).quantity.modifyFlat(id + "_0", volatileDemand, condition.name)
        }
        else {
            market.accessibilityMod.unmodifyPercent(id)
            industry.getDemand(Commodities.VOLATILES).quantity.unmodifyFlat(id + "_0")
        }
    }

    override fun unapply(id: String?) {
    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
      /*  var color = market.textColorForFactionOrPlanet
        tooltip!!.addTitle(condition.getName(), color)*/

        tooltip!!.addSpacer(10f)
        tooltip.addPara("The planet has multiple experimental launch systems spread across the surface that make space travel easier, but come at a heavy resource cost." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "")
        tooltip.addSpacer(10f)


        tooltip.addPara("+25%% accesibility", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+25%")
        tooltip.addPara("+${volatileDemand.toInt()} volatile demand", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+${volatileDemand.toInt()}")

    }



}