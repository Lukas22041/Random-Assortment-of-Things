package assortment_of_things.campaign.secondInCommand.abyssal

import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.skills.SeraphCoreSkill
import assortment_of_things.abyss.skills.SpaceCoreSkill
import assortment_of_things.abyss.skills.TimeCoreSkill
import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin
import second_in_command.specs.SCBaseSkillPlugin

class Interlinked : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all automated carriers"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("Carriers with Chronos, Cosmos or Seraph AI Cores apply the cores signature skill to all of their deployed fighters", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI, variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

        if (stats!!.isAutomated()) {

        }

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {

    }

    override fun applyEffectsToFighterSpawnedByShip(data: SCData, fighter: ShipAPI, ship: ShipAPI, id: String?) {
        if (Misc.isAutomated(ship)) {
            var core = ship!!.captain
            if (core == null || core.isDefault) return
            if (!core.isAICore) return

            var skill: RATBaseShipSkill? = null
            if (core.aiCoreId == RATItems.CHRONOS_CORE) skill = TimeCoreSkill()
            if (core.aiCoreId == RATItems.COSMOS_CORE) skill = SpaceCoreSkill()
            if (core.aiCoreId == RATItems.SERAPH_CORE) skill = SeraphCoreSkill()

            if (skill != null) {
                skill.apply(fighter.mutableStats, fighter.hullSize, id, 2f)
            }
        }
    }

    override fun advance(data: SCData, amunt: Float?) {

    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }

}