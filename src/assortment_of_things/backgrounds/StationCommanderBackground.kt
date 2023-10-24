package assortment_of_things.backgrounds

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig
import org.lazywizard.lazylib.MathUtils

class StationCommanderBackground : BaseCharacterBackground() {


    override fun shouldShowInSelection(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig): Boolean {
        return factionSpec.id != Factions.PLAYER && !factionConfig.pirateFaction && (!factionSpec.custom.has("decentralized") || factionSpec.custom.get("decentralized") != true)
    }

    override fun getShortDescription(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig?): String {
        return "You are the commander of a small ${factionSpec.displayName} station, coming with its own sets of benefits and responsibilites."
    }

    override fun getLongDescription(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig): String {
        return "You own a small ${factionSpec.displayName} station that you can expand to your own liking. It does not function as a traditional market, but can be upgraded for increased profit and utilities."
    }

    override fun getSpawnLocationOverwrite(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): SectorEntityToken? {
        return Global.getSector().memoryWithoutUpdate.getEntity("\$rat_spawn_location_overwrite")
    }

    override fun onNewGameAfterEconomyLoad(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig) {
        var markets = Global.getSector().economy.marketsCopy.filter { it.factionId == factionSpec.id }
        var system = markets.random().starSystem
        var station = system.addCustomEntity("test", "${Global.getSector().playerPerson.nameString} Station", Entities.MAKESHIFT_STATION, factionSpec!!.id)
        station!!.setCircularOrbit(system.center, MathUtils.getRandomNumberInRange(0f, 360f), 3000f, 180f)

        Global.getSector().memoryWithoutUpdate.set("\$rat_spawn_location_overwrite", station)

    }
}