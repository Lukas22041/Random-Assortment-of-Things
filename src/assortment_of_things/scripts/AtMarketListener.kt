package assortment_of_things.scripts

import com.fs.starfarer.api.campaign.econ.MarketAPI

class AtMarketListener : RATBaseCampaignEventListener() {

    var atMarket = false
    var market: MarketAPI? = null

    override fun reportPlayerClosedMarket(market: MarketAPI?) {
        atMarket = false
        this.market = null
    }

    override fun reportPlayerOpenedMarket(market: MarketAPI?) {
        atMarket = true
        this.market = market
    }
}