package assortment_of_things.abyss.activators

import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import org.magiclib.subsystems.MagicSubsystem
import java.awt.Color

class TemporalAssaultActivator(ship: ShipAPI?) : MagicSubsystem(ship) {

    var id = "rat_temporal_assault"
    var maxMult = 1.5f

    val jitterColor = Color(0, 255, 150, 55)
    val jitterUnderColor = Color(0, 255, 155, 155)

    val interval = IntervalUtil(0.5f, 1f)
    var afterimageInterval = IntervalUtil(0.05f, 0.05f)

    override fun getBaseInDuration(): Float {
        return 1f
    }

    override fun getBaseActiveDuration(): Float {
        return 7f
    }

    override fun getBaseOutDuration(): Float {
        return 1f
    }

    override fun getBaseCooldownDuration(): Float {
        return 15f
    }

    override fun advance(amount: Float, isPaused: Boolean) {
        super.advance(amount, isPaused)

        if (!ship.isAlive || ship.isHulk) return

        var player = ship == Global.getCombatEngine().playerShip

        if (state == State.READY || state == State.COOLDOWN) return

        var jitterLevel = effectLevel
        var jitterRangeBonus = 0f
        val maxRangeBonus = 4f
        if (state == State.IN) {
            jitterLevel = effectLevel / (1f / inDuration)
            if (jitterLevel > 1) {
                jitterLevel = 1f
            }
            jitterRangeBonus = jitterLevel * maxRangeBonus
        } else if (state == State.ACTIVE) {
            jitterLevel = 1f
            jitterRangeBonus = maxRangeBonus
        } else if (state == State.OUT) {
            jitterRangeBonus = jitterLevel * maxRangeBonus
        }
        jitterLevel = Math.sqrt(jitterLevel.toDouble()).toFloat()
        //effectLevel *= effectLevel

        ship.setJitter(id, jitterColor, jitterLevel, 3, 0f, 0 + jitterRangeBonus)
        ship.setJitterUnder(id, jitterUnderColor, jitterLevel, 10, 0f, 5f + jitterRangeBonus)

        if (state == State.IN || state == State.ACTIVE) {
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!, jitterUnderColor.setAlpha(75), jitterColor.setAlpha(75), 0.5f, 0f, Vector2f().plus(ship!!.location))
            }
        }

        val shipTimeMult = 1f + (maxMult - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)

        ship.engineController.fadeToOtherColor(this, jitterColor, Color(0, 0, 0, 0), effectLevel, 0.5f)
        ship.engineController.extendFlame(this, -0.25f, -0.25f, -0.25f)
    }


    override fun onFinished() {
        super.onFinished()

        stats.timeMult.unmodify(id)
    }

    override fun onActivate() {
        super.onActivate()

    }

    override fun shouldActivateAI(amount: Float): Boolean {


        interval.advance(amount)

        if (interval.intervalElapsed())
        {
            if (ship.shipTarget != null)
            {
                return true
            }

            var iter = Global.getCombatEngine().shipGrid.getCheckIterator(ship.location, 800f, 800f)

            for (it in iter)
            {
                if (it is ShipAPI)
                {
                    if (it.isAlive && !it.isHulk && it.owner != ship.owner && !it.isPiece)
                    {
                        return true
                    }
                }
            }
        }
        return false

    }

    override fun getDisplayText(): String {
        return ""
    }
}