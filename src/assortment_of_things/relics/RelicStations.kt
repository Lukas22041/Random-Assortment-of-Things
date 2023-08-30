package assortment_of_things.relics

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType

class RelicStations {

    //Default weight is 10f
    var stations = listOf<RelicStation>(

        //Skill Stations
        RelicStation("rat_bioengineering_station").apply {
            systemFilter = { system -> true}
        },

        RelicStation("rat_augmentation_station").apply {
            systemFilter = { system -> true }
        },

        RelicStation("rat_neural_laboratory").apply {
            systemFilter = { system -> true }
        },


        //Misc
        RelicStation("rat_orbital_construction_station").apply {
            systemFilter = { system -> system.planets.size >= 3 }
            locations = linkedMapOf(LocationType.PLANET_ORBIT to 100f)
            weight = 20f
        },

        RelicStation("rat_refurbishment_station").apply {
            systemFilter = { system -> true }
        },

        RelicStation("rat_cryochamber").apply {
            systemFilter = { system -> true }
        },
    )
}