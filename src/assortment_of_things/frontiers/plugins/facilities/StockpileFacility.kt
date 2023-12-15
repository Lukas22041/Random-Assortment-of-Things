package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.data.SettlementData
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class StockpileFacility : BaseSettlementFacility() {

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Stores a small amount of several commodites, being made up of leftover materials that the that the settlement acquired over time. The procured materials are free to use. " +
                "Stockpiles at most 500 units worth of cargo per commodity.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "500")
    }

    override fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {
        return super.addToMonthlyCargo(current)
    }
}