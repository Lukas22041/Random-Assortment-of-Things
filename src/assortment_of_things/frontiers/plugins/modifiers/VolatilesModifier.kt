package assortment_of_things.frontiers.plugins.modifiers

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.frontiers.data.SettlementModifierSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

open class VolatilesModifier() : BaseSettlementModifier() {


    var eff = 0.9f

    override fun getDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("${getName()}:\n" +
                "${getName()} can be found within this location. Improves the fleets fuel efficiency by 10%% a refinery is build.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "${getName()}", "10%")
    }


    override fun advance(amount: Float) {
        if (settlement.hasFacility("refinery")) {
            Global.getSector().playerFleet.stats.addTemporaryModMult(
                0.1f, "settlement_volatiles", "Settlement", eff, Global.getSector().playerFleet.stats.fuelUseHyperMult)
        }
    }

}