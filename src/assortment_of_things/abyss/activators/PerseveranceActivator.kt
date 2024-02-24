package assortment_of_things.abyss.activators

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import org.magiclib.subsystems.MagicSubsystem
import java.awt.Color

class PerseveranceActivator(ship: ShipAPI?) : MagicSubsystem(ship) {

    var id = "rat_perseverance"

    val jitterColor = Color(255, 150, 0, 150)
    val jitterUnderColor = Color(255, 150, 0, 255)

    val interval = IntervalUtil(0.25f, 1f)

    override fun getBaseInDuration(): Float {
        return 1f
    }

    override fun getBaseActiveDuration(): Float {
        return 8f
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

        ship.engineController.fadeToOtherColor(this, jitterColor, Color(0, 0, 0, 0), effectLevel, 0.5f)
        ship.engineController.extendFlame(this, -0.25f, -0.25f, -0.25f)

        stats.hullDamageTakenMult.modifyMult(id, 1f - (1f - 0.40f) * effectLevel)
        stats.armorDamageTakenMult.modifyMult(id, 1f - (1f - 0.40f) * effectLevel)
        stats.empDamageTakenMult.modifyMult(id, 1f - (1f - 0.40f) * effectLevel)
    }



    override fun onFinished() {
        super.onFinished()

        stats.hullDamageTakenMult.unmodify(id)
        stats.armorDamageTakenMult.unmodify(id)
        stats.empDamageTakenMult.unmodify(id)
    }

    override fun onActivate() {
        super.onActivate()

    }

    override fun shouldActivateAI(amount: Float): Boolean {

        interval.advance(amount)
        if (interval.intervalElapsed())
        {
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