package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import java.util.*

class RelicsGenerator {

    private val fractionToGenerate = 1f

    fun generate() {
        var relicStations = WeightedRandomPicker<RelicStation>()
        for (station in RelicStations().stations.shuffled())  {
            relicStations.add(station, station.weight)
        }

        var max = (relicStations.items.count() * fractionToGenerate).toInt()


        var generated = 0
        for (i in 0 until max) {
            var pick = relicStations.pickAndRemove()

            var systems = Global.getSector().starSystems.filter { it.hasTag(Tags.THEME_RUINS) && !it.hasTag(RelicsUtils.RELICS_SYSTEM_TAG) && !it.hasPulsar() }
            if (systems.isEmpty()) continue

            systems = systems.filter { pick.systemFilter(it) }
            if (systems.isEmpty()) continue

            systems = systems.sortedBy { it.hasTag(Tags.THEME_RUINS_MAIN) }
            var system = systems.random()

            var locations = BaseThemeGenerator.getLocations(Random(), system, 100f, pick.locations)
            if (locations.isEmpty) continue
            var location = locations.pick()

            var spec = Global.getSettings().getCustomEntitySpec(pick.entityID)
            var entity = system.addCustomEntity(spec.defaultName + Misc.genUID(), spec.defaultName, spec.id, Factions.NEUTRAL) ?: continue

            RelicsUtils.addRelicsStationToMemory(entity)
            entity.addTag(RelicsUtils.RELICS_ENTITY_TAG)
            system.addTag(RelicsUtils.RELICS_SYSTEM_TAG)

            entity.orbit = location.orbit

            pick.postGeneration(entity)
            generated++
        }

        Global.getSector().memoryWithoutUpdate.set("\$rat_relics_generated", true)
    }

}