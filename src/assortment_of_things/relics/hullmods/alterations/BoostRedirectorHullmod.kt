package assortment_of_things.relics.hullmods.alterations

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class BoostRedirectorHullmod : BaseAlteration() {

    var modID = "rat_boost_redirector"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.zeroFluxSpeedBoost.modifyMult(id, 0.2f)
    }



    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        if (ship.fluxTracker.isEngineBoostActive) {
            ship.mutableStats.ballisticAmmoRegenMult.modifyMult(modID, 2f)
            ship.mutableStats.energyAmmoRegenMult.modifyMult(modID, 2f)
        }
        else {
            ship.mutableStats.ballisticAmmoRegenMult.unmodify(modID)
            ship.mutableStats.energyAmmoRegenMult.unmodify(modID)
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("The zero flux engine boost is reduced to only 20%% of its original value. The rest of the energy is redirected to increase the ships ballistic and energy ammunition recharge by 100%%", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "zero flux engine boost", "20%", "100%")

    }
}