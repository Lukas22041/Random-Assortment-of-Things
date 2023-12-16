package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class StockpileFacility : BaseSettlementFacility() {

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Stores a small amount of several commodites, being made up of leftover materials that the that the settlement acquired over time. The procured materials are free to use. " +
                "Stockpiles at most 500 units worth of cargo per commodity.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "500")
    }

    override fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {
        return super.addToMonthlyCargo(current)
    }

    override fun reportEconomyMonthEnd() {

    }

    override fun reportEconomyTick(iterIndex: Int) {
        val report = SharedData.getData().currentReport



        var storageNode = report.getNode(MonthlyReport.STORAGE)
        storageNode.name = "Storage"
        storageNode.custom = MonthlyReport.STORAGE
        storageNode.tooltipCreator = report.getMonthlyReportTooltip()

        var mNode = report.getNode(storageNode, settlement.primaryPlanet.market.getId())
        mNode.name = settlement.primaryPlanet.market.getName() + " (" + "desc" + ")"
        mNode.custom = settlement.primaryPlanet.market
        mNode.custom2 = MonthlyReport.STORAGE
        mNode.upkeep = 0f
    }
}