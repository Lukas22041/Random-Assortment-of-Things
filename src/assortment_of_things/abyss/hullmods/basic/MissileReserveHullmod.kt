package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class MissileReserveHullmod : BaseAlteration() {

    var modID = "rat_missile_reserve"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.missileAmmoBonus.modifyFlat(modID, 2f)
        stats.missileAmmoRegenMult.modifyMult(modID, 1.25f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Implements a small storage compartment for missiles on the ship, enabling every missile weapon on the ship to hold 2 extra missiles. " +
                "Also enables missile weapons with rechargeable ammunition to do so 25%% faster.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "missiles", "2 extra missiles", "25%")

    }
}