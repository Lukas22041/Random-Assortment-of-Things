package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken

object RelicsUtils {

    var RELICS_STATION_KEY = "\$rat_relic_stations"
    var RELICS_SYSTEM_TAG = "rat_has_relic"
    var RELICS_ENTITY_TAG = "rat_relic_station"
    var RELICS_CONDITION_TAG = "rat_relic_condition"

    fun addRelicsStationToMemory(entity: SectorEntityToken)
    {
        var entities = Global.getSector().memoryWithoutUpdate.get(RELICS_STATION_KEY) as MutableList<SectorEntityToken>?
        if (entities == null)  {
            entities = ArrayList<SectorEntityToken>()
        }
        entities.add(entity)
        Global.getSector().memoryWithoutUpdate.set(RELICS_STATION_KEY, entities)

    }

    fun getAllRelicStations() : List<SectorEntityToken>
    {
        var entities = Global.getSector().memoryWithoutUpdate.get(RELICS_STATION_KEY) as MutableList<SectorEntityToken>?
        if (entities == null) {
            entities = ArrayList<SectorEntityToken>()
        }

        //Removes expired stations
        for (entity in ArrayList(entities)) {
            if (entity.isExpired)  {
                entities.remove(entity)
            }
        }

        return ArrayList(entities)
    }


}