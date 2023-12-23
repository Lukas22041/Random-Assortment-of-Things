package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.ui.SettlementCustomProduction
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ShipyardFacility : BaseSettlementFacility() {

    var budget = 350000f
    var budgetPerMonth = 20000f

    override fun apply() {
        settlement.stats.maxProductionBudget.modifyFlat("shipyard", budget)
        settlement.stats.productionBudgetPerMonth.modifyFlat("shipyard", budgetPerMonth)

        settlement.currentProductionBudget += budgetPerMonth
    }

    override fun unapply() {
        settlement.stats.maxProductionBudget.unmodify("shipyard")
        settlement.stats.productionBudgetPerMonth.unmodify("shipyard")
    }

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        var budgetString = Misc.getDGSCredits(budget)
        var budgetPerMonthString = Misc.getDGSCredits(budgetPerMonth)
        tooltip.addPara("Constructs the infrastructure to enable custom production orders for ship hulls. " +
                "The orders have a maxmimum budget of $budgetString credits. Every month the budget recovers by $budgetPerMonthString credits. " +
                "\n\nIncreases in the production budget stack with those from other facilities. Production costs 20%% more than through other means.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "ship hulls", "$budgetString", "$budgetPerMonthString", "20%")
    }

    override fun populateSettlementDialogOrder(): Int {
        return 2
    }

    override fun populateSettlementDialog(dialog: InteractionDialogAPI, plugin: RATInteractionPlugin) {
        if (!plugin.optionPanel.hasOption("Order custom production")) {
            plugin.createOption("Order custom production") {
                dialog.showCustomProductionPicker(SettlementCustomProduction(settlement))
            }
            plugin.optionPanel.setTooltip("Order custom production", "Order production based on what facilities have been build. " +
                    "Current production orders are displayed in the settlements intel entry and will be delivered towards the settlements storage.")
        }
    }
}