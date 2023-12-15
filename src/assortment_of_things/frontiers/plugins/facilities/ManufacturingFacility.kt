package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ManufacturingFacility : BaseSettlementFacility() {

    var budget = 250000f
    var budgetPerMonth = 20000f

    override fun apply() {
        settlement.stats.productionBudget.modifyMult("manufacturing", budget)
        settlement.stats.productionBudgetPerMonth.modifyMult("manufacturing", budgetPerMonth)
    }

    override fun unapply() {
        settlement.stats.productionBudget.unmodify("manufacturing")
        settlement.stats.productionBudgetPerMonth.unmodify("manufacturing")
    }

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        var budgetString = Misc.getDGSCredits(budget)
        var budgetPerMonthString = Misc.getDGSCredits(budgetPerMonth)
        tooltip.addPara("Constructs the infrastructure to enable custom production orders for weapons and fighter wings. " +
                "The orders have a maxmimum budget of $budgetString credits. Every month the budget recovers by $budgetPerMonthString credits. " +
                "\n\nIncreases in the production budget stack with those from other facilities.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "weapons", "fighter wings", "$budgetString", "$budgetPerMonthString")
    }
}