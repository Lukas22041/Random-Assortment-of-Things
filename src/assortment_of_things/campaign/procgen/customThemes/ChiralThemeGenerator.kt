package assortment_of_things.campaign.procgen.customThemes

import assortment_of_things.campaign.plugins.entities.DimensionalTearEntity
import assortment_of_things.campaign.procgen.ProcgenUtility
import assortment_of_things.misc.RATStrings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode
import com.fs.starfarer.api.campaign.FactionDoctrineAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.fleet.MutableMarketStats
import lunalib.lunaExtensions.getSystemsWithTag
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*


class ChiralThemeGenerator : BaseThemeGenerator() {


    companion object {
        var pirateOutposts: MutableList<MarketAPI> = ArrayList()
    }

    //0 for Misc
    //100 for Derelict,
    //100 for Ruins
    //100 for Remnant
    override fun getWeight(): Float {
        return 100f
    }

    //1000 for Derelict,
    //1500 for Remnant
    //2000 for Ruins
    //1000000 for Misc
    override fun getOrder(): Int {
        return 499
    }

    override fun getThemeId(): String {
        return RATStrings.CHIRAL_THEME_ID
    }

    override fun generateForSector(context: ThemeGenContext?, allowedSectorFraction: Float) {
        val total: Float = (context!!.constellations.size - context.majorThemes.size).toFloat() * allowedSectorFraction
        if (total <= 0) return



        //gets available constellations that havent been used yet
        val constellations: List<Constellation?>? = ProcgenUtility.getSortedAvailableConstellations(context, false, Vector2f(), null)
        Collections.reverse(constellations)
        for (constellation in constellations!!)
        {
            val systems: List<StarSystemData> = constellation!!.systems.map { computeSystemData(it) }

            //looks for a system with blackholes, if none are found it continues to the next constellation
            var allowedStars = listOf(StarTypes.WHITE_DWARF, StarTypes.RED_DWARF, StarTypes.BROWN_DWARF, StarTypes.ORANGE, StarTypes.YELLOW)
            val mainCandidates = ProcgenUtility.getScoredSystemsByFilter(systems) {
                it.system.center.isStar && allowedStars.contains((it.system.center as PlanetAPI).typeId)
                && it.system.terrainCopy.find { it.type == Terrain.ASTEROID_BELT } != null && it.planets.size >= 2
                && it.system.planets.find { planet -> planet.typeId.equals(StarTypes.NEUTRON_STAR) } == null
                && it.system.secondary == null
            }
            if (mainCandidates!!.isEmpty()) continue

            //Runs the generation for the main system of the constellation
            var secondarySystems = constellation.systems.toMutableList()
            /*for (main in mainCandidates)
            {
                populateMain(main)
                secondarySystems.remove(main.system)
            }*/

            var main = mainCandidates.get(0)
            populateMain(main)
            secondarySystems.remove(main.system)

            context.majorThemes.put(constellation, themeId)

            for (system in secondarySystems)
            {
                var data = computeSystemData(system)
                populateNonMain(data)
            }
            break;
        }
    }

    fun populateMain(data: StarSystemData)
    {
        data.system.addTag(RATStrings.THEME_CHIRAL)
        data.system.addTag(RATStrings.THEME_CHIRAL_MAIN)
        data.system.addTag(Tags.THEME_UNSAFE)

        //required to make ARS not spawn bases in it.
        data.system.isProcgen = false

        addDerelictShips(data, 1f, 3, 7, createStringPicker(Factions.HEGEMONY, 1f, Factions.TRITACHYON, 1f, Factions.PERSEAN, 1f))

        generateMirrorCopy(data)
    }

    fun populateNonMain(data: StarSystemData)
    {
        data.system.addTag(RATStrings.THEME_CHIRAL)
        data.system.addTag(RATStrings.THEME_CHIRAL_SECONDARY)

        val special = data.isBlackHole || data.isNebula || data.isPulsar
        if (special) {
            addResearchStations(data, 0.75f, 1, 1, createStringPicker(Entities.STATION_RESEARCH, 1f))
        }

        if (!data.resourceRich.isEmpty()) {
            addMiningStations(data, 0.5f, 1, 1, createStringPicker(Entities.STATION_MINING, 10f))
        }

        if (!special && !data.habitable.isEmpty()) {
            addHabCenters(data, 0.40f, 1, 1, createStringPicker(Entities.ORBITAL_HABITAT, 10f))
        }

        addCaches(data, 0.35f, 0,2, createStringPicker(
            Entities.SUPPLY_CACHE, 4f,
            Entities.SUPPLY_CACHE_SMALL, 10f,
            Entities.EQUIPMENT_CACHE, 4f,
            Entities.EQUIPMENT_CACHE_SMALL, 10f))

        addDerelictShips(data, 1f, 1, 5, createStringPicker(Factions.HEGEMONY, 1f, Factions.TRITACHYON, 1f, Factions.PERSEAN, 1f))
        addShipGraveyard(data, 0.6f, 1,1, createStringPicker(Factions.HEGEMONY, 1f, Factions.TRITACHYON, 1f, Factions.PERSEAN, 1f))
    }

