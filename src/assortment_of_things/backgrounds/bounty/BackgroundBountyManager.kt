package assortment_of_things.backgrounds.bounty

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getMarketsInLocation

class BackgroundBountyManager : EveryFrameScript {

    var finished = false
    var timestampSinceLastBounty = Global.getSector().clock.timestamp
    var daysTilNextBounty = MathUtils.getRandomNumberInRange(30f, 60f)

    var hasEnteredPopulatedSystem = true
    var timestampEnteredPopulatedSystem = Global.getSector().clock.timestamp
    var daysToWait = MathUtils.getRandomNumberInRange(9f, 20f)

    var bounties = ArrayList<BountyFleetIntel>()
    var maxBounties = 3

    var defeated = 0
    var totalFPDefeated = 0


    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

        if (defeated >= 3 && totalFPDefeated >= 700) {
            finished = true
        }

        for (bounty in ArrayList(bounties)) {
            if (bounty.over) {
                bounties.remove(bounty)
            }
        }

        var daysSinceLastBounty = Global.getSector().clock.getElapsedDaysSince(timestampSinceLastBounty)
        if (!finished && bounties.size < 3 && daysSinceLastBounty >= daysTilNextBounty) {

            if (!hasEnteredPopulatedSystem) {

                var location = Global.getSector().playerFleet.containingLocation
                var markets = location.getMarketsInLocation()

                if (markets.any { !it.isHidden && it.isInEconomy && it.size >= 3 }) {
                    timestampEnteredPopulatedSystem = Global.getSector().clock.timestamp
                    hasEnteredPopulatedSystem = true
                }
            }

            if (hasEnteredPopulatedSystem) {
                var daysSinceEnteredSystem = Global.getSector().clock.getElapsedDaysSince(timestampEnteredPopulatedSystem)
                if (daysSinceEnteredSystem >= daysToWait) {
                    var hostileMarkets = Global.getSector().economy.marketsCopy.filter { it.faction.relToPlayer.isHostile && !it.isHidden && it.size >= 3 }
                    if (hostileMarkets.isNotEmpty()) {
                        var market = hostileMarkets.random()
                        var bounty = BountyFleetIntel(market.factionId, market)
                        bounty.startEvent()
                        bounties.add(bounty)

                        hasEnteredPopulatedSystem = false
                        timestampSinceLastBounty = Global.getSector().clock.timestamp
                        daysToWait = MathUtils.getRandomNumberInRange(2f, 25f)
                        daysTilNextBounty = MathUtils.getRandomNumberInRange(10f, 90f)
                    }
                }
            }
        }
    }

}