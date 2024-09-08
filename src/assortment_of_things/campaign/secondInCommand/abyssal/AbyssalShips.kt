package assortment_of_things.campaign.secondInCommand.abyssal

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin

class AbyssalShips : SCBaseAutoPointsSkillPlugin() {
    override fun getProvidedPoints(): Int {
        return 120
    }


    override fun addTooltip(data: SCData, tooltip: TooltipMakerAPI) {

        tooltip.addPara("+100%% combat readiness degradation rate after peak performance time runs out", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        tooltip.addPara("-15%% non-missile weapon range", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        tooltip.addPara("+10%% top speed", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        super.addTooltip(data, tooltip)
    }

    override fun applyEffectsBeforeShipCreation(data: SCData?,stats: MutableShipStatsAPI?, variant: ShipVariantAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

        if (stats!!.isAutomated()) {
            stats!!.ballisticWeaponRangeBonus.modifyMult(id, 0.85f)
            stats.energyWeaponRangeBonus.modifyMult(id, 0.85f)

            stats.crLossPerSecondPercent.modifyMult(id, 2f)

            stats.maxSpeed.modifyPercent(id, 10f)

            super.applyEffectsBeforeShipCreation(data, stats, variant, hullSize, id)
        }
    }
}