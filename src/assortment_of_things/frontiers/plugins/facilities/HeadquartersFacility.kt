package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class HeadquartersFacility : BaseSettlementFacility() {

    override fun apply() {
        settlement.primaryPlanet.market?.stats?.dynamic?.getMod(Stats.MAX_INDUSTRIES)?.modifyFlat("rat_fac_headquarters", 1f, "Headquarters")
    }

    override fun unapply() {
        settlement.primaryPlanet.market?.stats?.dynamic?.getMod(Stats.MAX_INDUSTRIES)?.unmodify("rat_fac_headquarters")
    }

    override fun addDescriptionToTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Delegates much of the planetary management to a massive central building within this settlement. Enables the local colony to construct 1 more industry.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "1")
    }

    override fun canBeBuild(): Boolean {
        return settlement.primaryPlanet.faction.id == Factions.PLAYER
    }

    override fun canNotBeBuildReason(tooltip: TooltipMakerAPI, data: SettlementData) {
        tooltip.addNegativePara("Can only be build on a planet with a colony owned by your faction.")
    }
}