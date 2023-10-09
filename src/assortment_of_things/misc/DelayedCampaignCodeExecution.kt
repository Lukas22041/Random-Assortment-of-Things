package assortment_of_things.misc

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global

class DelayedCampaignCodeExecution(var days: Float, var lambda: () -> Unit) : EveryFrameScript {

    var timestamp = Global.getSector().clock.timestamp
    var done = false

    init {
        Global.getSector().addScript(this)
    }

    override fun isDone(): Boolean {
        return done
    }

    override fun runWhilePaused(): Boolean {
        return false
    }


    override fun advance(amount: Float) {
        if (Global.getSector().clock.getElapsedDaysSince(timestamp) >= days) {
            lambda()
            done = true
        }
    }
}