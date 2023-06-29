package assortment_of_things.abyss.hullmods.shipsystem

import activators.ActivatorManager
import assortment_of_things.abyss.activators.ParticleStreamActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ParticleStreamHullmod : BaseHullMod() {

    var modID = "rat_stream"

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
        ActivatorManager.addActivator(ship, ParticleStreamActivator(ship))
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

        tooltip.addSectionHeading("Shipsystem: Particle Stream", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Activating this system allows the ship to almost immediately change its velocity in to any other direction." +
                "\n\n" +
                "It has a cooldown of 10 seconds.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "velocity", "direction", "cooldown", "10")
        tooltip.addSpacer(10f)

        tooltip.addPara("Hull Alterations decrease the ships maximum CR by 5%%/5%%/5%%/10%% based on its hullsize.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

    }
}