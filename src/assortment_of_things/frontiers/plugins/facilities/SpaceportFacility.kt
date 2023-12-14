package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.data.SettlementData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SpaceportFacility : BaseSettlementFacility() {
    override fun apply(data: SettlementData) {

    }

    override fun unapply(data: SettlementData) {

    }


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI, data: SettlementData) {
        tooltip.addPara("The landing pad is the heart of the settlement. Every day small dropships come by, landing with new supplies, and leaving with the settlements exports. " +
                "\n\n" +
                "The settlers tend to gather around this humble spaceport, be that for planning, business or leisure. Its state represents that of the settlement as a whole", 0f)
    }
}