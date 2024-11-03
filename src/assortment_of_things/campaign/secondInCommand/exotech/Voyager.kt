package assortment_of_things.campaign.secondInCommand.exotech

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin
import second_in_command.specs.SCBaseSkillPlugin

class Voyager : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "fleet"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("-10%% fuel use", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("-20%% terrain movement penalty from all applicable terrain", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

        stats.fuelUseMod.modifyMult(id, 0.9f)

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {


        
    }

    override fun advance(data: SCData, amount: Float) {
        data.commander.stats.dynamic.getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat("rat_voyager", -0.2f)
    }

    override fun onActivation(data: SCData) {
        data.commander.stats.dynamic.getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat("rat_voyager", -0.2f)
    }

    override fun onDeactivation(data: SCData) {
        data.commander.stats.dynamic.getStat(Stats.NAVIGATION_PENALTY_MULT).unmodify("rat_voyager")
    }
}