package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SoundAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class HypatiaShipsystem : BaseShipSystemScript() {

    var activated = false
    var enteredActive = false
    var ship: ShipAPI? = null

    var afterimageInterval = IntervalUtil(0.05f, 0.05f)
    var empInterval = IntervalUtil(0.1f, 0.2f)

    val color = Color(248,172,44, 255)
    var chargeSound: SoundAPI? = null
    var wasPlaying = false

    var key = "rat_hypatia_warp"

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)
        ship = stats!!.entity as ShipAPI? ?: return

        var stats = ship!!.mutableStats

        var fasterLevel = effectLevel * 1.5f
        fasterLevel = fasterLevel.coerceIn(0f, 1f)

        var system = ship!!.system

        //On Deactivation
        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false
            enteredActive = false
            chargeSound = null
            wasPlaying = false
        }

        //On Activation
        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            chargeSound = Global.getSoundPlayer().playSound("hypatia_warp_chargeup", 1.1f, 0.8f, ship!!.location, Vector2f())
        }

        if (chargeSound?.isPlaying == true) {
            wasPlaying = true
        }

        //Once Chargeup is done
        if (!enteredActive && wasPlaying && chargeSound?.isPlaying == false && state == ShipSystemStatsScript.State.ACTIVE && !Global.getCombatEngine().isPaused) {
            enteredActive = true

           // chargeSound!!.stop()
            Global.getSoundPlayer().playSound("exoship_warp", 1.1f, 0.9f, ship!!.location, Vector2f())
        }

        if (enteredActive) {
            if (ship!!.shield?.isOn == true) {
                ship!!.shield?.toggleOff()
            }
        }

        if (system.isActive) {

            if (chargeSound != null && chargeSound!!.isPlaying) {
                chargeSound!!.setLocation(ship!!.location.x, ship!!.location.y)
            }

            ship!!.giveCommand(ShipCommand.ACCELERATE, null, 0)

            ship!!.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
            ship!!.blockCommandForOneFrame(ShipCommand.DECELERATE)

            ship!!.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
            ship!!.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)



            var intensity = 1f
            ship!!.setJitterUnder(this, color, intensity * (effectLevel -0.1f), 10, 2f, 2f)

            //Reduce afterimage count after warp, and increase duration
            var frequency = 1f
            var durationMod = 1f
            if (enteredActive) {
                frequency = 0.5f
                durationMod = 3f
            }

            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame * frequency)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                var alpha = (75 * effectLevel).toInt()

                //AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(alpha), color.setAlpha(alpha), 1.5f * effectLevel, 2f, Vector2f().plus(ship!!.location))
                AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(alpha), Color(130,4,189, 0), 0.5f + (1f * effectLevel) * durationMod, 2f, Vector2f().plus(ship!!.location))
            }

            if (effectLevel >= 0.1f) {
                empInterval.advance(Global.getCombatEngine().elapsedInLastFrame * effectLevel * frequency)
                if (empInterval.intervalElapsed() && !Global.getCombatEngine().isPaused) {
                    ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                    var from = Vector2f(ship!!.exactBounds.segments.random().p1)

                    var angle = Misc.getAngleInDegrees(ship!!.location, from)
                    //var to = MathUtils.getPointOnCircumference(ship!!.location, MathUtils.getRandomNumberInRange(20f, 50f) + ship!!.collisionRadius, angle + MathUtils.getRandomNumberInRange(-30f, 30f))

                    var to = Vector2f(ship!!.exactBounds.segments.random().p1)

                    var empColor = Misc.interpolateColor(color, Color(130,4,189, 255), Random().nextFloat())
                    Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, empColor.setAlpha(75), empColor.setAlpha(75))
                }

            }


            ship!!.engineController.extendFlame(this, 1.5f * effectLevel, 0.5f * effectLevel, 0.2f * effectLevel)
        }

        if (system.state == ShipSystemAPI.SystemState.IN) {



            stats.maxSpeed.modifyMult(key, 1 + 0.5f * effectLevel)
         /*   stats.acceleration.modifyMult(key, 1 - 0.5f * effectLevel)
            stats.deceleration.modifyMult(key, 1 - 0.5f * effectLevel)*/
            stats.maxTurnRate.modifyMult(key, 1 - 0.80f * fasterLevel)
            stats.turnAcceleration.modifyMult(key, 1 - 0.80f * fasterLevel)
        }


    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        return system!!.state != ShipSystemAPI.SystemState.IN
    }
}