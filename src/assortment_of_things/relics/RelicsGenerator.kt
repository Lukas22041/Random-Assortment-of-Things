package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.apache.log4j.Level
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class RelicsGenerator {

    private val stationFractionToGenerate = 1f

    private val chanceToGenerateCondition = 0.1f
    private val extraChanceToGenerateConditionInRuins = 0.10f

    private val chanceToGenerateSecondCondition = 0.05f
    private val extraChanceToGenerateSecondConditionInRuins = 0.1f

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

            var systems = Global.getSector().starSystems.filter { (it.hasTag(Tags.THEME_RUINS) || it.hasTag(Tags.THEME_DERELICT) || it.hasTag(Tags.THEME_REMNANT) || it.hasTag(Tags.THEME_MISC))
                    &&  !it.hasTag(RelicsUtils.RELICS_SYSTEM_TAG) && !it.hasTag(Tags.SYSTEM_ABYSSAL) && !it.hasPulsar() }
            if (systems.isEmpty()) continue

            systems = systems.filter { pick.systemFilter(it) }
            if (systems.isEmpty()) continue

            var systemPicker = WeightedRandomPicker<StarSystemAPI>()
            for (system in systems) {
                var weight = 0f

                if (system.hasTag(Tags.THEME_RUINS)) weight = 70f
                else if (system.hasTag(Tags.THEME_DERELICT)) weight = 15f
                else if (system.hasTag(Tags.THEME_REMNANT)) weight = 15f
                else if (system.hasTag(Tags.THEME_MISC)) weight = 5f

                systemPicker.add(system, weight)
            }

            if (systemPicker.isEmpty) return
            var system = systemPicker.pick()

            var spec = Global.getSettings().getCustomEntitySpec(pick.entityID)

            var locations = BaseThemeGenerator.getLocations(Random(), system, 100 + spec.defaultRadius, pick.locations)
            if (locations.isEmpty) continue
            var location = locations.pick()

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

        var generated = 0
        var generatedConditons = HashMap<String, Int>()

        var planets = Global.getSector().starSystems.filter { it.hasTag(Tags.THEME_RUINS) || it.hasTag(Tags.THEME_DERELICT) || it.hasTag(Tags.THEME_REMNANT) || it.hasTag(Tags.THEME_MISC) }
            .flatMap { system -> system.planets.filter { planet -> !planet.isStar } }

        planets = planets.shuffled()

        for (planet in planets) {
            var chance = chanceToGenerateCondition
            var spec = Global.getSettings().getSpec(PlanetGenDataSpec::class.java, planet.typeId, true) as PlanetGenDataSpec? ?: return

            if (spec.category == "cat_hab4" || spec.category == "cat_hab3") chance += 0.1f

            if (planet.starSystem.hasTag(Tags.THEME_RUINS)) chance += extraChanceToGenerateConditionInRuins

            if (Random().nextFloat() > chance) continue

            var firstCondition = generateCondition(planet, spec)

            if (firstCondition != null) {

                if (generatedConditons.contains(firstCondition)) {
                    generatedConditons.put(firstCondition, generatedConditons.get(firstCondition)!! + 1)
                }
                else {
                    generatedConditons.put(firstCondition, 1)
                }

                var secondChance = chanceToGenerateSecondCondition
                if (planet.starSystem.hasTag(Tags.THEME_RUINS)) secondChance += extraChanceToGenerateSecondConditionInRuins

                if (Random().nextFloat() < secondChance) {
                    var secondCondition = generateCondition(planet, spec)
                    if (secondCondition != null) {
                        generated++

                        if (generatedConditons.contains(secondCondition)) {
                            generatedConditons.put(secondCondition, generatedConditons.get(secondCondition)!! + 1)
                        }
                        else {
                            generatedConditons.put(secondCondition, 1)
                        }
                    }
                }

                generated++
            }
        }

        var percentage = (generated.toFloat() / planets.size.toFloat() * 100f).roundToInt()

        var logger = Global.getLogger(this::class.java)
        logger.level = Level.ALL
        logger.debug("RAT: Generated $generated conditions in the sector")
        logger.debug("RAT: Spread across ${planets.size} planets, covering $percentage% of all planets")
        logger.debug(" ")

        for ((key, value) in generatedConditons) {
            var conditionPercentage = (value.toFloat() / generated.toFloat() * 100f)
            logger.debug("$key: $value ($conditionPercentage%)")
        }

        logger.debug(" ")

        Global.getSector().memoryWithoutUpdate.set("\$rat_relics_conditions_generated", true)

    }

    fun generateCondition(planet: PlanetAPI, spec: PlanetGenDataSpec) : String? {
        var system = planet.starSystem

        var relicConditions = WeightedRandomPicker<RelicCondition>()
        for (condition in RelicConditions().conditions.shuffled())  {
            if (!condition.systemFilter(system)) continue
            if (!condition.planetFilter(planet)) continue
            if (system.planets.any { it.hasCondition(condition.conditionID) }) continue
            if (condition.allowedCategories.isNotEmpty()) {
                if (!condition.allowedCategories.contains(spec.category)) continue
            }
            if (condition.disallowedCategories.isNotEmpty()) {
                if (condition.disallowedCategories.contains(spec.category)) continue
            }

            relicConditions.add(condition, condition.weight)
        }

        if (relicConditions.isEmpty) return null
        var pick = relicConditions.pick()

        planet.market.addCondition(pick.conditionID)
        var condition = planet.market.getFirstCondition(pick.conditionID)
        condition.isSurveyed = pick.surveyed

        planet.addTag(RelicsUtils.RELICS_CONDITION_TAG)
        planet.addTag(pick.conditionID)

        return condition.id
    }
}