    fun generateMirrorCopy(ogData: StarSystemData) : StarSystemAPI
    {
        var ogSystem = ogData.system
        var mirroredSystem = Global.getSector().createStarSystem("${ogSystem.baseName}")
        mirroredSystem.location.set(ogSystem.location)
        mirroredSystem.doNotShowIntelFromThisLocationOnMap = true
        mirroredSystem.backgroundTextureFilename = "graphics/backgrounds/chiral_bg.jpg"
        mirroredSystem.addTag(RATStrings.THEME_CHIRAL_COPY)
        mirroredSystem.addTag(Tags.THEME_HIDDEN)
        mirroredSystem.addTag(Tags.THEME_UNSAFE)
        mirroredSystem.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
        mirroredSystem.isProcgen = false

        var mirrorEntities: MutableList<MirrorEntity> = ArrayList()

        //Copy Star
        var ogStar = ogSystem.star
        var mirroredStar = mirroredSystem.initStar("${ogStar.id}-chiral","rat_chiral_star", ogStar.radius, 200f)
        mirroredSystem.lightColor = Color(0,100,200,150)

        mirrorEntities.add(MirrorEntity(ogStar, mirroredStar))

        for (ogPlanet in ogSystem.planets)
        {
            if (ogPlanet.isStar) continue
            if (ogPlanet.orbitFocus != ogStar) continue

            var type = "rat_chiral_planet"

            var mirroredPlanet = mirroredSystem.addPlanet("${ogPlanet.id}-chiral", mirroredStar, ogPlanet.name,
                type, ogPlanet.circularOrbitAngle, ogPlanet.radius, ogPlanet.circularOrbitRadius, ogPlanet.circularOrbitPeriod)


            mirroredPlanet.addTag(Tags.PLANET)

            mirroredPlanet.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)

            mirrorEntities.add(MirrorEntity(ogPlanet, mirroredPlanet))
        }

        //Copy moons using mirrorEntities here
        for (ogMoon in ogSystem.planets)
        {
            var ogFocus = mirrorEntities.find { ogMoon.orbitFocus == it.originalEntity }
            if (ogFocus == null) continue

            var mirroredMoon = mirroredSystem.addPlanet("${ogMoon.id}-chiral", ogFocus.mirroredEntity, ogMoon.name,
                "rat_chiral_planet", ogMoon.circularOrbitAngle, ogMoon.radius, ogMoon.circularOrbitRadius, ogMoon.circularOrbitPeriod)

            mirroredMoon.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)

