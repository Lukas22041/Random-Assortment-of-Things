package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SoundAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.combat.canUseSystemThisFrame
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class SupernovaShipsystem : BaseShipSystemScript() {

    var activated = false
    var enteredActive = false
    var ship: ShipAPI? = null

    var afterimageInterval = IntervalUtil(0.035f, 0.035f)
    var empInterval = IntervalUtil(0.1f, 0.2f)

    val color = Color(248,172,44, 255)
    var chargeSound: SoundAPI? = null

    var triggeredOut = false

    var SHIP_ALPHA_MULT = 0.25f
    var ENGINE_COLOR = Color(255, 177, 127, 200)

    var key = "rat_hypatia_warp"

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)
        ship = stats!!.entity as ShipAPI? ?: return
        var id = id + "_" + ship!!.getId()

        var stats = ship!!.mutableStats

        var fasterLevel = effectLevel * 1.5f
        fasterLevel = fasterLevel.coerceIn(0f, 1f)

        var system = ship!!.system

        //On Deactivation
        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false
            enteredActive = false
            chargeSound = null
          /*  wasPlaying = false*/
            triggeredOut = false

            stats.maxSpeed.unmodify(key)
            stats.acceleration.unmodify(key)
            stats.deceleration.unmodify(key)
            stats.maxTurnRate.unmodify(key)
            stats.turnAcceleration.unmodify(key)
        }

        //On Activation
        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            chargeSound = Global.getSoundPlayer().playSound("hypatia_warp_chargeup", 1.1f, 0.8f, ship!!.location, Vector2f())
        }

       /* if (chargeSound?.isPlaying == true) {
            wasPlaying = true
        }*/

        //Once Chargeup is done
        if (!enteredActive && /*wasPlaying && chargeSound?.isPlaying == false && */state == ShipSystemStatsScript.State.ACTIVE && !Global.getCombatEngine().isPaused) {
            enteredActive = true

            if (chargeSound?.isPlaying == true) {
                chargeSound?.stop()
            }

           // chargeSound!!.stop()
            Global.getSoundPlayer().playSound("exoship_warp", 1.1f, 0.9f, ship!!.location, Vector2f())

            var flash = HypatiaFlashRenderer(Vector2f(ship!!.location), ExoUtils.color1, Color(130,4,189, 255),
                400f, 1600f, 0.0f, 0.25f, 1f)
            Global.getCombatEngine().addLayeredRenderingPlugin(flash)

            ship!!.isPhased = true

        }



        if (system.isActive) {

            if (chargeSound != null && chargeSound!!.isPlaying) {
                chargeSound!!.setLocation(ship!!.location.x, ship!!.location.y)
            }

            //Stop forced forwards thrust slightly before ability ends
            if (state != ShipSystemStatsScript.State.OUT || effectLevel >= 0.25f) {
                ship!!.giveCommand(ShipCommand.ACCELERATE, null, 0)

                ship!!.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
                ship!!.blockCommandForOneFrame(ShipCommand.DECELERATE)
            }

            ship!!.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
            ship!!.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)


            var intensity = 1f

            if (!enteredActive) {
                ship!!.setJitterUnder(this, color, intensity * (effectLevel -0.1f), 10, 2f, 2f)
            }

            //Reduce afterimage count after warp, and increase duration
            var frequency = 1f
            var empFrequency = 1f
            var durationMod = 1f
            if (enteredActive) {
                frequency *= 3f
                empFrequency *= 1.5f
                durationMod *= 0.3f
            }

            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame * frequency)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                var alpha = (75 * effectLevel).toInt()
              //  if (enteredActive) alpha -= 50

                //AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(alpha), color.setAlpha(alpha), 1.5f * effectLevel, 2f, Vector2f().plus(ship!!.location))
                AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(alpha), Color(130,4,189, 0), 0.5f + (1f * effectLevel) * durationMod, 2f, Vector2f().plus(ship!!.location))
            }

            if (effectLevel >= 0.1f) {
                empInterval.advance(Global.getCombatEngine().elapsedInLastFrame * effectLevel * empFrequency)
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


            //stats.maxSpeed.modifyFlat(key, 60 * effectLevel)
            stats.maxSpeed.modifyMult(key, 1 - 0.6f * effectLevel)
         /*   stats.acceleration.modifyMult(key, 1 - 0.5f * effectLevel)
            stats.deceleration.modifyMult(key, 1 - 0.5f * effectLevel)*/
            stats.maxTurnRate.modifyMult(key, 1 - 0.50f * fasterLevel)
            stats.turnAcceleration.modifyMult(key, 1 - 0.50f * fasterLevel)
        }


        if (!Global.getCombatEngine().isPaused && system.isActive) {
            var nearbyShipsIterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 1200f, 1200f)
            var nearbyShips = ArrayList<ShipAPI>()
            nearbyShipsIterator.forEach { nearbyShips.add(it as ShipAPI) }

            //Push away ships that are to close
            for (other in nearbyShips) {
                if (ship == other) continue
                if (ship!!.baseOrModSpec().hullId != other.baseOrModSpec().hullId) continue
                //if (!other.system.isActive) continue

                var angle = Misc.getAngleInDegrees(ship!!.location, other.location)
                var distance = MathUtils.getDistance(ship!!, other)
                var level = distance.levelBetween(ship!!.collisionRadius+other.collisionRadius, (ship!!.collisionRadius+other.collisionRadius) * 2)
                level = 1-level
                level *= level * level

                var force = 1.25f
                if (state == ShipSystemStatsScript.State.OUT) {
                    force *= 1.25f
                }

                CombatUtils.applyForce(other, angle, force * level)
            }
        }




        if (enteredActive) {
            if (ship!!.shield?.isOn == true && effectLevel >= 0.5f) {
                ship!!.shield?.toggleOff()
            }


            ship!!.extraAlphaMult = 1f - (1f - SHIP_ALPHA_MULT) * effectLevel
            ship!!.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small

            ship!!.engineController.fadeToOtherColor(this, ExophaseShipsystem.ENGINE_COLOR,ExophaseShipsystem.ENGINE_COLOR, 1f * effectLevel, 1f)
            //ship!!.engineController.extendFlame(this, -0.1f * effectLevel, -0.1f * effectLevel, 0f)

            var player = ship == Global.getCombatEngine().getPlayerShip();
            val shipTimeMult = 1f + (5 * effectLevel)
            stats.timeMult.modifyMult(id, shipTimeMult)
            if (player) {
                Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
            } else {
                Global.getCombatEngine().timeMult.unmodify(id)
            }

            if (!Global.getCombatEngine().isPaused && state == ShipSystemStatsScript.State.ACTIVE) {
                CombatUtils.applyForce(ship!!, ship!!.facing, 5000f)
            }


            stats.maxSpeed.unmodifyMult(key)
            stats.maxSpeed.modifyFlat(key, 350 * effectLevel) //Dont make this to high, because anything thats over 600 will make it exit the ability a lot faster & for longer
            stats.acceleration.modifyMult(key, 1 + 30f * effectLevel)
            //stats.deceleration.modifyMult(key, 1 + 0.5f * effectLevel)
            stats.maxTurnRate.modifyMult(key, 1 - 0.50f * effectLevel)
            stats.turnAcceleration.modifyMult(key, 1 - 0.50f * effectLevel)
        }

        if (system.state == ShipSystemAPI.SystemState.OUT) {

            if (effectLevel <= 0.5f) {
                ship!!.isPhased = false
            }

            if (enteredActive && !triggeredOut) {
                triggeredOut = true

                Global.getSoundPlayer().playSound("exoship_warp", 1.1f, 0.9f, ship!!.location, Vector2f())

                var flash = HypatiaFlashRenderer(Vector2f(ship!!.location), ExoUtils.color1, Color(130,4,189, 255),
                    300f, 1400f, 0.0f, 0.2f, 1f)

                Global.getCombatEngine().addLayeredRenderingPlugin(flash)

               /* var x = ship!!.velocity.x
                var y = ship!!.velocity.y
                ship!!.velocity.set(x * 0.2f, y * 0.2f)*/
            }

        }

        if (state == ShipSystemStatsScript.State.ACTIVE) {
           if (isOutOfBounds()) {
               ship!!.useSystem()
           }
        }
    }

    fun isOutOfBounds() : Boolean {
        var width = Global.getCombatEngine().mapWidth + 500f
        var height = Global.getCombatEngine().mapHeight + 500f

        var x = ship!!.location.x
        var y = ship!!.location.y

        if (x in -(width/2)..(width/2) && y in -(height/2)..(height/2)) {
            return false
        }
        return true
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        return system!!.state != ShipSystemAPI.SystemState.IN
    }
}

