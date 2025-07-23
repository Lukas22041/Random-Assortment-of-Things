package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssSensorEntity
import assortment_of_things.abyss.entities.light.AbyssalLightsource
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getSalvageSeed
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*
import kotlin.math.min

object AbyssProcgenUtils {

    fun setAbyssalMatterDrop(sectorEntityToken: SectorEntityToken, amount: Float) {
        sectorEntityToken.memoryWithoutUpdate.set("\$rat_abyssal_drop", amount)
    }

    fun getAbyssalMatterDrop(sectorEntityToken: SectorEntityToken) : Float {
        var drop = sectorEntityToken.memoryWithoutUpdate.get("\$rat_abyssal_drop") as Float? ?: 0f
        return drop
    }

    class EntityRotationScript(var entity: SectorEntityToken) : EveryFrameScript {

        var speed = MathUtils.getRandomNumberInRange(-3.5f, 3.5f)

        override fun isDone(): Boolean {
            return false
        }

        override fun runWhilePaused(): Boolean {
            return false
        }

        override fun advance(amount: Float) {
            entity.facing += speed * amount
        }
    }

    //Fleets base their defense range to the target based on the targets radius,
    //So using invisible tokens allows changing that distance dynamicly.
    class DynamicPatrolTokenScript(var token: CustomCampaignEntityAPI, var minDistance: Float, var maxDistance: Float, var minDays: Float, var maxDays: Float) : EveryFrameScript {

        var daysTilReset = MathUtils.getRandomNumberInRange(minDays, maxDays)
        var clock = Global.getSector().clock

        override fun isDone(): Boolean {
            return false
        }

        override fun runWhilePaused(): Boolean {
            return false
        }


        override fun advance(amount: Float) {
            daysTilReset -= clock.convertToDays(amount)
            if (daysTilReset <= 0) {
                daysTilReset = MathUtils.getRandomNumberInRange(minDays, maxDays)
                token.radius = MathUtils.getRandomNumberInRange(minDistance, maxDistance)
            }
        }
    }

    fun createPatrolToken(target: SectorEntityToken, system: StarSystemAPI, biome: BaseAbyssBiome, minDistance: Float, maxDistance: Float, minDays: Float, maxDays: Float) : CustomCampaignEntityAPI {
        var token = system.addCustomEntity("rat_abyss_token${Misc.genUID()}", "", "rat_abyss_token", Factions.NEUTRAL)
        token.setCircularOrbit(target, 0.1f, 0.1f, 999f)
        token.radius = MathUtils.getRandomNumberInRange(minDistance, maxDistance)

        token.addScript(DynamicPatrolTokenScript(token, minDistance, maxDistance, minDays, maxDays))

        return token
    }

    fun spawnEntity(system: StarSystemAPI, biome: BaseAbyssBiome, type: String) : SectorEntityToken {
        var entity = system.addCustomEntity("${type}_${biome.getBiomeID()}_${Misc.genUID()}", null, type, Factions.NEUTRAL)
        entity.addScript(EntityRotationScript(entity))
        entity.getSalvageSeed() //Ensures that a salvage seed gets generated.
        return entity
    }

    //Claims the cell, if picked
    fun spawnEntityAtOrbitOrLightsource(system: StarSystemAPI, type: String, orbits: List<BaseAbyssBiome.LightsourceOrbit>, cells: List<BiomeCellData>, isMajor: Boolean, defenseFleetChance: Float) : SectorEntityToken? {

        var orbit: BaseAbyssBiome.LightsourceOrbit? = null
        var cell: BiomeCellData? = null


        orbit = orbits.randomOrNull()
        cell = cells.randomOrNull()

        //If either of them is null, guarantee one of them is picked atleast.
        var pickOrbit = Random().nextFloat() >= 0.5f
        if (orbit != null && (pickOrbit || cell == null)) {
            var biome = orbit.biome
            biome.lightsourceOrbits.remove(orbit) //Remove from lightsources list
            if (isMajor) orbit.setClaimedByMajor()

            var entity = spawnEntity(system, biome, type)
            entity.setCircularOrbit(orbit.lightsource, MathUtils.getRandomNumberInRange(0f, 360f), orbit.distance, orbit.orbitDays)
            return entity
        }
        else if (cell != null) {
            var biome = cell.getBiome()
            var loc = cell.getRandomLocationInCell()
            cell.claimed = true

            var entity = spawnEntity(system, biome!!, type)
            entity.setLocation(loc.x, loc.y)
            if (Random().nextFloat() <= defenseFleetChance) {
                biome.spawnDefenseFleet(entity)
            }
            return entity
        }

        return null
    }

