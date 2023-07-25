package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

abstract class BaseAlteration : BaseHullMod() {

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
        return false
    }


    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }
}