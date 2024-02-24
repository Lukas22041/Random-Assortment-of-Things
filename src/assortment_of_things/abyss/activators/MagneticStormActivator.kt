package assortment_of_things.abyss.activators

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.magiclib.subsystems.MagicSubsystem
import java.awt.Color

class MagneticStormActivator(ship: ShipAPI?) : MagicSubsystem(ship) {

    var id = "rat_magnetic_storm"

    val jitterColor = Color(200, 0, 50, 150)
    val jitterUnderColor = Color(200, 0, 50, 200)

    val interval = IntervalUtil(0.15f, 0.3f)


    override fun getBaseInDuration(): Float {
        return 1f
    }

    override fun getBaseActiveDuration(): Float {
        return 5f
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

        interval.advance(amount)
        if (interval.intervalElapsed())
        {
            var iter = Global.getCombatEngine().allObjectGrid.getCheckIterator(ship.location, 200f, 200f)
            for (it in iter)
            {
                if (it is DamagingProjectileAPI)
                {
                    if (it.hitpoints != 0f && it.maxHitpoints != 0f && it.owner != ship.owner)
                    {
                        Global.getCombatEngine().spawnEmpArc(ship, ship.location, ship, it, DamageType.ENERGY, 10f, 10f, 250f,   // max range
                            "tachyon_lance_emp_impact", 1f, jitterColor, jitterUnderColor)
                        break;
                    }
                }
                if (it is ShipAPI)
                {
                    if (it.isAlive && !it.isHulk && it.owner != ship.owner)
                    {
                        Global.getCombatEngine().spawnEmpArc(ship, ship.location, ship, it, DamageType.ENERGY, 10f, 10f, 250f,   // max range
                            "tachyon_lance_emp_impact", 1f, jitterColor, jitterUnderColor)
                        break;
                    }
                }
            }
        }

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