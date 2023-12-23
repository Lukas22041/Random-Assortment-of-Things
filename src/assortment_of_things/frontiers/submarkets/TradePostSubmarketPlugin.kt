package assortment_of_things.frontiers.submarkets

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode
import com.fs.starfarer.api.campaign.FactionDoctrineAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin
import org.lazywizard.lazylib.MathUtils
import java.util.*

class TradePostSubmarketPlugin : BaseSubmarketPlugin() {


    var weapons = 7
    var fighters = 3

    var lastResetTimestamp = 0L
    var daysToReset = 30

    override fun updateCargoPrePlayerInteraction() {
        val seconds = Global.getSector().clock.convertToSeconds(sinceLastCargoUpdate)

        if (okToUpdateShipsAndWeapons()) {

            lastResetTimestamp = Global.getSector().clock.timestamp

            if (cargo == null) {
                cargo = Global.getFactory().createCargo(true)
                cargo!!.initMothballedShips(Factions.PLAYER)
            }

            cargo.clear()
            cargo.mothballedShips.clear()

            addCommodities()

            addWeapons(weapons, weapons + 3, 0, market.factionId)
            addFighters(fighters, fighters + 3, 0, market.factionId)

            addShips(market.factionId,
                90f, // combat
                25f, // freighter
                25f, // tanker
                0f, // transport
                0f, // liner
                0f, // utilityPts
                0f, // qualityOverride
                0f, // qualityMod
                null, null, 3)



        }
        cargo.sort()
    }

    override fun okToUpdateShipsAndWeapons(): Boolean {
        return Global.getSector().clock.getElapsedDaysSince(lastResetTimestamp) >= daysToReset
    }

    override fun isOpenMarket(): Boolean {
        return true
    }

    override fun isFreeTransfer(): Boolean {
        return false
    }

    override fun getTariff(): Float {
        return 0.2f
    }

    fun addCommodities() {
        var commodities = mapOf(
            Commodities.SUPPLIES to 500f,
            Commodities.FUEL to  750f,
            Commodities.HEAVY_MACHINERY to 300f,
            Commodities.CREW to 250f,
            Commodities.METALS to 100f,
            Commodities.RARE_METALS to 50f,
            Commodities.DOMESTIC_GOODS to 100f
        )

        for ((com, amount) in commodities) {
            var spec = Global.getSettings().getCommoditySpec(com)
            cargo.addCommodity(com, MathUtils.getRandomNumberInRange(amount * 0.33f, amount))
            var data = market.getCommodityData(com)
            data.playerDemandPriceMod.modifyFlat("price", spec.basePrice)
            data.playerSupplyPriceMod.modifyFlat("price", spec.basePrice)
        }
    }

    override fun addShips(factionId: String?, combat: Float, freighter: Float, tanker: Float, transport: Float, liner: Float, utility: Float, qualityOverride: Float?, qualityMod: Float,
                          modeOverride: ShipPickMode?, doctrineOverride: FactionDoctrineAPI?, maxShipSize: Int) {

        val params = FleetParamsV3(null,
            Global.getSector().playerFleet.locationInHyperspace,
            factionId,
            qualityOverride,  // qualityOverride
            FleetTypes.PATROL_LARGE,
            combat,  // combatPts
            freighter,  // freighterPts
            tanker,  // tankerPts
            transport,  // transportPts
            liner,  // linerPts
            utility,  // utilityPts
            0f // qualityMod
        )
        params.maxShipSize = maxShipSize
        params.random = Random(itemGenRandom.nextLong())
       /* params.qualityOverride = Misc.getShipQuality(market, factionId) + qualityMod
        if (qualityOverride != null) {
            params.qualityOverride = qualityOverride + qualityMod
        }*/
        //params.qualityMod = qualityMod;
        params.withOfficers = false
        params.forceAllowPhaseShipsEtc = true
        params.treatCombatFreighterSettingAsFraction = true
        /*params.modeOverride = Misc.getShipPickMode(market, factionId)
        if (modeOverride != null) {
            params.modeOverride = modeOverride
        }*/
        params.doctrineOverride = doctrineOverride
        val fleet = FleetFactoryV3.createFleet(params)
        if (fleet != null) {
            val p = 0.5f
            //p = 1f;
            for (member in fleet.fleetData.membersListCopy) {
                if (itemGenRandom.nextFloat() > p) continue
                if (member.hullSpec.hasTag(Tags.NO_SELL)) continue
                //if (!isMilitaryMarket && member.hullSpec.hasTag(Tags.MILITARY_MARKET_ONLY)) continue
                val emptyVariantId = member.hullId + "_Hull"
                addShip(emptyVariantId, true, params.qualityOverride)
            }
        }
    }

}

