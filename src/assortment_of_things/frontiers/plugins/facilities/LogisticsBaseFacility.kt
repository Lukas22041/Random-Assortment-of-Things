package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class LogisticsBaseFacility : BaseSettlementFacility() {


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Manages a small logistics fleet that assists the main fleet in its operations. Increases the cargo and fuel capacity of your fleet by 25%%.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "cargo", "fuel", "25%")
    }

    override fun advance(amount: Float) {

    }

    override fun unapply() {

    }
}