package assortment_of_things.abyss.hullmods.shipsystem

import activators.ActivatorManager
import assortment_of_things.abyss.activators.EmergencySupportActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class EmergencySupportHullmod : BaseHullMod() {

    var modID = "rat_drone"

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
        ActivatorManager.addActivator(ship, EmergencySupportActivator(ship))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Installs a secondary shipsystem in to the hull that can be used independently of the original system.", 0f)
        tooltip!!.addSpacer(5f)

        tooltip.addSectionHeading("Shipsystem: Emergency Support", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Creates 3/4/6/8 small omni shielded support drones equipped with a LR PD Laser that position around the ship. After 20 seconds they are destroyed, requiring another 20 seconds to become ready again.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "3/4/6/8", "omni shielded support drones", "LR PD Laser", "20 seconds", "20 seconds")


    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }
    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }
}