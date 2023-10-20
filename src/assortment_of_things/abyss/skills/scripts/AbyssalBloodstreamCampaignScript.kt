package assortment_of_things.abyss.skills.scripts

import com.fs.starfarer.api.EveryFrameScript

class AbyssalBloodstreamCampaignScript : EveryFrameScript {

    var done = false

    override fun isDone(): Boolean {
        return done
    }


    override fun runWhilePaused(): Boolean {
       return true
    }

    override fun advance(amount: Float) {

    }
}