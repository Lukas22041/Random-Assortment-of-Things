package assortment_of_things.campaign.secondInCommand.exotech

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin

class SpaceSuperiority : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all carriers"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("+15%% faster fighter replacement rate", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("+20%% engagement range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

        stats.fighterRefitTimeMult.modifyMult(id, 0.85f)
        stats.fighterWingRange.modifyPercent(id, 20f)

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {


        
    }

    override fun advance(data: SCData, amunt: Float?) {

    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }

}