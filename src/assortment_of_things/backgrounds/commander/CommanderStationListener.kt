package assortment_of_things.backgrounds.commander

import assortment_of_things.backgrounds.commander.projects.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import org.magiclib.kotlin.getStorageCargo

class CommanderStationListener(var market: MarketAPI) : EconomyTickListener {

    var allProjects = ArrayList<BaseCommanderProject>()

    var bank = 0
    var maxCargo = 10000
    var currentProductionBudget = 0f
    var shipProduction = ArrayList<String>()
    var weaponProduction = ArrayList<String>()
    var fighterProduction = ArrayList<String>()

    var maxProjects = 8

    init {
        allProjects.add(SpaceportProject().apply { active = true })
        allProjects.add(StorageProject().apply { active = true })

        allProjects.add(MarketProject())
        allProjects.add(ExpandedQuartersProject())

        allProjects.add(ShipProductionProject())
        allProjects.add(WeaponProductionProject())

        allProjects.add(SupplyProductionProject())
        allProjects.add(FuelProductionProject())
    }

    fun hasActiveProject(string: String) : Boolean {
        return allProjects.find { it.getID() == string }?.active ?: return false
    }

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

        val stipend: Float = calculateIncome() * 0.2f
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
        for (project in allProjects) {
            if (project.active) {
                income += project.getIncome()
            }
        }
        return income
    }

    fun calculateProductionCapacity() : Float {
        var cap = 0f
        for (project in allProjects) {
            if (project.active) {
                cap += project.getCustomProductionBudget()
            }
        }
        return cap
    }

    override fun reportEconomyMonthEnd() {
        var cargo = market.getStorageCargo()

        currentProductionBudget = 0f
        for (project in allProjects) {
            if (project.active) {
                currentProductionBudget += project.getCustomProductionBudget()

                for ((commodity, amount) in project.addToMonthlyCargo()) {
                    if (cargo.getCommodityQuantity(commodity) + amount <= maxCargo) {
                        cargo.addCommodity(commodity, amount)
                    }
                }
            }
        }

        var income = calculateIncome()
        bank += (income * 0.8f).toInt()
    }
}