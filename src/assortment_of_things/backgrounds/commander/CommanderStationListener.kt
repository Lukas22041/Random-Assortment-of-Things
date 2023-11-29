package assortment_of_things.backgrounds.commander

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getStorageCargo

class CommanderStationListener(var market: MarketAPI) : EconomyTickListener {

    var currentProductionBudget = 0f
    var maxProductionCapacity = 500000f
    var budgetPerMonth = 50000f

    override fun reportEconomyTick(iterIndex: Int) {
        val numIter = Global.getSettings().getFloat("economyIterPerMonth")
        val f = 1f / numIter

        //CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        //CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        val report = SharedData.getData().currentReport

        val fleetNode = report.getNode(MonthlyReport.FLEET)
        fleetNode.name = "Fleet"
        fleetNode.custom = MonthlyReport.FLEET
        fleetNode.tooltipCreator = report.monthlyReportTooltip

        val stipend: Float = calculateIncome().toFloat()
        val stipendNode = report.getNode(fleetNode, "rat_node_id_station_commander")
        stipendNode.income += stipend * f

        if (stipendNode.name == null) {
            stipendNode.name = "${market.name} Commander"
            stipendNode.icon = market.faction.getCrest()
            stipendNode.tooltipCreator = object : TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any): Float {
                    return 450f
                }

                override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                    tooltip.addPara("Your monthly income from commanding ${market.name}",
                        0f)
                }
            }
        }
    }

    fun calculateIncome() : Int {
        var income = 0

        return 2000 * Global.getSector().playerPerson.stats.level
    }



    override fun reportEconomyMonthEnd() {
        var cargo = market.getStorageCargo()

        currentProductionBudget += budgetPerMonth
        currentProductionBudget = MathUtils.clamp(currentProductionBudget, 0f, maxProductionCapacity)


    }
}