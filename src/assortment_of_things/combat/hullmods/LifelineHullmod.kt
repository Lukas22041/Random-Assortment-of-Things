package assortment_of_things.combat.hullmods

import activators.ActivatorManager
import assortment_of_things.combat.activators.LifelineActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class LifelineHullmod : BaseHullMod() {


    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
        if (ship == null) return
        ActivatorManager.addActivator(ship, LifelineActivator(ship))
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Augments the ship with an additional shipsystem.", 0f)

        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("System: Lifeline", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Activating this system will link the ship to another ship within a distance of 1200 units. Once linked, 50%% of the shield damage received will instead be transfered to the linked ship as hardflux." +
                "\n\n" +
                "The ship closest to the mouse cursor will be linked with.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "link", "1200", "50%")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("System Stats", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Active: 10s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "10s")
        tooltip.addPara("Cooldown: 20s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "20s")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("AI", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("The AI will use this system when engaging an enemy while having already taken light flux damage.", 0f, Misc.getTextColor(), Misc.getHighlightColor())

    }
}