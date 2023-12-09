package assortment_of_things.backgrounds.commander

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin
import com.fs.starfarer.api.ui.MarkerData
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exerelin.campaign.backgrounds.CharacterBackgroundIntel
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.ArrayList

class CommanderLocationInteraction(var faction: FactionSpecAPI) : RATInteractionPlugin() {

    override fun init() {

        textPanel.addPara("Select a market for your commanded station to orbit. Only markets from your commissioned faction are available unless they do not have any markets.")

        createOption("Select a market.") {

            var markets = ArrayList<MarketAPI>()
            markets.addAll(Global.getSector().economy.marketsCopy.filter { it.factionId == faction.id })
            if (markets.isEmpty()) {
                markets.addAll(Global.getSector().economy.marketsCopy)
            }

            dialog.showCampaignEntityPicker("Select where your station should orbit", "Orbit: ", "Confirm", Global.getSector().getFaction(faction.id),
            markets.map { it.primaryEntity }, object : BaseCampaignEntityPickerListener() {

                    override fun pickedEntity(entity: SectorEntityToken?) {

                        var system = entity!!.starSystem

                        var station = system.addCustomEntity("rat_station_commander_station", "${generateName()} Port", "station_side03", Factions.PLAYER)
                        //station!!.setCircularOrbit(system.center, MathUtils.getRandomNumberInRange(0f, 360f), 3000f, 180f)
                        station.setCircularOrbitPointingDown(entity, MathUtils.getRandomNumberInRange(0f, 360f), entity.radius + station.radius + 200f, 120f)
                        station.memoryWithoutUpdate["\$abandonedStation"] = true
                        val market = Global.getFactory().createMarket("rat_station_commander_market", station.name, 3)
                        market.surveyLevel = MarketAPI.SurveyLevel.FULL
                        market.primaryEntity = station
                        market.factionId = Factions.PLAYER
                        market.addSubmarket(Submarkets.SUBMARKET_STORAGE)
                        market.isPlanetConditionMarketOnly = false
                        market.addIndustry(Industries.SPACEPORT)
                        (market.getSubmarket(Submarkets.SUBMARKET_STORAGE).plugin as StoragePlugin).setPlayerPaidToUnlock(true)
                        station.market = market
                        station.memoryWithoutUpdate.unset("\$tradeMode")

                        market.stability.modifyFlat("rat_commander", 10f)
                        market.accessibilityMod.modifyFlat("rat_commander", 0.60f)
                        market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat("rat_commander", 600f)

                        station.customDescriptionId = "rat_commander_station"
                        station.setInteractionImage("illustrations", "urban00")

                        Global.getSector().memoryWithoutUpdate.set("\$rat_base_commander_station", station)

                        var intel = Global.getSector().intelManager.getIntel(CharacterBackgroundIntel::class.java).first() as CharacterBackgroundIntel
                        intel.location = system


                        var listener = CommanderStationListener(market)
                        station.memoryWithoutUpdate.set("\$rat_commander_listener", listener)
                        Global.getSector().listenerManager.addListener(listener)

                        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

                        closeDialog()
                    }

                    override fun canConfirmSelection(entity: SectorEntityToken?): Boolean {
                        return entity != null
                    }
                })
        }
    }

    fun generateName(): String? {
        MarkovNames.loadIfNeeded()
        var gen: MarkovNames.MarkovNameResult? = null
        for (i in 0..9) {
            gen = MarkovNames.generate(null)
            if (gen != null) {
                var test = gen.name
                if (test.lowercase(Locale.getDefault()).startsWith("the ")) continue
                // val p: String = pickPostfix()
                /* if (p != null && !p.isEmpty()) {
                     test += " $p"
                 }*/
                if (test.length > 22) continue
                return test
            }
        }
        return null
    }

}