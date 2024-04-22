package assortment_of_things.backgrounds.bounty

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getDistance
import org.magiclib.kotlin.getMarketsInLocation

class BackgroundBountyManager : EveryFrameScript {

    var finished = false
    var timestampSinceLastBounty = Global.getSector().clock.timestamp
    var daysTilNextBounty = MathUtils.getRandomNumberInRange(20f, 40f)

    var bounties = ArrayList<BountyFleetIntel>()
    var maxBounties = 3

    var defeated = 0
    var totalFPDefeated = 0

    var interval = IntervalUtil(5f, 20f)

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

        if (defeated >= 3 && totalFPDefeated >= 800) {
            finished = true
        }

        for (bounty in ArrayList(bounties)) {
            if (bounty.over) {
                bounties.remove(bounty)
            }
        }

        var daysSinceLastBounty = Global.getSector().clock.getElapsedDaysSince(timestampSinceLastBounty)
        if (!finished && bounties.size < 3 && daysSinceLastBounty >= daysTilNextBounty) {


            var location = Global.getSector().playerFleet.containingLocation
            var markets = location.getMarketsInLocation()

            var hasMarkets = markets.isNotEmpty()
            var nearMarket = markets.any { it.primaryEntity.getDistance(Global.getSector().playerFleet) <= 3000 && !it.isHidden && it.size >= 3}

            if (nearMarket) {

                interval.advance(amount)
                if (interval.intervalElapsed()) {
                    if (!Global.getSector().playerFleet.containingLocation.hasTag(Tags.THEME_HIDDEN)) {

                        var marketsToSpawnFrom = Global.getSector().economy.marketsCopy.filter {
                            it.faction.relToPlayer.isHostile && !it.isHidden && it.size >= 3 && it.containingLocation != Global.getSector().playerFleet.containingLocation }

                        var noHostiles = marketsToSpawnFrom.isEmpty()
                        if (noHostiles) {
                            marketsToSpawnFrom =  Global.getSector().economy.marketsCopy.filter {
                               !it.isHidden && it.size >= 3 && it.containingLocation != Global.getSector().playerFleet.containingLocation }
                        }

                        if (marketsToSpawnFrom.isNotEmpty()) {
                            var market = marketsToSpawnFrom.random()
                            var bounty = BountyFleetIntel(market.factionId, market, noHostiles)
                            bounty.startEvent()
                            bounties.add(bounty)

                            timestampSinceLastBounty = Global.getSector().clock.timestamp
                            daysTilNextBounty = MathUtils.getRandomNumberInRange(20f, 60f)
                        }
                    }
                }
            }
        }
    }
}
