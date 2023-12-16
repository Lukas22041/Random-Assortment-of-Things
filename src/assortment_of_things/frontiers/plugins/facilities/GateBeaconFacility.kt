package assortment_of_things.frontiers.plugins.facilities

import com.fs.starfarer.api.impl.campaign.GateEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class GateBeaconFacility : BaseSettlementFacility() {


    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("An incredibly powerful piece of machinery for its size. Emits a hyper-dimensional wave detectable by the gate network. " +
                "This allows warping towards this planet from a gate. " +
                "\n\nWhile not a new technology, this device barely saw any use in the domain-era, as it was highly disincentivized due to the threat of immediate jumps from hostile forces, and a lack of support for passthrough of more than a few fleets a day.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "warping towards this planet")
    }

    override fun shouldShowInPicker(): Boolean {
        return GateEntityPlugin.areGatesActive()
    }

    override fun apply() {
        GateEntityPlugin.getGateData().scanned.add(settlement.settlementEntity)
    }

    override fun unapply() {
        GateEntityPlugin.getGateData().scanned.remove(settlement.settlementEntity)
    }

}