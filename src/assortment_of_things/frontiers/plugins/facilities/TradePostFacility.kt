package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class TradePostFacility : BaseSettlementFacility() {


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "")
    }

    override fun advance(amount: Float) {

    }

    override fun apply() {
        settlement.settlementEntity.market.addSubmarket("rat_settlement_market")
    }

    override fun unapply() {
        settlement.settlementEntity.market.removeSubmarket("rat_settlement_market")
    }
}