package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class EnergyFocusHullmod : BaseAlteration() {

    var modID = "rat_ballistic_focus"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.energyRoFMult.modifyMult(modID, 1.2f)
        stats!!.energyWeaponFluxCostMod.modifyMult(modID, 0.8f)
        stats!!.energyAmmoRegenMult.modifyMult(modID, 1.25f)
        stats!!.energyWeaponDamageMult.modifyMult(modID, 1.05f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Transfigures the hulls internal configuration in to a state that benefits the standard operation of energy weapons.\n\n" +
                "" +
                "Increases the rate of fire by 20%% without increasing flux usage, ammunition recharges 25%% faster and all energy weapons deal 5%% more damage.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "energy weapons", "20%", "25%", "5%")

    }
}