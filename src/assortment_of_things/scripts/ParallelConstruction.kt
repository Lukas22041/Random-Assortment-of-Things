package assortment_of_things.scripts

import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc

class ParallelConstruction : EveryFrameScript {

    private var interval = IntervalUtil(3f,3f)

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {

        if (!RATSettings.parallelEnabled!!) return

        interval.advance(amount)
        if (interval.intervalElapsed())
        {
            Global.getSector().allLocations.flatMap { location -> Misc.getMarketsInLocation(location) }.forEach { market ->
                if (!RATSettings.parallelApplyToNPCs!! && !market.isPlayerOwned) return@forEach
                var industriesToBuild = market.constructionQueue.items.filterNotNull()
                industriesToBuild.forEach {
                    market.constructionQueue.removeItem(it.id)
                    market.addIndustry(it.id)
                    market.getIndustry(it.id).startBuilding()
                }
            }
        }
    }

}