            mirrorEntities.add(MirrorEntity(ogMoon, mirroredMoon))
        }


        for (ogTerrain in ogSystem.terrainCopy)
        {
            var matchingFocus = mirrorEntities.find { ogTerrain.orbitFocus == it.originalEntity } ?: continue
            var plugin = ogTerrain.plugin


            var test =  ogSystem.terrainCopy
            var test2 = ogTerrain

            if (plugin is AsteroidBeltTerrainPlugin)
            {
                if (plugin.params == null) continue
                /*var mirrorTerrain = mirroredSystem.addAsteroidBelt(matchingFocus.mirroredEntity, plugin.params.numAsteroids, plugin.params.middleRadius, plugin.params.bandWidthInEngine, plugin.params.minOrbitDays, plugin.params.maxOrbitDays)*/
                if (plugin.ringParams == null) continue
                mirroredSystem.addRingBand(matchingFocus.mirroredEntity,  "misc", "rings_dust0", 256f, 1, mirroredStar.lightColor, 256f, plugin.params.middleRadius, plugin.params.maxOrbitDays,
                    Terrain.RING, "Ring Band")
                //system.addRingBand(lethia_star, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 3650, 220f);
            }

            if (plugin is RingSystemTerrainPlugin)
            {
                if (plugin.params == null) continue
                if (plugin.ringParams == null) continue

                mirroredSystem.addRingBand(matchingFocus.mirroredEntity,  "misc", "rings_dust0", 256f, 1, Color.white, 256f, plugin.ringParams.middleRadius, plugin.entity.circularOrbitPeriod,
                    Terrain.RING, "Ring Band")
                //system.addRingBand(lethia_star, "misc", "rings_asteroids0", 256f, 3, Color.gray, 256f, 3650, 220f);
            }
        }

        for (entity in ogSystem.customEntities)
        {

        }

        var tear = generateTear(ogData, mirrorEntities.filter { it.originalEntity is PlanetAPI && !it.originalEntity.isStar && !(it.originalEntity as PlanetAPI).isGasGiant }.random(), 350f )

        mirrorEntities.add(tear)
        ogSystem.memoryWithoutUpdate.set("\$rat_mirrored_entities", mirrorEntities)


        val weights = LinkedHashMap<LocationType, Float>()
        //weights[LocationType.GAS_GIANT_ORBIT] = 3f
        weights[LocationType.OUTER_SYSTEM] = 10f
        var station = generateMirroredEntity(ogData, mirrorEntities, mirroredSystem, "rat_chiral_station1", "rat_chiral_station2", weights)
        mirrorEntities.add(station)
        var mirrorStation = station.mirroredEntity

        val params = FleetParamsV3(null,
            null,
            "chirality",
            3f,
            FleetTypes.PATROL_SMALL,
            80f,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )
        params.averageSMods = 1;
        params.withOfficers = true
        val defenderFleet = FleetFactoryV3.createFleet(params)
        station.mirroredEntity.memoryWithoutUpdate.set("\$defenderFleet", defenderFleet)
        Misc.setAbandonedStationMarket("chiral_station", mirrorStation)

        var storage = Misc.getStorage(mirrorStation.market) as StoragePlugin
        storage.cargo.mothballedShips.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_dune_Hull"))
        storage.cargo.mothballedShips.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_dune_Hull"))
        storage.cargo.mothballedShips.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_opera_Hull"))
        storage.cargo.mothballedShips.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_phenix_Hull"))

        addDerelictShips(computeSystemData(mirroredSystem), 1f, 5, 9, createStringPicker("chirality", 1f))

        return mirroredSystem
    }

    fun generateTear(data: StarSystemData, focus: MirrorEntity, orbitRadius: Float) : MirrorEntity
    {
        var system = data.system
        var angle = MathUtils.getRandomNumberInRange(0f, 360f)
        var ogTear = data.system.addCustomEntity("${data.system.id}_tear", "Dimensional Tear", "rat_dimensional_tear", Factions.NEUTRAL)
        var ogPlugin = ogTear.customPlugin
        data.system.addEntity(ogTear)
        ogTear.setCircularOrbit(focus.originalEntity, angle, orbitRadius, 200f)
        ogTear.addTag(RATStrings.TAG_DIMENSIONAL_TEAR)

        var mirrorTear = focus.mirroredEntity.starSystem.addCustomEntity("${focus.mirroredEntity.starSystem.id}_tear", "Dimensional Tear", "rat_dimensional_tear", Factions.NEUTRAL)
        var mirrorPlugin = mirrorTear.customPlugin
        focus.mirroredEntity.starSystem.addEntity(mirrorTear)
        mirrorTear.setCircularOrbit(focus.mirroredEntity, angle, orbitRadius, 200f)
        mirrorTear.addTag(RATStrings.TAG_DIMENSIONAL_TEAR)


        if (ogPlugin is DimensionalTearEntity)
        {
            ogPlugin.teleportLocation = mirrorTear
        }

        if (mirrorPlugin is DimensionalTearEntity)
        {
            mirrorPlugin.teleportLocation = ogTear
        }

        data.alreadyUsed.add(focus.originalEntity)
        return MirrorEntity(ogTear, mirrorTear)
    }

    fun generateMirroredEntity(data: StarSystemData, mirrorEntities: List<MirrorEntity>, mirrorSystem: StarSystemAPI, ogID: String, mirrorID: String, locations: LinkedHashMap<LocationType, Float>) : MirrorEntity
    {
        var system = data.system
        var angle = MathUtils.getRandomNumberInRange(0f, 360f)

        var locations = getLocations(StarSystemGenerator.random, data.system, data.alreadyUsed, 100f, locations)

        var ogEntity = addNonSalvageEntity(system, locations.pick(), ogID, Factions.NEUTRAL)

        var mirrorFocus = mirrorEntities.find { it.originalEntity == ogEntity.entity.orbitFocus }

        var mirrorEntity = mirrorSystem.addCustomEntity(Misc.genUID(), null, mirrorID, Factions.NEUTRAL)
        mirrorSystem.addEntity(mirrorEntity)
        mirrorEntity.setCircularOrbit(mirrorFocus!!.mirroredEntity, ogEntity.entity.circularOrbitAngle, ogEntity.entity.circularOrbitRadius, ogEntity.entity.circularOrbitPeriod)

        var mirror = MirrorEntity(ogEntity.entity, mirrorEntity)

        return mirror
    }
}

