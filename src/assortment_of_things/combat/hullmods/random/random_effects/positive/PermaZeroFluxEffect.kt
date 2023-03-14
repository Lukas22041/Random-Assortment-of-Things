package assortment_of_things.combat.hullmods.random.random_effects.positive

import assortment_of_things.combat.hullmods.random.BaseRandomHullmodEffect
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class PermaZeroFluxEffect : BaseRandomHullmodEffect() {
    override fun getEffectId() = "zero_flux_effect"
    override fun getWeight() = 0.5f
    override fun getLimitPerSector() = 1

    override fun getFrigateCost() = 12
    override fun getDestroyerCost() = 14
    override fun getCruiserCost() = 20
    override fun getCapitalCost() = 30

    override fun getPossibleNames() = mapOf("Overdrive" to 1f)

    override fun getDescription(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip.addPara("Causes the zero flux boost to always be active.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "zero flux","always")
    }

    override fun applyEffectPreCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.getZeroFluxMinimumFluxLevel().modifyFlat(getEffectId(), 2f);
    }

    override fun applyEffectAfterCreation(ship: ShipAPI?, id: String?) {

    }

    override fun getIncompatible() = listOf(HullMods.SAFETYOVERRIDES)
}