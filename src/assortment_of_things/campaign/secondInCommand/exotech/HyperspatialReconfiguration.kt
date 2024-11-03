package assortment_of_things.campaign.secondInCommand.exotech

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.combat.TemporalShellStats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin

class HyperspatialReconfiguration : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all ships in the fleet"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("+5%% timeflow", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {



    }

    override fun advance(data: SCData, amunt: Float?) {

    }

    override fun advanceInCombat(data: SCData?, ship: ShipAPI?, amount: Float?) {

        if (Global.getCombatEngine() == null) return

        var id = "rat_hyperspatial_reconfiguration_" + ship!!.id

        val shipTimeMult = 1f + (0.05f)
        ship!!.mutableStats.timeMult.modifyMult(id, shipTimeMult)
        if (Global.getCombatEngine().playerShip == ship) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }
    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }

}