package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class StockpileFacility : BaseSettlementFacility() {

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("A dedicated storage facility for the settlement. Removes storage fees from both the settlement and colony, if there is one. " +
                "Also increases the settlements income by 10%%.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "10%")
    }

    override fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {
        return super.addToMonthlyCargo(current)
    }

    override fun apply() {
        settlement.stats.income.modifyPercent("stockpile", 10f)
    }

    override fun unapply() {
        settlement.stats.income.unmodify("stockpile")
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