    fun createRandomDerelictShip(system: StarSystemAPI, faction: String = "rat_abyssals", isNoLarges: Boolean = false) : SectorEntityToken{
        val params = DerelictShipEntityPlugin.createRandom(faction, pickDerelictType(Random(), isNoLarges), Random(), DerelictShipEntityPlugin.getDefaultSModProb())

        val entity = BaseThemeGenerator.addSalvageEntity(Random(), system, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI
        entity.setDiscoverable(true)
        entity.addTag(params.ship.variantId)

        return entity
    }

    fun createRandomDerelictAbyssalShip(system: StarSystemAPI, faction: String = "rat_abyssals", isNoLarges: Boolean = false) : SectorEntityToken{
        var entity = createRandomDerelictShip(system, faction, isNoLarges)
        entity.addTag(AbyssTags.ABYSS_WRECK)
        return entity
    }

    fun createDerelictShip(system: StarSystemAPI, variant: String) : SectorEntityToken{
        val params = DerelictShipEntityPlugin.createVariant(variant, Random(), DerelictShipEntityPlugin.getDefaultSModProb())

        val entity = BaseThemeGenerator.addSalvageEntity(Random(), system, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI
        entity.addTag(variant)
        entity.setDiscoverable(true)

        return entity
    }

    fun createDerelictAbyssalShip(system: StarSystemAPI, variant: String) : SectorEntityToken {
        var entity = createDerelictShip(system, variant)
        entity.addTag(AbyssTags.ABYSS_WRECK)
        return entity
    }

    fun pickDerelictType(random: Random?, isNoLarges: Boolean): DerelictType {
        val picker = WeightedRandomPicker<DerelictType>(random)

        //picker.add(DerelictType.CIVILIAN, 10f)
        if (!isNoLarges) picker.add(DerelictType.LARGE, 5f)
        picker.add(DerelictType.MEDIUM, 10f)
        picker.add(DerelictType.SMALL, 20f)

        return picker.pick()
    }

    fun createSensorArray(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var array = spawnEntity(system, biome, "rat_abyss_sensor")
        var plugin = array.customPlugin as AbyssSensorEntity
        plugin.biome = biome
        return array
    }

    fun createDecayingSensorArray(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var array = spawnEntity(system, biome, "rat_decaying_abyss_sensor")
        var plugin = array.customPlugin as AbyssSensorEntity
        plugin.biome = biome
        return array
    }

    fun createResearchStation(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var research = spawnEntity(system, biome, "rat_abyss_research")
        return research
    }

    fun createFabricatorStation(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var fabricator = spawnEntity(system, biome, "rat_abyss_fabrication")
        return fabricator
    }

    fun createAbyssalDrone(system: StarSystemAPI, biome: BaseAbyssBiome) : SectorEntityToken {
        var drone = spawnEntity(system, biome, "rat_abyss_drone")
        return drone
    }

    fun addLightsourceWithBiomeColor(entity: SectorEntityToken, biome: BaseAbyssBiome, radius: Float, alpha: Int,) : AbyssalLightsource {
        var lightsource = entity.containingLocation.addCustomEntity("rat_lightsource_${biome.getBiomeID()}_${Misc.genUID()}", "", "rat_lightsource", Factions.NEUTRAL)
        lightsource.setCircularOrbit(entity, 0f, 0f, 1000f)

        var plugin = lightsource.customPlugin as AbyssalLightsource
        plugin.radius = radius
        plugin.color = biome.getBiomeColor().setAlpha(alpha)
        return plugin
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