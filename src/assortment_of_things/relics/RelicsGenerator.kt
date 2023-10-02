package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList

class RelicsGenerator {

    private val stationFractionToGenerate = 1f
    private val conditionFractionToGenerate = 1f

    fun generateStations() {
        var relicStations = WeightedRandomPicker<RelicStation>()
        for (station in RelicStations().stations.shuffled())  {
            relicStations.add(station, station.weight)
        }

        var max = (relicStations.items.sumOf { it.amount } * stationFractionToGenerate).toInt()

        var generated = 0
        for (i in 0 until max) {
            var pick = relicStations.pick()

            pick.amount -= 1
            if (pick.amount <= 0) {
                relicStations.remove(pick)
            }

            var systems = Global.getSector().starSystems.filter { it.hasTag(Tags.THEME_RUINS) && !it.hasTag(RelicsUtils.RELICS_SYSTEM_TAG) && !it.hasPulsar() }
            if (systems.isEmpty()) continue

            systems = systems.filter { pick.systemFilter(it) }
            if (systems.isEmpty()) continue

            var system = systems.random()

            var locations = BaseThemeGenerator.getLocations(Random(), system, 100f, pick.locations)
            if (locations.isEmpty) continue
            var location = locations.pick()

            var spec = Global.getSettings().getCustomEntitySpec(pick.entityID)
            var entity = system.addCustomEntity(spec.defaultName + Misc.genUID(), spec.defaultName, spec.id, Factions.NEUTRAL) ?: continue
            entity.getSalvageSeed()

            RelicsUtils.addRelicsStationToMemory(entity)
            entity.addTag(RelicsUtils.RELICS_ENTITY_TAG)
            system.addTag(RelicsUtils.RELICS_SYSTEM_TAG)

            entity.orbit = location.orbit

            pick.postGeneration(entity)

            generated++
        }

        Global.getSector().memoryWithoutUpdate.set("\$rat_relics_generated", true)
    }

    fun generateConditions() {
        var relicConditions = WeightedRandomPicker<RelicCondition>()
        for (condition in RelicConditions().conditions.shuffled())  {
            relicConditions.add(condition, condition.weight)
        }

        var max = (relicConditions.items.sumOf { it.amount } * conditionFractionToGenerate).toInt()

        var generated = 0
        for (i in 0 until max) {
            var pick = relicConditions.pick()

            pick.amount -= 1
            if (pick.amount <= 0) {
                relicConditions.remove(pick)
            }

            var systems = Global.getSector().starSystems.filter { it.hasTag(Tags.THEME_RUINS)  }
            if (systems.isEmpty()) continue

            systems = systems.filter { pick.systemFilter(it) }
            if (systems.isEmpty()) continue


            var planets = ArrayList<PlanetAPI>()
            for (system in systems) {
                var planetsInSystem = system.planets.filter { !it.isStar && !it.hasTag(RelicsUtils.RELICS_CONDITION_TAG) && it.market != null}
                if (planetsInSystem.isEmpty()) continue

                planetsInSystem = planetsInSystem.filter { pick.planetFilter(it) }
                if (planetsInSystem.isEmpty()) continue

                planets.addAll(planetsInSystem)
            }

            if (planets.isEmpty()) continue
            var planet = planets.random()
            planet.market.addCondition(pick.conditionID)
            var condition = planet.market.getFirstCondition(pick.conditionID)
            condition.isSurveyed = pick.surveyed

            planet.addTag(RelicsUtils.RELICS_CONDITION_TAG)
            planet.addTag(pick.conditionID)
            //planet.addTag("rat_relic_condition")

        }

        Global.getSector().memoryWithoutUpdate.set("\$rat_relics_conditions_generated", true)


    }
}

