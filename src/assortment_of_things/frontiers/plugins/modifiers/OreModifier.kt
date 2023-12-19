package assortment_of_things.frontiers.plugins.modifiers

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.frontiers.data.SettlementModifierSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

open class OreModifier() : BaseSettlementModifier() {


    var budgetPerMonth = hashMapOf(
        1 to 1000,
        2 to 2500,
        3 to 5000,
        4 to 7500,
        5 to 10000)

    override fun getDescription(tooltip: TooltipMakerAPI) {
        var budget = budgetPerMonth.get(getTier())!!
        if (getID() == "rareore") {
            budget = (budget * 1.5f).toInt()
        }
        tooltip.addPara("${getName()}:\n" +
                "${getName()} can be mined within this location. A site with ${getName().lowercase()} will increase the monthly custom production budget by ${budget} credits if a refinery is build.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "${getName()}", "$budget")
    }


    override fun reportEconomyMonthEnd() {
        if (settlement.hasFacility("refinery")) {
            var budget = budgetPerMonth.get(getTier())!!
            if (getID() == "rareore") {
                budget = (budget * 1.5f).toInt()
            }
            settlement.stats.productionBudgetPerMonth.modifyFlat("ore", budget.toFloat())
        }
        else {
            settlement.stats.productionBudgetPerMonth.unmodify("ore")
        }
    }

    override fun apply() {

    }


}