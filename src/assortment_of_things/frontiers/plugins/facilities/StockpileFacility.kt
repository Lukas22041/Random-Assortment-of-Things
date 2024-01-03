package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.impl.campaign.shared.SharedData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.ArrayList

class StockpileFacility : BaseSettlementFacility() {

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("A dedicated storage facility for the settlement. Removes the monthly fees from the settlements storage. " +
                "If the settlement is located on a planet owned by another faction, it removes the storage fees from the market aswell. " +
                "\n\n" +
                "Stores some supplies, fuel and heavy machinery every month. Those are free to take, but not more than 1000 of each can be stored.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "monthly fees", "supplies", "fuel", "heavy machinery", "1000")
    }

    override fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {
        var max = 1000f
        var new = Global.getFactory().createCargo(true)

        val dropRandom: MutableList<SalvageEntityGenDataSpec.DropData> = ArrayList()
        val dropValue: MutableList<SalvageEntityGenDataSpec.DropData> = ArrayList()

        var d = SalvageEntityGenDataSpec.DropData()

        d = SalvageEntityGenDataSpec.DropData()
        d.group = "rat_settlement_stockpile"
        d.value = 20000
        dropValue.add(d)

        val result = SalvageEntity.generateSalvage(Random(), 1f, 1f, 1f, 1f, dropValue, dropRandom)

        for (stack in result.stacksCopy) {
            var inCargo = current.getCommodityQuantity(stack.commodityId) ?: continue
            var availableSpace = max - inCargo
            availableSpace = MathUtils.clamp(availableSpace, 0f, max)
            var toAdd = MathUtils.clamp(stack.size, 0f, availableSpace)
            toAdd = MathUtils.clamp(toAdd, 0f, max)
            new.addCommodity(stack.commodityId, toAdd)
        }

        return new
    }

    override fun apply() {
        //settlement.stats.income.modifyPercent("stockpile", 10f, "Stockpile")
    }

    override fun unapply() {
        //settlement.stats.income.unmodify("stockpile")
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