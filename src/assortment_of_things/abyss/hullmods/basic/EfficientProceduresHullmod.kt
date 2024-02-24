package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class EfficientProceduresHullmod : BaseAlteration() {


    var modID = "rat_efficient_procedures"

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)



        stats!!.suppliesPerMonth.modifyMult(modID, 0.666f);
        stats.fuelUseMod.modifyMult(modID, 0.666f);


    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {


        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Decreases the monthly supply and fuel usage by 33%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "supply", "fuel", "usage", "33%")

    }

}