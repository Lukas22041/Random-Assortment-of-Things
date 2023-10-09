package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class RelicConditions {

    //Default weight is 10f
    var conditions = listOf<RelicCondition>(

        RelicCondition("rat_warscape").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> Misc.hasRuins(planet.market) }

            weight = 20f
        },

        RelicCondition("rat_ancient_industries").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            weight = 5f
        },

        RelicCondition("rat_ancient_fuel_hub").apply {
            systemFilter = { system -> true}
            planetFilter = { planet ->
                 planet.hasCondition("volatiles_diffuse") || planet.hasCondition("volatiles_abundant") || planet.hasCondition("volatiles_plentiful") }
            weight = 5f
        },

        RelicCondition("rat_ancient_military_hub").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            weight = 5f
        },

        RelicCondition("rat_ancient_megacities").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            weight = 5f
        },

        RelicCondition("rat_kinetic_launchsystem").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            weight = 7.5f
        },

        RelicCondition("rat_bionic_plantlife").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> Misc.hasFarmland(planet.market) }
            weight = 20f
        },

        RelicCondition("rat_defensive_drones").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            weight = 5f
        },

        RelicCondition("rat_engineered_utopia").apply {

            systemFilter = { system -> true}

            planetFilter = { planet ->
                Misc.hasFarmland(planet.market)
            }

            allowedCategories.add("cat_hab4")
            allowedCategories.add("cat_hab3")

            weight = 20f
        },

        RelicCondition("rat_rampant_military_core").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            weight = 5f
        },
    )
}