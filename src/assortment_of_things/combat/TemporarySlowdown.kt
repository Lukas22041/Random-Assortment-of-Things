package assortment_of_things.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI

class TemporarySlowdown(var mult: Float, var duration: Float) : BaseEveryFrameCombatPlugin() {

    var maxDuration = duration

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

        if (Global.getCombatEngine().isPaused) return

        duration -= 1 * amount
        if (duration <= 0) {
            Global.getCombatEngine().timeMult.unmodify("rat_temp_mult")
            Global.getCombatEngine().removePlugin(this)
        }

        var level = (duration - 0f) / (maxDuration - 0f)

        var timemult = 1 + (mult * level)

        Global.getCombatEngine().timeMult.modifyMult("rat_temp_mult", 1f / timemult)
    }

}