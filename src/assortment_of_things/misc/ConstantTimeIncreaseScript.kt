package assortment_of_things.misc

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global

//Used for some visual effects requiring some passing time
class ConstantTimeIncreaseScript : EveryFrameScript {

    var time = 0f

    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        if (time >= 1000000) {
            time = 0f
        }

        var div = 1f
        if (Global.getSector().isFastForwardIteration) {
            div = Global.getSettings().getFloat("campaignSpeedupMult")
        }

        time += 1 / div * amount
    }

}