package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.ui.TooltipMakerAPI

class SpaceportFacility : BaseSettlementFacility() {

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The landing pad is the heart of the settlement. Every day small dropships come by, landing with new supplies, and leaving with the settlements exports. " +
                "\n\n" +
                "The settlers tend to gather around this humble spaceport, be that for planning, business or leisure. Its state represents that of the settlement as a whole", 0f)
    }

    override fun apply() {
        settlement.stats.income.modifyFlat("landing_pad", 5000f, "Landing Pad")

    }

    override fun unapply() {
        settlement.stats.income.unmodifyFlat("landing_pad")
    }
}