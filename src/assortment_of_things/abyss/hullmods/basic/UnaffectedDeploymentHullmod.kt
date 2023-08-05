package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.abyss.hullmods.HullmodUtils
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class UnaffectedDeploymentHullmod : BaseAlteration() {

    var modID = "rat_unaffected_deployment"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)
        HullmodUtils.negateStatDecrease("rat_negate_cr", stats!!.peakCRDuration, stats)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        HullmodUtils.negateStatDecrease("rat_negate_cr", ship!!.mutableStats.peakCRDuration, ship.mutableStats)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        HullmodUtils.negateStatDecrease("rat_negate_cr", ship!!.mutableStats.peakCRDuration, ship.mutableStats)
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Prevents any other sources (i.e hullmods, skills) from reducing the ships peak performance time. This negation may not be shown correctly in the ships statcard.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "Prevents", "reducing", "peak performance time",
            )

    }
}