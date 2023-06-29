package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.IntervalUtil

class AbyssPingEmitter(var entity: SectorEntityToken) : EveryFrameScript {

    var interval = IntervalUtil(5f, 5f)
    override fun isDone(): Boolean {
        return !entity.isDiscoverable
    }

    override fun runWhilePaused(): Boolean {
       return false
    }

    override fun advance(amount: Float) {
        interval.advance(amount)
        if (interval.intervalElapsed())
        {
            AbyssUtils.createSensorIcon(entity, 20000f, AbyssUtils.SUPERCHARGED_COLOR)
        }


    }

}