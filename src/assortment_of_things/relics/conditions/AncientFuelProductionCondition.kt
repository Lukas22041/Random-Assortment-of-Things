package assortment_of_things.relics.conditions

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AncientFuelProductionCondition : BaseMarketConditionPlugin() {


    var increase = 1


    override fun apply(id: String?) {

        var mining = market?.industries?.find { it.spec.hasTag("mining") }
        if (mining != null) {
            if (mining.isFunctional) {
                mining.supply("${id}_0", Commodities.VOLATILES, increase, condition.name)
            }
            else {
                mining.getSupply(Commodities.VOLATILES).quantity.unmodifyFlat(id + "_0")
            }
        }

        var fuel = market?.industries?.find { it.spec.hasTag("fuelprod") }
        if (fuel != null) {
            if (fuel.isFunctional) {
                fuel.supply("${id}_0", Commodities.FUEL, increase, condition.name)
            }
            else {
                fuel.getSupply(Commodities.FUEL).quantity.unmodifyFlat(id + "_0")
            }
        }

    }

    override fun unapply(id: String?) {

    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {

        tooltip!!.addSpacer(10f)
        tooltip.addPara("This planet was once a centre for fuel production in the sector, much of the infrastructure for this purpose remains and could be revived." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), )
        tooltip.addSpacer(10f)

        tooltip.addPara("+$increase fuel production (Fuel Production)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+$increase")
        tooltip.addPara("+$increase volatiles production (Mining)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+$increase")

    }

}