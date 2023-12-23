package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class RefineryFacility : BaseSettlementFacility() {

    override fun apply() {
        settlement.stats.income.modifyPercent("refinery", 75f, "Refinery")
    }

    override fun unapply() {
        settlement.stats.income.unmodify("refinery")
    }

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the settlements income by 75%%. Can only be build in settlements that are located on a hotspot of a refineable resource.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "75%")
    }

    override fun canBeBuild(): Boolean {
        var resource = FrontiersUtils.getRessource(settlement)
        return resource != null && resource.getSpec().canBeRefined
    }

    override fun canNotBeBuildReason(tooltip: TooltipMakerAPI, data: SettlementData) {
        tooltip.addNegativePara("This settlements resource can not be refined.")
    }
}