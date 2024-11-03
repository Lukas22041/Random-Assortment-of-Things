package assortment_of_things.campaign.secondInCommand.exotech

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.isAutomated
import org.magiclib.kotlin.setAlpha
import second_in_command.SCData
import second_in_command.misc.levelBetween
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin
import second_in_command.specs.SCBaseSkillPlugin
import java.awt.Color

class PhaseDrift : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all phase ships"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("Phase ships gain a temporary increase to their speed upon entering phase-space", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("   - This effect only applies to right-click phase systems", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "right-click")

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {



    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {

        if (!ship.hasListenerOfClass(PhaseDriftListener::class.java)) {
            ship.addListener(PhaseDriftListener(ship))
        }
        
    }

    override fun advance(data: SCData, amunt: Float?) {


    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }

}

class PhaseDriftListener(var ship: ShipAPI) : AdvanceableListener {

    var activated = false;

    var duration = -1f;
    var afterimageInterval = IntervalUtil(0.15f, 0.15f)

    override fun advance(amount: Float) {

        var cloak = ship.phaseCloak
        if (cloak != null && (cloak.specAPI.isPhaseCloak) || ship.baseOrModSpec().hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE)) {
            if (cloak.isActive && !activated) {
                activated = true
                duration = 1.5f;
            }

            var id = "rat_phase_drift"

            if (!cloak.isActive) {
                activated = false;
                duration -= 1f * amount; //Decrease boost faster after unphase
            }

            duration -= 0.33f * amount
            var level = duration.levelBetween(0f, 1.5f)
            ship.mutableStats.maxSpeed.modifyPercent(id, 20f * level)
            ship.mutableStats.maxSpeed.modifyFlat(id, 75f * level)
            ship.mutableStats.acceleration.modifyFlat(id, 1000f * level)
            ship.mutableStats.deceleration.modifyFlat(id, 1000f * level)
            ship.mutableStats.turnAcceleration.modifyFlat(id, 30f * level)
            ship.mutableStats.maxTurnRate.modifyFlat(id, 30f * level)

            if (duration > 0) {
                afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
                if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
                {
                    var alpha = (75 * level).toInt()

                    AfterImageRenderer.addAfterimage(ship!!, Color(130,4,189, alpha), Color(130,4,189, 0), 0.5f, 2f, Vector2f().plus(ship!!.location))
                }
            }


        }

    }

}