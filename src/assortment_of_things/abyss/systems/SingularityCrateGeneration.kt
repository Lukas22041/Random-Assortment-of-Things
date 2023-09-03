package assortment_of_things.abyss.systems

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.instantTeleport
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.magiclib.kotlin.makeImportant
import java.util.*
import kotlin.collections.LinkedHashMap

object SingularityCrateGeneration {


    fun generate() : SectorEntityToken {
        var systems = Global.getSector().starSystems.filter { it.hasTag(Tags.THEME_REMNANT_DESTROYED) && it.hasTag(Tags.THEME_REMNANT_MAIN) && !it.isNebula }
        if (systems.isEmpty()) systems = Global.getSector().starSystems.filter { it.hasTag(Tags.THEME_REMNANT_SUPPRESSED) && it.hasTag(Tags.THEME_REMNANT_MAIN) && !it.isNebula }
        if (systems.isEmpty()) systems = Global.getSector().starSystems.filter { it.hasTag(Tags.THEME_REMNANT_RESURGENT) && it.hasTag(Tags.THEME_REMNANT_MAIN) && !it.isNebula }
        if (systems.isEmpty()) systems = Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_HIDDEN) && !it.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) && !it.isNebula }

        var system = systems.random()

        var picker = linkedMapOf<LocationType, Float>()
        picker.put(LocationType.PLANET_ORBIT, 1f)

        if (system.planets.map { it.isStar }.size == 1) {
            picker.put(LocationType.NEAR_STAR, 3f)
        }

        var locations = BaseThemeGenerator.getLocations(Random(), system, 0f, picker)
        var location = locations.pick()

        var cache = system.addCustomEntity("rat_singularity_chache_${Misc.genUID()}", "Lost Shipment", "rat_abyss_cache_singularity", Factions.REMNANTS)
        cache.orbit = location.orbit

        var fleet = generateFleet(system)
        cache.memoryWithoutUpdate.set("\$defenderFleet", fleet)

        Global.getSector().memoryWithoutUpdate.set("\$rat_singularity_cache", cache)

        return cache
    }

    fun generateFleet(system: StarSystemAPI) : CampaignFleetAPI {

        val params = FleetParamsV3(null,
            system.location,
            Factions.REMNANTS,
            5f,
            FleetTypes.PATROL_MEDIUM,
            70f,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            5f // qualityMod
        )

        val fleet = FleetFactoryV3.createFleet(params)
        return fleet
    }

}