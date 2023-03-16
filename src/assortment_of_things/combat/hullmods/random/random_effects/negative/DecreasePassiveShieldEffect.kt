package assortment_of_things.combat.hullmods.random.random_effects.negative

import assortment_of_things.combat.hullmods.random.BaseRandomHullmodEffect
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.glu.Sphere

class DecreasePassiveShieldEffect : BaseRandomHullmodEffect() {

    override fun getEffectId() = "shield_eff_effect"
    override fun getWeight() = 1f
    override fun getLimitPerSector() = 10
    override fun isNegative() = true

    override fun getFrigateCost() = 4
    override fun getDestroyerCost() = 6
    override fun getCruiserCost() = 8
    override fun getCapitalCost() = 12

    override fun getPossibleNames() = mapOf("Shield" to 1f, "Efficiency" to 1f)

    override fun getDescription(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip.addPara("Increases the Shield Upkeep Cost by 25%%.", 0f).apply {
            setHighlight("Shield Upkeep Cost", "25%")
            setHighlightColors(Misc.getHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }

    override fun applyEffectPreCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.shieldUpkeepMult.modifyMult(getEffectId(), 1.25f);
    }

    override fun applyEffectAfterCreation(ship: ShipAPI?, id: String?) {

    }



}