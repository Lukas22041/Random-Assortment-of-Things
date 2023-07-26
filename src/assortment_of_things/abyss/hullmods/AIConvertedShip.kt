package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AIConvertedShip : BaseAlteration() {

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (!stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
            stats.variant.addTag("no_auto_penalty")
            stats.fleetMember.captain = null
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addPara("Replaces the internals of the ships bridge with systems for AI control, turning the ship in to an automated hull. \n\n" +
                "This specific conversion is able to negate the combat readiness penalty from the \"Automated\" Hullmod.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "automated hull", "negate the combat readiness penalty", "Automated")

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return !member!!.isCapital && (member!!.captain == null || member!!.captain.nameString == "") && !variant!!.hasHullMod(HullMods.AUTOMATED)
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?,  member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        if (member!!.isCapital) {
            tooltip!!.addPara("Can not be installed on capital ships.", 0f,
                Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
            tooltip.addSpacer(5f)
        }

        if (variant!!.hasHullMod(HullMods.AUTOMATED)) {
            tooltip!!.addPara("Can not be installed on automated ships.", 0f,
                Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
            tooltip.addSpacer(5f)
        }

        if (member!!.captain != null && member!!.captain.nameString != "") {
            tooltip!!.addPara("Can not be installed while an officer is assigned to the ship.", 0f,
                Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }

    override fun canUninstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return member!!.captain == null || member!!.captain.nameString == ""
    }

    override fun cannotUninstallAlterationTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?,width: Float) {
        tooltip!!.addPara("Cannot be uninstalled while an AI core is assigned to the ship.", 0f,
            Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }

    override fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {
        variant!!.removePermaMod(HullMods.AUTOMATED)
    }
}

