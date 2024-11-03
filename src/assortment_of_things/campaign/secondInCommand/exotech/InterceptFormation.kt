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

class InterceptFormation : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all ships and fighters in the fleet"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("+100 point-defense weapon range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("+20%% damage to missiles and fighters", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

        stats.nonBeamPDWeaponRangeBonus.modifyFlat(id, 100f)
        stats.beamPDWeaponRangeBonus.modifyFlat(id, 100f)

        stats.damageToFighters.modifyPercent(id, 20f)
        stats.damageToMissiles.modifyPercent(id, 20f)

    }

    override fun applyEffectsToFighterSpawnedByShip(data: SCData?, fighter: ShipAPI?, ship: ShipAPI?, id: String?) {

        var stats = fighter!!.mutableStats

        stats.nonBeamPDWeaponRangeBonus.modifyFlat(id, 100f)
        stats.beamPDWeaponRangeBonus.modifyFlat(id, 100f)

        stats.damageToFighters.modifyPercent(id, 20f)
        stats.damageToMissiles.modifyPercent(id, 20f)

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