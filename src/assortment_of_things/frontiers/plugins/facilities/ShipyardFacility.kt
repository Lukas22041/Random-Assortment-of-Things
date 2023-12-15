package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ShipyardFacility : BaseSettlementFacility() {

    var budget = 500000f
    var budgetPerMonth = 40000f

    override fun apply() {
        settlement.stats.productionBudget.modifyMult("shipyard", budget)
        settlement.stats.productionBudgetPerMonth.modifyMult("shipyard", budgetPerMonth)
    }

    override fun unapply() {
        settlement.stats.productionBudget.unmodify("shipyard")
        settlement.stats.productionBudgetPerMonth.unmodify("shipyard")
    }

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        var budgetString = Misc.getDGSCredits(budget)
        var budgetPerMonthString = Misc.getDGSCredits(budgetPerMonth)
        tooltip.addPara("Constructs the infrastructure to enable custom production orders for ship hulls. " +
                "The orders have a maxmimum budget of $budgetString credits. Every month the budget recovers by $budgetPerMonthString credits. " +
                "\n\nIncreases in the production budget stack with those from other facilities.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "ship hulls", "$budgetString", "$budgetPerMonthString")
    }

    override fun populateSettlementDialogOrder(): Int {
        return 2
    }

    override fun populateSettlementDialog(dialog: InteractionDialogAPI, plugin: RATInteractionPlugin) {
        if (!plugin.optionPanel.hasOption("Order custom production")) {
            plugin.createOption("Order custom production") {

            }
        }
    }
}