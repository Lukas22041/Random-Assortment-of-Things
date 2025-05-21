package assortment_of_things.campaign.secondInCommand.abyssal

import assortment_of_things.misc.addPara
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
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
        tooltip.addPara("   - For every 1%% of recovered hull the peak performance time is reduced by 3/4/5/6 seconds based on hullsize*", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1%", "3", "4", "5", "6")
        tooltip.addPara("   - The regeneration is stopped if the ship has no peak performance time remaining", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1%", "1")

        tooltip.addSpacer(10f)
        tooltip.addPara("*Instead consumes 0.5%% combat readiness on ships with infinite peak performance time. Ships below 40%% CR will stop regenerating.", 0f,
            Misc.getGrayColor(), Misc.getHighlightColor(), "0.5%", "40%")

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
    var crCostPerPercentIfInfinite = 0.005f

    init {
        secondsCostPerPercent = when(ship.hullSize) {
            ShipAPI.HullSize.FRIGATE -> 3
            ShipAPI.HullSize.DESTROYER -> 4
            ShipAPI.HullSize.CRUISER -> 5
            ShipAPI.HullSize.CAPITAL_SHIP -> 6
            else -> 2
        }
    }

    override fun advance(amount: Float) {
        if (!ship.isAlive) return

        var ppt = ship.peakTimeRemaining

        if (ppt > 0) {
            var max = ship.maxHitpoints
            var hp = ship.hitpoints

            if (hp < max) {
                var restorable = max * percentPerSecond * amount
                var toRestore = max - hp

                toRestore = toRestore.coerceIn(0f, restorable)

                var level = toRestore.levelBetween(0f, restorable)

                if (ship.hullSpec.noCRLossTime <= 10000f) {
                    var pptCost = (percentPerSecond * 100f * secondsCostPerPercent) * level * amount
                    pptReduction += pptCost
                    ship.hitpoints += toRestore
                } else if (ship.currentCR >= 0.4){
                    ship.currentCR -= (percentPerSecond * 100f * crCostPerPercentIfInfinite) * level * amount
                    ship.currentCR = MathUtils.clamp(ship.currentCR, 0f, 1f)
                    ship.hitpoints += toRestore
                }
            }
        }

        ship.mutableStats.peakCRDuration.modifyFlat("rat_abyssal_symphony", -pptReduction)

    }

}