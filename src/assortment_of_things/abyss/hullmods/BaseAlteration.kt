package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.campaign.CampaignUIAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.magiclib.kotlin.setAlpha
import java.awt.Color

abstract class BaseAlteration : BaseHullMod() {

    var color = Color(250, 187, 77)

    open fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) : Boolean {
        return true
    }

    open fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {

    }

    open fun canUninstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) : Boolean {
        return true;
    }

    open fun cannotUninstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {

    }

    open fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return true
    }

    override fun canBeAddedOrRemovedNow(ship: ShipAPI?, marketOrNull: MarketAPI?, mode: CampaignUIAPI.CoreUITradeMode?): Boolean {
        return false
    }

    override fun getBorderColor(): Color {
        return color.setAlpha(50)
    }

    override fun getNameColor(): Color {
        return color
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }

    override fun getDisplayCategoryIndex(): Int {
        return 3
    }

    override fun getDisplaySortOrder(): Int {
        return Int.MAX_VALUE
    }

    override fun showInRefitScreenModPickerFor(ship: ShipAPI?): Boolean {
        return false
    }
}