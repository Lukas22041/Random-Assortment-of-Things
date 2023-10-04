package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class RelicConditions {

    //Default weight is 10f
    var conditions = listOf<RelicCondition>(

        RelicCondition("rat_warscape").apply {
            systemFilter = { system -> true}
            planetFilter = { planet ->
                var spec = Global.getSettings().getSpec(PlanetGenDataSpec::class.java, planet.typeId, true) as PlanetGenDataSpec?
                Misc.hasRuins(planet.market) &&  spec != null && spec.category != "cat_hab4" && spec.category != "cat_hab3"
            }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_ancient_industries").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_ancient_fuel_hub").apply {
            systemFilter = { system -> true}
            planetFilter = { planet ->
                 planet.hasCondition("volatiles_diffuse") || planet.hasCondition("volatiles_abundant") || planet.hasCondition("volatiles_plentiful") }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_ancient_military_hub").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_kinetic_launchsystem").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_bionic_plantlife").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> Misc.hasFarmland(planet.market) }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_ancient_megacities").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_defensive_drones").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            amount = MathUtils.getRandomNumberInRange(2,3)
        },

        RelicCondition("rat_engineered_utopia").apply {

            systemFilter = { system -> true}

            planetFilter = { planet ->
                var spec = Global.getSettings().getSpec(PlanetGenDataSpec::class.java, planet.typeId, true) as PlanetGenDataSpec?
                Misc.hasFarmland(planet.market)  && spec != null && (spec.category == "cat_hab4" || spec.category == "cat_hab3")
            }
            amount = 2
        },

        RelicCondition("rat_rampant_military_core").apply {
            systemFilter = { system -> true}
            planetFilter = { planet -> !planet.isGasGiant }
            amount = MathUtils.getRandomNumberInRange(2,4)
        },
    )
}