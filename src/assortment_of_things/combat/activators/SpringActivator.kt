package assortment_of_things.combat.activators

import activators.CombatActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import java.awt.Color

class SpringActivator(ship: ShipAPI?) : CombatActivator(ship) {

    var color = Color(255, 0, 100)

    override fun getBaseActiveDuration(): Float {
       return  1.5f
    }

    override fun getBaseCooldownDuration(): Float {
        return  15f
    }

    override fun shouldActivateAI(p0: Float): Boolean {
        var activateFlags = listOf(AIFlags.PURSUING, AIFlags.MOVEMENT_DEST, AIFlags.RUN_QUICKLY)
        var flags = ship.aiFlags

        //Make it not use the system if its close to hostile ships
        var ships = CombatUtils.getShipsWithinRange(ship.location, 600f).filter { it.owner != ship.owner }
        if (ships.isNotEmpty()) return false

        for (flag in activateFlags)
        {
            if (flags.hasFlag(flag))
            {
                return true
            }
        }

        return false
    }

    override fun getBaseInDuration(): Float {
        return 2f
    }

    override fun getOutDuration(): Float {
        return 2f
    }

    override fun onActivate() {
        super.onActivate()



    }

    override fun advance(amount: Float) {

        var id = "rat_spring"

        if (state == State.IN)
        {
            ship.mutableStats.maxSpeed.modifyFlat(id, -1000f)
            ship.mutableStats.turnAcceleration.modifyMult(id, 5f)
            ship.mutableStats.maxTurnRate.modifyMult(id, 5f)
            ship.engineController.fadeToOtherColor(id, color, color, effectLevel, 1f)
        }
        if (state == State.ACTIVE)
        {
            ship.mutableStats.maxSpeed.unmodify(id)
            ship.mutableStats.turnAcceleration.unmodify(id)
            ship.mutableStats.maxTurnRate.unmodify(id)

            ship.mutableStats.maxSpeed.modifyFlat(id, 500f)
            ship.mutableStats.acceleration.modifyFlat(id, 1000f)
            ship.giveCommand(ShipCommand.ACCELERATE, null, 0)

           /* var force = 1000f / Global.getCombatEngine().timeMult.mult

            CombatUtils.applyForce(ship, ship.facing, force )*/

            ship.engineController.fadeToOtherColor(id, color, color, effectLevel, 1f)
            ship.engineController.extendFlame(id, 2f, 1f, 1f)
        }
        if (state == State.OUT )
        {
            ship.mutableStats.maxSpeed.unmodify(id)
            ship.mutableStats.acceleration.unmodify(id)
            ship.engineController.fadeToOtherColor(id, color, color, effectLevel, 1f)

        }
    }


    override fun getDisplayText(): String {
        return "Hyperspring"
    }

    override fun getHUDColor(): Color {
        return color
    }
}