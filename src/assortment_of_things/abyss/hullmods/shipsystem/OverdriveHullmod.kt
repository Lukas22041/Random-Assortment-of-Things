package assortment_of_things.abyss.hullmods.shipsystem

import activators.ActivatorManager
import assortment_of_things.abyss.activators.OverdriveActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class OverdriveHullmod : BaseHullMod() {

    var modID = "rat_overdrive"

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
        ActivatorManager.addActivator(ship, OverdriveActivator(ship))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP)
        {
            stats!!.maxCombatReadiness.modifyFlat(modID, -0.10f)
        }
        else
        {
            stats!!.maxCombatReadiness.modifyFlat(modID, -0.05f)
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Installs a secondary shipsystem in to the hull that can be used independently of the original system.", 0f)
        tooltip!!.addSpacer(5f)

        tooltip.addSectionHeading("Shipsystem: Overdrive", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("For 6 seconds, temporarily increase the ships max speed and both the ballistic and energy rate of fire, without increasing flux useage." +
                "\n\n" +
                "It has a cooldown of 20 seconds.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "6 seconds", "max speed", "ballistic", "energy", "rate of fire", "flux useage", "20 seconds")
        tooltip.addSpacer(10f)

        tooltip.addPara("Hull Alterations decrease the ships maximum CR by 5%%/5%%/5%%/10%% based on its hullsize.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

    }
}