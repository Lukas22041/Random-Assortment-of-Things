package assortment_of_things.campaign.scripts

import assortment_of_things.misc.RATControllerHullmod
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.util.IntervalUtil

class ApplyRATControllerToPlayerFleet : EveryFrameScript {

    var interval = IntervalUtil(0.2f, 0.2f)

    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }


    override fun advance(amount: Float) {
        interval.advance(amount)
        if (interval.intervalElapsed()) {
            RATControllerHullmod.ensureAddedControllerToFleet()
        }
    }

}