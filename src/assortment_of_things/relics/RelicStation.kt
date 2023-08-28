package assortment_of_things.relics

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType
import com.fs.starfarer.api.util.WeightedRandomPicker

data class RelicStation(val entityID: String) {



    var weight = 10f

    var systemFilter: (system: StarSystemAPI) -> Boolean = { starsystem -> true }

    var locations = linkedMapOf<LocationType, Float>(
        LocationType.PLANET_ORBIT to 5f, LocationType.IN_ASTEROID_BELT to 2f, LocationType.NEAR_STAR to 2f, LocationType.STAR_ORBIT to 1f)

    var postGeneration: (system: SectorEntityToken) -> Unit = { }

}