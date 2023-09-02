package assortment_of_things.relics.hullmods.alterations

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SoftShieldsHullmod : BaseAlteration() {

    var modID = "rat_soft_shields"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.shieldSoftFluxConversion.modifyFlat(modID, 0.2f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("20%% of shield damage taken is converted to soft-flux", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "20%", "soft-flux")

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?,  marketAPI: MarketAPI?): Boolean {
        return variant!!.hullSpec.shieldType != ShieldAPI.ShieldType.NONE && variant.hullSpec.shieldType != ShieldAPI.ShieldType.PHASE
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?,variant: ShipVariantAPI?,width: Float) {
        tooltip!!.addNegativePara("Can only be installed on ships that have a shield by default.")
    }
}