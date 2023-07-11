package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

abstract class BaseAlteration : BaseHullMod() {

    open fun alterationInstallFilter(fleet: List<FleetMemberAPI>) : List<FleetMemberAPI> {
        return fleet
    }

    open fun addItemPostDescription(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }
    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }
}