package assortment_of_things.abyss.hullmods

import assortment_of_things.misc.ReflectionUtils
import com.fs.graphics.Sprite
import com.fs.starfarer.BaseGameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.loading.ResourceLoaderState
import com.fs.state.AppDriver

class AIConvertedShip : BaseAlteration() {

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (!stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
            stats.variant.addTag("no_auto_penalty")
            stats.fleetMember.captain = null
        }

        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
            stats.suppliesPerMonth.modifyMult(id, 1.5f)
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        var label =tooltip!!.addPara("Replaces the internals of the ships bridge with systems for AI control, turning the ship in to an automated hull. \n\n" +
                "This specific conversion is able to negate the combat readiness penalty from the \"Automated\" Hullmod.\n\n" +
                "If installed in to a capital hull, it will increase the maintenance cost by 50%%", 0f)

        var h = Misc.getHighlightColor()

        label.setHighlight("automated hull", "negate the combat readiness penalty", "Automated", "maintenance cost", "50%")
        label.setHighlightColors(h, h, h, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return (member!!.captain == null || member!!.captain.nameString == "") && !variant!!.hasHullMod(HullMods.AUTOMATED)
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?,  member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {

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

