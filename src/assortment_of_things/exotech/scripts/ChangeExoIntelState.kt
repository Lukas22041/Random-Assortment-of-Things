package assortment_of_things.exotech.scripts

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global

class ChangeExoIntelState : EveryFrameScript {

    var data = ExoUtils.getExoData()
    var faction = Global.getSector().getFaction("rat_exotech")

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    override fun advance(amount: Float) {
        faction.isShowInIntelTab = data.interactedWithExoship
    }

}