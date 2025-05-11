package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssSensorEntity
import assortment_of_things.abyss.entities.light.AbyssalLightsource
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

object AbyssProcgenUtils {

    fun createSensorArray(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var array = system!!.addCustomEntity("rat_sensor_abyss_${biome.getBiomeID()}_${Misc.genUID()}", "Sensor Array", "rat_abyss_sensor", Factions.NEUTRAL)
        var plugin = array.customPlugin as AbyssSensorEntity
        plugin.biome = biome
        return array
    }

    fun createDecayingSensorArray(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var array = system!!.addCustomEntity("rat_sensor_abyss_${biome.getBiomeID()}_${Misc.genUID()}", "Deteriorating Sensor Array", "rat_decaying_abyss_sensor", Factions.NEUTRAL)
        var plugin = array.customPlugin as AbyssSensorEntity
        plugin.biome = biome
        return array
    }

    fun createResearchStation(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var research = system!!.addCustomEntity("rat_abyss_research_${biome.getBiomeID()}_${Misc.genUID()}", "Abyssal Research Station", "rat_abyss_research", Factions.NEUTRAL)
        return research
    }

    fun createAbyssalDrone(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var drone = system!!.addCustomEntity("rat_abyss_drone_${biome.getBiomeID()}_${Misc.genUID()}", "Abyssal Droneship", "rat_abyss_drone", Factions.NEUTRAL)
        return drone
    }

    fun addLightsource(entity: SectorEntityToken, radius: Float, color: Color? = AbyssUtils.ABYSS_COLOR.setAlpha(50)) : AbyssalLightsource {
        var lightsource = entity.containingLocation.addCustomEntity("rat_lightsource_${Misc.genUID()}", "", "rat_lightsource", Factions.NEUTRAL)
        lightsource.setCircularOrbit(entity, 0f, 0f, 1000f)

        var plugin = lightsource.customPlugin as AbyssalLightsource
        plugin.radius = radius
        plugin.color = color
        return plugin
    }

    fun addLightsource(system: LocationAPI, location: Vector2f, radius: Float, color: Color? = AbyssUtils.ABYSS_COLOR.setAlpha(50)) : AbyssalLightsource {
        var lightsource = system.addCustomEntity("rat_lightsource_${Misc.genUID()}", "", "rat_lightsource", Factions.NEUTRAL)
        lightsource.location.set(location)

        var plugin = lightsource.customPlugin as AbyssalLightsource
        plugin.radius = radius
        plugin.color = color
        return plugin
    }

    fun clearTerrainAround(fogTerrain: BaseFogTerrain, entity: SectorEntityToken, radius: Float)
    {
        val editor = OldNebulaEditor(fogTerrain)

        editor.clearArc(entity.location.x, entity.location.y, 0f, radius, 0f, 360f)
        editor.clearArc(entity.location.x, entity.location.y, 0f, radius, 0f, 360f, 0.25f)
    }

    fun clearTerrainAround(fogTerrain: BaseFogTerrain, system: LocationAPI, location: Vector2f, radius: Float)
    {
        val editor = OldNebulaEditor(fogTerrain)

        editor.clearArc(location.x, location.y, 0f, radius, 0f, 360f)
        editor.clearArc(location.x, location.y, 0f, radius, 0f, 360f, 0.25f)
    }

}