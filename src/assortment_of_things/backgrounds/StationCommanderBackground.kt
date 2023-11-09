package assortment_of_things.backgrounds

import assortment_of_things.backgrounds.commander.CommanderStationListener
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames.MarkovNameResult
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.campaign.backgrounds.CharacterBackgroundIntel
import exerelin.utilities.NexFactionConfig
import java.util.*

class StationCommanderBackground : BaseCharacterBackground() {


    override fun shouldShowInSelection(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig): Boolean {
        return RATSettings.backgroundsEnabled!! && factionSpec.id != Factions.PLAYER
    }

    override fun getShortDescription(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig?): String {
        return "You are the commander of a small station in ${factionSpec.displayName} space, coming with its own sets of benefits and responsibilites."
    }

    override fun getLongDescription(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig): String {
        return "You command a small station in ${factionSpec.displayName} space that you can expand to your own liking. It does not function as a traditional market, but can be upgraded with its own set of modules for increased profit and utilities, like storage or ship and weapon production."
    }

    override fun getSpawnLocationOverwrite(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): SectorEntityToken? {
        return Global.getSector().memoryWithoutUpdate.getEntity("\$rat_base_commander_station")
    }


    override fun addTooltipForIntel(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.addTooltipForIntel(tooltip, factionSpec, factionConfig)

        var station = Global.getSector().memoryWithoutUpdate.getEntity("\$rat_base_commander_station")
        var system = station.starSystem

        tooltip!!.addSpacer(10f)
        tooltip.addPara("The station is located in the ${system.name}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${system.name}")
    }

    override fun onNewGameAfterEconomyLoad(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig) {
        var markets = Global.getSector().economy.marketsCopy.filter { it.factionId == factionSpec.id }

        if (markets.isEmpty()) {
            var friendlyFactions = Global.getSector().allFactions.filter { it.relToPlayer.isAtWorst(RepLevel.NEUTRAL) }
            markets = Global.getSector().economy.marketsCopy.filter { friendlyFactions.any { friendly -> friendly.id == it.factionId } }
        }

        if (markets.isEmpty()) {
            markets = Global.getSector().economy.marketsCopy
        }

        markets = markets.sortedByDescending { it.size }


        var system = markets.first().starSystem


        var locations = BaseThemeGenerator.getLocations(Random(), system, 2f,
        linkedMapOf(LocationType.STAR_ORBIT to 1f, LocationType.IN_ASTEROID_BELT to 5f, LocationType.OUTER_SYSTEM to 0.1f))

        var location = locations.pick()

        var station = system.addCustomEntity("rat_station_commander_station", "${generateName()} Port", "station_side03", Factions.PLAYER)
        //station!!.setCircularOrbit(system.center, MathUtils.getRandomNumberInRange(0f, 360f), 3000f, 180f)
        station.orbit = location.orbit
        station.memoryWithoutUpdate["\$abandonedStation"] = true
        val market = Global.getFactory().createMarket("rat_station_commander_market", station.name, 3)
        market.surveyLevel = SurveyLevel.FULL
        market.primaryEntity = station
        market.factionId = Factions.PLAYER
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE)
        market.isPlanetConditionMarketOnly = false
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

    }

    override fun onNewGameAfterTimePass(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        var station = Global.getSector().memoryWithoutUpdate.getEntity("\$rat_base_commander_station")
        var listener = station.memoryWithoutUpdate.get("\$rat_commander_listener") as CommanderStationListener
        listener.bank = 25000
        //listener.bank = 250000
    }

    fun generateName(): String? {
        MarkovNames.loadIfNeeded()
        var gen: MarkovNameResult? = null
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