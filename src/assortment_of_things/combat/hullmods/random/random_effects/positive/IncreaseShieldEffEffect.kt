package assortment_of_things.combat.hullmods.random.random_effects.positive

import assortment_of_things.combat.hullmods.random.BaseRandomHullmodEffect
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class IncreaseShieldEffEffect : BaseRandomHullmodEffect() {

    override fun getEffectId() = "shield_eff_effect"
    override fun getWeight() = 1f
    override fun getLimitPerSector() = 10

    override fun getFrigateCost() = 4
    override fun getDestroyerCost() = 6
    override fun getCruiserCost() = 8
    override fun getCapitalCost() = 12

    override fun getPossibleNames() = mapOf("Shield" to 1f, "Efficiency" to 1f)

    override fun getDescription(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip.addPara("Increases the Shield Efficiency by 10%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Shield Efficiency","10%")
    }

    override fun applyEffectPreCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.shieldDamageTakenMult.modifyMult(getEffectId(), 0.9f);
    }

    override fun applyEffectAfterCreation(ship: ShipAPI?, id: String?) {

    }



}