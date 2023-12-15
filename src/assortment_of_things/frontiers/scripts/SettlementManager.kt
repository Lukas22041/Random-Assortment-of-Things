package assortment_of_things.frontiers.scripts

import assortment_of_things.frontiers.data.SettlementData
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI

class SettlementManager(var settlement: SettlementData) : EveryFrameScript, EconomyTickListener {


    fun update() {

/*
        for (slot in settlement.facilitySlots) {
            if (slot.facilityID != "" && !slot.isBuilding) {
                slot.getFacilityPlugin()?.apply(settlement)
            }
        }*/


    }

    override fun advance(amount: Float) {
        var slots = settlement.facilitySlots
        for (slot in slots) {
            if (slot.facilityID == "") continue

            if (slot.isBuilding) {
                slot.updateDays()
                if (slot.daysRemaining <= 0.1) {
                    slot.finishConstruction()
                    update()
                }
            }

            slot.getFacilityPlugin()?.advance(amount)
        }

        for (modifier in settlement.modifiers) {
            modifier.advance(amount)
        }
    }

    override fun reportEconomyTick(iterIndex: Int) {
        val numIter = Global.getSettings().getFloat("economyIterPerMonth")
        val f = 1f / numIter

        val report = SharedData.getData().currentReport

        val fleetNode = report.getNode(MonthlyReport.FLEET)
        fleetNode.name = "Fleet"
        fleetNode.custom = MonthlyReport.FLEET
        fleetNode.tooltipCreator = report.monthlyReportTooltip

        val stipend = settlement.stats.income.modifiedValue
        val stipendNode = report.getNode(fleetNode, "rat_settlement")
        stipendNode.income += stipend * f

        if (stipendNode.name == null) {
            stipendNode.name = "Settlement"
            stipendNode.icon = Global.getSector().playerFaction.getCrest()
            stipendNode.tooltipCreator = object : TooltipMakerAPI.TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any): Float {
                    return 450f
                }

                override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                    tooltip.addPara("The monthly income from your settlement",
                        0f)
                }
            }
        }
    }

    override fun reportEconomyMonthEnd() {

    }


    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }


}