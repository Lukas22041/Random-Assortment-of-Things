package assortment_of_things.combat.hullmods

import activators.ActivatorManager
import assortment_of_things.combat.activators.LifelineActivator
import assortment_of_things.combat.activators.SpringActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SpringHullmod : BaseHullMod() {


    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
        ActivatorManager.addActivator(ship, SpringActivator(ship))
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Augments the ship with an additional shipsystem.", 0f)

        tooltip.addSpacer(5f)
        tooltip.addSectionHeading("System: Hyper-Spring", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Activating this system will slow the ship to a halt, but strongly boost its turn speed. After 2 seconds, it rapidly propells the ship forward." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "slow", "turn speed", "2 seconds")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("System Stats", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Cooldown: 15s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "15s")
        tooltip.addSpacer(5f)

        tooltip.addSectionHeading("AI", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("The AI will use the system to pursue targets or move towards destinations, but it will not use it if enemy targets are close by.", 0f, Misc.getTextColor(), Misc.getHighlightColor())

    }
}