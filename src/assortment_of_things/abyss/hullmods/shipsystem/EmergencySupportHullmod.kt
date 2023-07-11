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
        tooltip!!.addPara("Installs the \"Emergency Support\" system in to the hull. This exists in addition to the ships current system. When used, it deploys small shielded drones that are equipped with Vulcan cannons.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Particle Stream", "momentum")
        tooltip!!.addSpacer(5f)
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }
    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }
}