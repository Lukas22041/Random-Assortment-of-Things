package assortment_of_things.abyss.activators

import activators.CombatActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class OverdriveActivator(ship: ShipAPI?) : CombatActivator(ship) {

    var color = Color(0, 150, 255)
    var id = "rat_overdrive"

    var interval = IntervalUtil(0.1f, 0.1f)

    override fun getBaseActiveDuration(): Float {
       return  4f
    }

    override fun getBaseCooldownDuration(): Float {
        return  20f
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

    override fun getBaseInDuration(): Float {
        return 1f
    }

    override fun getOutDuration(): Float {
        return 1f
    }


    override fun advance(amount: Float) {
        if (state == State.ACTIVE || state == State.IN || state == State.OUT)
        {
            ship.mutableStats.ballisticRoFMult.modifyMult(id, 1.0f + (0.3f * effectLevel))
            ship.mutableStats.ballisticWeaponFluxCostMod.modifyMult(id, 1.0f - (0.3f * effectLevel))

            ship.mutableStats.energyRoFMult.modifyMult(id, 1.0f + (0.3f * effectLevel))
            ship.mutableStats.energyWeaponFluxCostMod.modifyMult(id, 1.0f - (0.3f * effectLevel))

            ship.setJitterUnder(ship, color, effectLevel, 10, 2f, 6f)
            ship.engineController.fadeToOtherColor(id, color, color.setAlpha(10), effectLevel, 1f)
            ship.engineController.extendFlame(id, 0.2f * effectLevel, 0.2f, 0.2f)
        }
    }

    override fun onFinished() {
        super.onFinished()


        ship.mutableStats.ballisticRoFMult.unmodify(id)
        ship.mutableStats.ballisticWeaponFluxCostMod.unmodify(id)

        ship.mutableStats.energyRoFMult.unmodify(id)
        ship.mutableStats.energyWeaponFluxCostMod.unmodify(id)
    }


    override fun getDisplayText(): String {
        return "Overdrive"
    }

    override fun getHUDColor(): Color {
        return color
    }
}