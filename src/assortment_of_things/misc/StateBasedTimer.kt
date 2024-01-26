package assortment_of_things.misc

import org.lazywizard.lazylib.MathUtils

class StateBasedTimer(var fadeIn: Float, var duration: Float, var fadeOut: Float) {

    var state = TimerState.In

    var timer = 0f
    var maxTimer = fadeIn + duration + fadeOut
    var level = 0f
    var done = false

    enum class TimerState {
        In, Active, Out
    }

    fun advance(amount: Float) {
        timer += 1 * amount

        if (timer <= fadeIn) {
            state = TimerState.In
            level = timer / fadeIn
            level = MathUtils.clamp(level, 0f, 1f)
        }
        else if (timer <= fadeIn + duration) {
            state = TimerState.Active
            level = 1f
        }
        else if (timer <= fadeIn + duration + fadeOut){
            state = TimerState.Out
            var outTimer = timer - fadeIn - duration
            level = outTimer / fadeOut
            level = 1 - level
            level = MathUtils.clamp(level, 0f, 1f)
        }
        else {
            level = 0f
            done = true
        }
    }

    fun reset() {
        level = 0f
        duration = 0f
    }
}