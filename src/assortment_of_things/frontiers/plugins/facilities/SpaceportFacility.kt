package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.data.SettlementData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SpaceportFacility : BaseSettlementFacility() {
    override fun apply(data: SettlementData) {

    }

    override fun getColor(): Color {
        return Misc.getBasePlayerColor()
    }

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI, data: SettlementData) {

    }
}