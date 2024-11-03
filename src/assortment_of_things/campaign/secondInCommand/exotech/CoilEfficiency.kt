package assortment_of_things.campaign.secondInCommand.exotech

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.hullmods.AdaptivePhaseCoils
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin

class CoilEfficiency : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all phase ships"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("Phase ships gain 25%% increased soft-flux dissipation while phased", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("+20%% required hard-flux to reach the minimum speed due to phase coil stress", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

        stats.dynamic.getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD)
            .modifyPercent(id, 20f)

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {


        
    }

    override fun advance(data: SCData, amunt: Float?) {

    }

    override fun advanceInCombat(data: SCData?, ship: ShipAPI?, amount: Float?) {
        if (ship!!.isPhased) {
            ship.mutableStats.fluxDissipation.modifyMult("rat_coil_efficiency", 1.25f)
        } else {
            ship.mutableStats.fluxDissipation.unmodify("rat_coil_efficiency")
        }
    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }

}