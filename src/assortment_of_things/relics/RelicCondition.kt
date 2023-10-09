package assortment_of_things.relics

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.StarSystemAPI

class RelicCondition(var conditionID: String) {

    var weight = 10f

    var planetFilter: (planet: PlanetAPI) -> Boolean = { planet -> !planet.isGasGiant }
    var systemFilter: (system: StarSystemAPI) -> Boolean = { system -> true }

    var allowedCategories = ArrayList<String>()
    var disallowedCategories = ArrayList<String>()

    var surveyed = false

}