package assortment_of_things.relics.conditions

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AncientIndustriesCondition : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {

        var heavyIndustry = market?.industries?.find { it.spec.hasTag("heavyindustry") }
        if (heavyIndustry != null) {
            if (heavyIndustry.isFunctional) {
                heavyIndustry.supply("${id}_0", Commodities.HEAVY_MACHINERY, 1, condition.name)
                heavyIndustry.supply("${id}_1", Commodities.SUPPLIES, 1, condition.name)
                heavyIndustry.supply("${id}_2", Commodities.HAND_WEAPONS, 1, condition.name)
                heavyIndustry.supply("${id}_3", Commodities.SHIPS, 1, condition.name)
            }
            else {
                heavyIndustry.getSupply(Commodities.HEAVY_MACHINERY).quantity.unmodifyFlat(id + "_0")
                heavyIndustry.getSupply(Commodities.SUPPLIES).quantity.unmodifyFlat(id + "_1")
                heavyIndustry.getSupply(Commodities.HAND_WEAPONS).quantity.unmodifyFlat(id + "_2")
                heavyIndustry.getSupply(Commodities.SHIPS).quantity.unmodifyFlat(id + "_3")
            }
        }

        var lightIndustry = market?.industries?.find { it.spec.hasTag("lightindustry") }
        if (lightIndustry != null) {
            if (lightIndustry.isFunctional) {
                lightIndustry.supply("${id}_0", Commodities.DOMESTIC_GOODS, 1, condition.name)

                if (!market.isIllegal(Commodities.LUXURY_GOODS)) {
                    lightIndustry.supply("${id}_1", Commodities.LUXURY_GOODS, 1, condition.name)
                }

                if (!market.isIllegal(Commodities.DRUGS)) {
                    lightIndustry.supply("${id}_2", Commodities.DRUGS, 1, condition.name)
                }
            }
            else {
                lightIndustry.getSupply(Commodities.DOMESTIC_GOODS).quantity.unmodifyFlat(id + "_0")
                lightIndustry.getSupply(Commodities.LUXURY_GOODS).quantity.unmodifyFlat(id + "_1")
                lightIndustry.getSupply(Commodities.DRUGS).quantity.unmodifyFlat(id + "_2")
            }
        }

    }

    override fun unapply(id: String?) {

    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        var increase = 1

        tooltip!!.addSpacer(10f)
        tooltip.addPara("The planet was once gifted with large-scale industrial efforts. Some of it is in recoverable state, and can be re-used to extend any new industrial expansion." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), )
        tooltip.addSpacer(10f)

        tooltip.addPara("+$increase for all output of heavy industries", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+$increase")
        tooltip.addPara("+$increase for all output of light industries", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+$increase")

    }

}