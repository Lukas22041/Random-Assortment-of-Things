package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class TimegearHullmod : BaseAlteration() {


    var modID = "rat_timegear"

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        super.advanceInCombat(ship, amount)
        if (ship == null) return

        if (ship == Global.getCombatEngine().playerShip)
        {
            Global.getCombatEngine().timeMult.modifyMult(modID, 0.85f)
        }
        else
        {
            Global.getCombatEngine().timeMult.unmodify(modID)
        }

    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.timeMult.modifyMult(modID, 1.15f);
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Installs experimental components in to the ships generators. When they are active, they increases the ships perceived timeflow by 15%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "timeflow", "15%")

    }
}