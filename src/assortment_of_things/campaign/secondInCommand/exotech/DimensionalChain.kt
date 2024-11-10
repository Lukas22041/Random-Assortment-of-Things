package assortment_of_things.campaign.secondInCommand.exotech

import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.combat.TemporalShellStats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.magiclib.subsystems.MagicSubsystem
import org.magiclib.subsystems.MagicSubsystemsManager
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin
import java.awt.Color

class DimensionalChain : SCBaseSkillPlugin() {
    override fun getAffectsString(): String {
        return "all fighters"
    }

    override fun addTooltip(data: SCData?, tooltip: TooltipMakerAPI) {

        tooltip.addPara("Fighters deployed from your fleet receive the \"Dimensional Chain\" subsystem", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        tooltip.addSpacer(10f)

        tooltip.addPara("Dimensional Chain", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("Upon activation, strike the target with an arc that deals 25 emp damage. \n" +
                "On impact, the target receives a debuff that decreases the ships timeflow by 3%% (max 15%%) and increases its damage taken by 5%% (max 25%%). These effects can be stacked. \n\n" +
                "The effectiveness of those debuffs is divided by the size of the fighter's wing. A wing with 5 fighters would only increase damage taken by 1%%, whereas a wing with just 1 fighter would apply the full 5%% per strike.\n\n" +
                "The debuff stays active for 7 seconds, and the subsystem has a cooldown of 15 seconds.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "25", "3%", "15%", "5%", "25%", "divided", "5", "1%", "1", "5%", "7", "15")

    }

    override fun applyEffectsToFighterSpawnedByShip(data: SCData?, fighter: ShipAPI, ship: ShipAPI?, id: String?) {

        MagicSubsystemsManager.addSubsystemToShip(fighter, DimensionalChainSubsystem(fighter))

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI,  variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {



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

class DimensionalChainSubsystem(ship: ShipAPI) : MagicSubsystem(ship) {

    var systemRange = 600f
    var duration = 7f

    var interval = IntervalUtil(0f, 2f)

    override fun getBaseInDuration(): Float {
        return 0f
    }

    override fun getBaseActiveDuration(): Float {
        return duration
    }

    override fun getBaseOutDuration(): Float {
        return 0f
    }

    override fun getBaseCooldownDuration(): Float {
        return 15f
    }

    override fun canActivate(): Boolean {

        var target = ship.shipTarget
        if (target != null && target.owner != ship.owner && target.isAlive) {
            if (MathUtils.getDistance(ship, target) <= systemRange) {

                return true
            }
        }

        return false

    }


    override fun advance(amount: Float, isPaused: Boolean) {
        super.advance(amount, isPaused)

    }

    override fun onFinished() {


    }

    override fun onActivate() {


        var target = ship.shipTarget
        if (target != null) {
            var listener = target.getListeners(DimensionalChainListener::class.java).firstOrNull()
            if (listener == null) {
                listener = DimensionalChainListener(target)
                target.addListener(listener)
            }


            var timeflowDecrease = 0.03f
            var damageIncrease = 0.05f
            var wing = ship.wing
            if (wing != null && wing.spec.numFighters != 0) {
                /*timeflowDecrease /= wing.wingMembers.size
                damageIncrease /= wing.wingMembers.size*/
                timeflowDecrease /= wing.spec.numFighters
                damageIncrease /= wing.spec.numFighters
            }

            listener.instances.add(DimensionalChainListener.ChainInstances(duration, timeflowDecrease, damageIncrease))

            Global.getCombatEngine().spawnEmpArcPierceShields(ship, ship.location, ship, target, DamageType.ENERGY, 0f, 25f,  // emp
                100000f,  // max range
                "tachyon_lance_emp_impact", 20f,  // thickness
                Color(130,4,189, 255), Color(130,4,189, 255))

        }

    }

    override fun shouldActivateAI(amount: Float): Boolean {

        var target = ship.shipTarget
        if (target != null && target.owner != ship.owner && target.isAlive) {
            if (MathUtils.getDistance(ship, target) <= systemRange) {

                interval.advance(amount)
                if (interval.intervalElapsed()) {
                    return true
                }


            }
        }

        return false
    }

    override fun getDisplayText(): String {
        if (!canActivate() && !isActive) return "Dimensional Chain (Requires Nearby Target)"
        return "Dimensional Chain"
    }

    override fun getHUDColor(): Color {
        return Color(130, 0, 200)
    }

}

//Applied to the target ship.
class DimensionalChainListener(var target: ShipAPI) : AdvanceableListener {

    data class ChainInstances(var duration: Float, var timeflowDecrease: Float, var damageIncrease: Float)

    var instances = ArrayList<ChainInstances>()

    override fun advance(amount: Float) {

        if (instances.isEmpty()) {
            return
        }

        var timeflowDecrease = 0f
        var damageIncrease = 0f

        for (instance in ArrayList(instances)) {
            instance.duration -= 1 * amount
            if (instance.duration <= 0) {
                instances.remove(instance)
                continue
            }

            timeflowDecrease += instance.timeflowDecrease
            damageIncrease += instance.damageIncrease
        }

        timeflowDecrease = MathUtils.clamp(timeflowDecrease, 0f, 0.15f)
        damageIncrease = MathUtils.clamp(damageIncrease, 0f, 0.25f)

        target.mutableStats.hullDamageTakenMult.modifyMult("rat_dimensional_chain", 1+damageIncrease)
        target.mutableStats.armorDamageTakenMult.modifyMult("rat_dimensional_chain", 1+damageIncrease)
        target.mutableStats.shieldDamageTakenMult.modifyMult("rat_dimensional_chain", 1+damageIncrease)

        target.mutableStats.timeMult.modifyMult("rat_dimensional_chain", 1-timeflowDecrease)


        var jitterLevel = damageIncrease.levelBetween(0f, 0.25f) * 0.5f

        target.setJitter(this, Color(130, 0, 200), jitterLevel, 3, 0f, 0 + 2f)
        target.setJitterUnder(this, Color(130, 0, 200), jitterLevel, 25, 0f, 7f + 14f)


    }
}