package assortment_of_things.combat.activators

import activators.CombatActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil
import java.awt.Color

class TemporalAssaultActivator(ship: ShipAPI?) : CombatActivator(ship) {

    var id = "rat_temporal_assault"
    var maxMult = 1.5f

    val jitterColor = Color(0, 255, 150, 55)
    val jitterUnderColor = Color(0, 255, 155, 155)

    val interval = IntervalUtil(3f, 5f)



    override fun getBaseInDuration(): Float {
        return 2f
    }

    override fun getBaseActiveDuration(): Float {
        return 8f
    }

    override fun getBaseOutDuration(): Float {
        return 2f
    }

    override fun getBaseCooldownDuration(): Float {
        return 10f
    }

    override fun advance(amount: Float) {
        super.advance(amount)

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


        val shipTimeMult = 1f + (maxMult - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)

        ship.engineController.fadeToOtherColor(this, jitterColor, Color(0, 0, 0, 0), effectLevel, 0.5f)
        ship.engineController.extendFlame(this, -0.25f, -0.25f, -0.25f)
    }


    override fun onFinished() {
        super.onFinished()

        stats.timeMult.unmodify(id)
    }

    override fun shouldActivateAI(p0: Float): Boolean {


        interval.advance(p0)

        if (interval.intervalElapsed())
        {
            return true
        }
        return false

    }

    override fun getDisplayText(): String {
        return ""
    }
}