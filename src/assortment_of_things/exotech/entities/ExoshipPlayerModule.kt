package assortment_of_things.exotech.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class ExoshipPlayerModule {

    var fuelProductionLevel = 0.25f
    var fuelPercentPerMonthMax = 20f / 100f
    var currentFuelPercent = 0.5f
    var maxFuelPercent = 1f

    var maxCost = 50000f

    var fuelPerLightyear = 1f / 100f

    var playerJoinsWarp = false

    var isPlayerOwned = true


    fun advance(amount: Float) {
        if (!Global.getSector().isPaused) {
            var days = Misc.getDays(amount)

            currentFuelPercent += (fuelPercentPerMonthMax * fuelProductionLevel) / 30f * days
            currentFuelPercent = MathUtils.clamp(currentFuelPercent, 0f, maxFuelPercent)


            if (currentFuelPercent < 1f) {
                val report = SharedData.getData().currentReport

                val colonyNode = report.getNode(MonthlyReport.OUTPOSTS)
                colonyNode.name = "Colonies"
                colonyNode.custom = MonthlyReport.OUTPOSTS
                colonyNode.tooltipCreator = report.monthlyReportTooltip

                var cost = computeMonthlyCost(fuelProductionLevel)

                val stipend = cost / 30f * days
                val stipendNode = report.getNode(colonyNode, "rat_exoship_cost")
                stipendNode.upkeep += stipend

                if (stipendNode.name == null) {
                    stipendNode.name = "Exoship Fuel Production"
                    stipendNode.icon = Global.getSector().getFaction("rat_exotech").getCrest()
                    stipendNode.tooltipCreator = object : TooltipCreator {
                        override fun isTooltipExpandable(tooltipParam: Any): Boolean {
                            return false
                        }

                        override fun getTooltipWidth(tooltipParam: Any): Float {
                            return 450f
                        }

                        override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                            tooltip.addPara("Your monthly cost for the Exoships fuel production. You can adjust it within its management screen. " +
                                    "The cost is adjusted every day, if the production value has been changed in the middle of the month, that part of the price will remain.",
                                0f)
                        }
                    }
                }
            }


        }
    }

    fun computeMonthlyCost(level: Float) : Float {
        return maxCost * level * level
    }
}