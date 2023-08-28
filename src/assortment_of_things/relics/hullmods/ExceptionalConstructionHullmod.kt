package assortment_of_things.relics.hullmods

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ExceptionalConstructionHullmod : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.maxCombatReadiness.modifyFlat(id, 0.10f)
        stats!!.armorBonus.modifyMult(id, 1.05f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("The construction method used for this ship increases the maximum combat readiness by 10%% and its armor by 5%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "10%", "5%")

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

}