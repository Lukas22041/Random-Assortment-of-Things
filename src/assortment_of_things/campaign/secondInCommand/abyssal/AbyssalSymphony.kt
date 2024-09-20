package assortment_of_things.campaign.secondInCommand.abyssal

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.isAutomated
import second_in_command.SCData
import second_in_command.misc.levelBetween
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin
import second_in_command.specs.SCBaseSkillPlugin

class AbyssalSymphony : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all automated ships"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("Automated ships can recover their hitpoints over time, at the cost of their peak performance time", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("   - The regeneration happens at a rate of 2%% of the ships hitpoints per second", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "2%")
        tooltip.addPara("   - For every 1%% of recovered hull the peak performance time is reduced by 2/2/3/4 seconds based on hullsize", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1%", "2", "2", "3", "4")
        tooltip.addPara("   - The regeneration is stopped if the ship has no peak performance time remaining", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1%", "1")

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {
        if (ship!!.isAutomated()) {
            if (!ship.hasListenerOfClass(AbyssalSymphonyScript::class.java)) {
                ship.addListener(AbyssalSymphonyScript(ship))
            }
        }
    }
}

class AbyssalSymphonyScript(var ship: ShipAPI) : AdvanceableListener {

    var pptReduction = 0f
    var percentPerSecond = 0.02f
    var secondsCostPerPercent = 2

    init {
        secondsCostPerPercent = when(ship.hullSize) {
            ShipAPI.HullSize.FRIGATE -> 2
            ShipAPI.HullSize.DESTROYER -> 2
            ShipAPI.HullSize.CRUISER -> 3
            ShipAPI.HullSize.CAPITAL_SHIP -> 4
            else -> 2
        }
    }

    override fun advance(amount: Float) {
        var ppt = ship.peakTimeRemaining

        if (ppt > 0) {
            var max = ship.maxHitpoints
            var hp = ship.hitpoints

            if (hp < max) {
                var restorable = max * percentPerSecond * amount
                var toRestore = max - hp

                toRestore = toRestore.coerceIn(0f, restorable)
                ship.hitpoints += toRestore

                var level = toRestore.levelBetween(0f, restorable)
                var pptCost = (percentPerSecond * 100f * secondsCostPerPercent) * level * amount

                pptReduction += pptCost


            }
        }

        ship.mutableStats.peakCRDuration.modifyFlat("rat_abyssal_symphony", -pptReduction)

    }

}