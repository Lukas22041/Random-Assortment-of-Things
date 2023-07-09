package assortment_of_things.abyss.hullmods

import activators.ActivatorManager
import assortment_of_things.abyss.activators.ParticleStreamActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class EfficientProceduresHullmod : BaseHullMod() {


    var modID = "rat_efficient_procedures"

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return
        ActivatorManager.addActivator(ship, ParticleStreamActivator(ship))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)



        stats!!.suppliesPerMonth.modifyMult(modID, 0.75f);
        stats.fuelUseMod.modifyMult(modID, 0.75f);


    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {



        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Decreases the monthly supply and hyperspace fuel useage by 25%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "supply", "fuel", "useage", "25%")


    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }
    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }
}