package assortment_of_things.frontiers

import assortment_of_things.frontiers.submarkets.SettlementStoragePlugin
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getStorageCargo

class SettlementManager(var settlement: SettlementData) : EveryFrameScript, EconomyTickListener {


    fun update() {

    }

    fun abandon() {

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

            slot.getPlugin()?.advance(amount)
        }

        for (modifier in settlement.modifiers) {
            modifier.advance(amount)
        }

        for (prod in ArrayList(settlement.productionOrders)) {
            var sinceTimestamp = Global.getSector().clock.getElapsedDaysSince(prod.timestamp)
            if (sinceTimestamp > prod.days) {

                var marketStorage = settlement.settlementEntity.market.submarketsCopy.find { it.plugin is SettlementStoragePlugin }!!.plugin.cargo
                marketStorage.addAll(prod.cargo)

                if (prod.cargo.mothballedShips == null) {
                    prod.cargo.initMothballedShips(Factions.PLAYER)
                }

                for (member in prod.cargo.mothballedShips.membersListCopy) {
                    marketStorage.mothballedShips.addFleetMember(member)
                }

                settlement.productionOrders.remove(prod)

                Global.getSector().campaignUI.addMessage(object : BaseIntelPlugin() {
                    override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {
                        info!!.addPara( "The settlement completed a custom production order.",
                            0f, Misc.getTextColor(), Misc.getHighlightColor(), "")
                    }

                    override fun getIcon(): String {
                        return settlement.intel.icon
                    }
                })
            }
        }
    }

    override fun reportEconomyTick(iterIndex: Int) {

        addStorageFees()

        for (mod in settlement.modifiers) {
            mod.reportEconomyTick(iterIndex)
        }

        for (slot in settlement.facilitySlots) {
            if (slot.isFunctional()) {
                slot.getPlugin()?.reportEconomyTick(iterIndex)
            }
        }

        val numIter = Global.getSettings().getFloat("economyIterPerMonth")
        val f = 1f / numIter

        val report = SharedData.getData().currentReport

        val fleetNode = report.getNode(MonthlyReport.FLEET)
        fleetNode.name = "Fleet"
        fleetNode.custom = MonthlyReport.FLEET
        fleetNode.tooltipCreator = report.monthlyReportTooltip

        val stipend = settlement.stats.income.modifiedValue * RATSettings.frontiersIncomeMult!!
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

    fun addStorageFees() {

        var primStorage = settlement.primaryPlanet.market?.getStorageCargo()
        if (primStorage == null && !settlement.hasFacility("stockpile")) {
            var sub = settlement.settlementEntity.market.submarketsCopy.find { it.plugin is SettlementStoragePlugin }!!.plugin as SettlementStoragePlugin
            val storageFraction = Global.getSettings().getFloat("storageFreeFraction")

            var vc = 0f
            for (stack in sub.cargo.stacksCopy) {
                vc += stack.size * stack.baseValuePerUnit
            }

            var vs = 0f
            for (member in sub.getCargo().getMothballedShips().getMembersListCopy()) {
                vs += member.baseValue
            }

            val fc: Float = (vc * storageFraction).toInt().toFloat()
            val fs: Float = (vs * storageFraction).toInt().toFloat()

            val report = SharedData.getData().currentReport

            var desc = ""
            desc = if (fc > 0 && fs > 0) {
                "ships & cargo"
            } else if (fc > 0) {
                "cargo"
            } else {
                "ships"
            }

            if (fc > 0 || fs > 0) {
                var storageNode = report.getNode(MonthlyReport.STORAGE)
                storageNode.name = "Storage"
                storageNode.custom = MonthlyReport.STORAGE
                storageNode.tooltipCreator = report.getMonthlyReportTooltip()

                var desc = ""
                desc = if (fc > 0 && fs > 0) {
                    "ships & cargo"
                } else if (fc > 0) {
                    "cargo"
                } else {
                    "ships"
                }

                var mNode = report.getNode(storageNode, settlement.settlementEntity.market.getId())
                mNode.name = settlement.settlementEntity.market.getName() + " (" + "$desc" + ")"
                mNode.custom = settlement.settlementEntity.market
                mNode.custom2 = MonthlyReport.STORAGE
                mNode.upkeep = fc + fs
            }
        }
    }

    override fun reportEconomyMonthEnd() {


        var storage = settlement.settlementEntity.market.submarketsCopy.find { it.plugin is SettlementStoragePlugin }?.plugin?.cargo ?: return
        var next = settlement.nextMonthsProduction

        for (mod in settlement.modifiers) {
            mod.reportEconomyMonthEnd()
        }

        for (slot in settlement.facilitySlots) {
            if (slot.isFunctional()) {
                var product = slot.getPlugin()?.addToMonthlyCargo(storage)
                if (product != null) {
                    next.addAll(product)
                }
                slot.getPlugin()?.reportEconomyMonthEnd()
            }
        }



        for (modifier in settlement.modifiers) {
            var product = modifier.addToMonthlyCargo(storage)
            if (product != null) {
                next.addAll(product)
            }
        }

        if (!next.isEmpty) {
            Global.getSector().campaignUI.addMessage(settlement.intel)
        }

        settlement.previousMonthsProduction.clear()
        settlement.previousMonthsProduction.addAll(next)
        storage.addAll(next)
        next.clear()

        settlement.currentProductionBudget += settlement.stats.productionBudgetPerMonth.modifiedValue
        settlement.currentProductionBudget = MathUtils.clamp(settlement.currentProductionBudget, 0f, settlement.stats.maxProductionBudget.modifiedValue)
    }


    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }


}