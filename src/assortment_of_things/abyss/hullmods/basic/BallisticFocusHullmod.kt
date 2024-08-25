package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class BallisticFocusHullmod : BaseAlteration() {

    var modID = "rat_ballistic_focus"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.ballisticRoFMult.modifyMult(modID, 1.2f)
        stats!!.ballisticWeaponFluxCostMod.modifyMult(modID, 0.8f)
        stats!!.ballisticAmmoRegenMult.modifyMult(modID, 1.25f)
        stats!!.ballisticWeaponDamageMult.modifyMult(modID, 1.05f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Transfigures the hull's internal configuration into a state that benefits the standard operation of ballistic weapons.\n\n" +
                "" +
                "Increases rate-of-fire for ballistic weapons by 20%% without increasing flux usage. Additionally, ammunition recharges 25%% faster and deals 5%% more damage.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "ballistic weapons", "20%", "25%", "5%")

    }
}