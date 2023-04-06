package assortment_of_things.combat.hullmods

import activators.ActivatorManager
import assortment_of_things.combat.activators.PhaseDiveActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class PhaseDiveHullmod : BaseHullMod() {


    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
        ActivatorManager.addActivator(ship, PhaseDiveActivator(ship))
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Augments the ship with an additional shipsystem.", 0f)

        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("System: Phase Dive", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Dives the ship in to p-space. This is done without the usual safeties of stable phase-coils, allowing the ship to ignore the usual phase restrictions, but at the same time rapidly decreasing the ships Peak Performance while dived." +
                "\n\n" +
                "The dive can be ended early by pressing the activation key again." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "p-space", "phase-coils", "5 seconds")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("System Stats", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Active: 5s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "5s")
        tooltip.addPara("Active (Realtime): 1s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1s")
        tooltip.addPara("Cooldown: 15s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "15s")
        tooltip.addSpacer(2f)
        tooltip.addPara("PPT Loss per Full Dive: 15s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "15s")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("AI", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("The AI will use this system to pursue targets, maneuver around them or to back off from engagements.\n\nThe AI may do poorly as it does not consider the PPT loss in its decisions.", 0f, Misc.getTextColor(), Misc.getHighlightColor())

    }
}