package assortment_of_things.campaign.secondInCommand.abyssal

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin
import second_in_command.specs.SCBaseSkillPlugin

class EquivalentExchange : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all automated ships"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("The contributed automated points from all automated ships are halved", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("+20%% deployment points cost", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI, variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

        if (stats!!.isAutomated()) {
            stats.dynamic.getStat("sc_auto_points_mult").modifyMult("sc_equivelant_exchange", 0.5f)
            stats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(id, 20f)
        }

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {

    }

    override fun advance(data: SCData, amunt: Float?) {
        for (member in data.fleet.fleetData.membersListCopy) {
            member.stats.dynamic.getStat("sc_auto_points_mult").modifyMult("sc_equivelant_exchange", 0.5f)
        }
    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {
        for (member in data.fleet.fleetData.membersListCopy) {
            member.stats.dynamic.getStat("sc_auto_points_mult").unmodify("sc_equivelant_exchange")
        }
    }

}