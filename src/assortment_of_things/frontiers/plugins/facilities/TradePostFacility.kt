package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class TradePostFacility : BaseSettlementFacility() {


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Opens a small trading center within the settlement. The settlers exchange commodities and ships & weapons that are known by your faction are exchanged here, with the catch being that their hulls tend to be in a rather poor state." +
                "\n\nThe market has a 20%% tariff. " +
                "\n\nIncreases settlement income by 20%%.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "commodities", "ships", "weapons", "tend to be in a rather poor state", "20%", "20%")
    }

    override fun advance(amount: Float) {

    }

    override fun apply() {
        settlement.settlementEntity.market.addSubmarket("rat_settlement_market")
        settlement.stats.income.modifyPercent("trade_post", 20f, "Trade Post")
    }

    override fun unapply() {
        settlement.settlementEntity.market.removeSubmarket("rat_settlement_market")
        settlement.stats.income.unmodify("trade_post")
    }
}