package assortment_of_things.abyss.activators

import activators.CombatActivator
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import org.lazywizard.lazylib.combat.CombatUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class ParticleStreamActivator(ship: ShipAPI?) : CombatActivator(ship) {

    var color = Color(255, 0, 100)

    override fun getBaseActiveDuration(): Float {
       return  4f
    }

    override fun getBaseCooldownDuration(): Float {
        return  20f
    }

    override fun shouldActivateAI(p0: Float): Boolean {
        var activateFlags = listOf(AIFlags.PURSUING, AIFlags.MOVEMENT_DEST, AIFlags.RUN_QUICKLY)
        var flags = ship.aiFlags

        //Make it not use the system if its close to hostile ships
       /* var ships = CombatUtils.getShipsWithinRange(ship.location, 600f).filter { it.owner != ship.owner }
        if (ships.isNotEmpty()) return false*/

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
        return 1f
    }

    override fun getOutDuration(): Float {
        return 1f
    }

    override fun onActivate() {
        super.onActivate()



    }

    override fun advance(amount: Float) {

        var id = "rat_stream"

        ship.mutableStats.maxSpeed.modifyMult(id, 1.0f + (0.2f * effectLevel))
        ship.mutableStats.acceleration.modifyMult(id, 1.0f + (10f * effectLevel))
        ship.mutableStats.deceleration.modifyMult(id, 1.0f + (10f * effectLevel))
        ship.mutableStats.maxTurnRate.modifyMult(id, 1.0f + (10f * effectLevel))
        ship.mutableStats.turnAcceleration.modifyMult(id, 1.0f + (10f * effectLevel))

        ship.engineController.fadeToOtherColor(id, color, color.setAlpha(50), effectLevel, effectLevel)
       // ship.engineController.extendFlame(id, 0.5f * effectLevel, 0.5f * effectLevel, 0.5f * effectLevel)

    }


    override fun getDisplayText(): String {
        return "Particle Stream"
    }

    override fun getHUDColor(): Color {
        return color
    }
}