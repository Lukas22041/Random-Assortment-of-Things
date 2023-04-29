package assortment_of_things.campaign.procgen.customThemes

import assortment_of_things.campaign.plugins.entities.DimensionalGate
import assortment_of_things.campaign.procgen.ProcgenUtility
import assortment_of_things.scripts.ChiralBaseFleetManager
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
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
        return 1000010
    }

    override fun getThemeId(): String {
        return RATTags.CHIRAL_THEME_ID
    }

    override fun generateForSector(context: ThemeGenContext?, allowedSectorFraction: Float) {
        val total: Float = (context!!.constellations.size - context.majorThemes.size).toFloat() * allowedSectorFraction
        if (total <= 0) return



        //gets available constellations that havent been used yet
        //val constellations: List<Constellation?>? = ProcgenUtility.getSortedAvailableConstellations(context, false, Vector2f(), null)
        //Collections.reverse(constellations)
        val constellations = context.constellations

        for (constellation in constellations!!)
        {
            val systems: List<StarSystemData> = constellation!!.systems.map { computeSystemData(it) }

            //looks for a system with blackholes, if none are found it continues to the next constellation
            var allowedStars = listOf(StarTypes.WHITE_DWARF, StarTypes.RED_DWARF, StarTypes.BROWN_DWARF, StarTypes.ORANGE, StarTypes.YELLOW)
            val mainCandidates = ProcgenUtility.getScoredSystemsByFilter(systems) {
                it.system.hasTag(Tags.THEME_MISC)
                && !it.system.hasTag(RATTags.THEME_OUTPOST)
                && it.system.center.isStar && allowedStars.contains((it.system.center as PlanetAPI).typeId)
                && it.system.terrainCopy.find { it.type == Terrain.ASTEROID_BELT } != null && it.planets.size >= 3
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
            main.system.setBackgroundOffset(2f, 2f)
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
        data.system.addTag(RATTags.THEME_CHIRAL)
        data.system.addTag(RATTags.THEME_CHIRAL_MAIN)
        data.system.addTag(Tags.THEME_UNSAFE)

        generateMirrorCopy(data)
    }

    fun populateNonMain(data: StarSystemData)
    {
        data.system.addTag(RATTags.THEME_CHIRAL)
        data.system.addTag(RATTags.THEME_CHIRAL_SECONDARY)

        /*val special = data.isBlackHole || data.isNebula || data.isPulsar
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
        addShipGraveyard(data, 0.6f, 1,1, createStringPicker(Factions.HEGEMONY, 1f, Factions.TRITACHYON, 1f, Factions.PERSEAN, 1f))*/
    }

    fun generateMirrorCopy(ogData: StarSystemData) : StarSystemAPI
    {
        var ogSystem = ogData.system
        var mirroredSystem = Global.getSector().createStarSystem("${ogSystem.baseName}")
        mirroredSystem.location.set(ogSystem.location)
        mirroredSystem.doNotShowIntelFromThisLocationOnMap = true
        mirroredSystem.backgroundTextureFilename = "graphics/backgrounds/chiral_bg.jpg"
        mirroredSystem.addTag(RATTags.THEME_CHIRAL_COPY)
        mirroredSystem.addTag(Tags.THEME_HIDDEN)
        mirroredSystem.addTag(Tags.THEME_UNSAFE)
        mirroredSystem.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
        mirroredSystem.isProcgen = false
        mirroredSystem.generateAnchorIfNeeded()

       /* if (mirroredSystem is BaseLocation)
        {
            var background = mirroredSystem.background
            ReflectionUtils.set("hyperspaceMode", background, true)
            ReflectionUtils.set("warpngRenderer", background, WarpingSpriteRenderer(16, 16))
        }*/

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
            var ogFocus = mirrorEntities.find { ogMoon.orbitFocus == it.originalEntity && !it.originalEntity.isStar}
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
                if (plugin.ringParams == null) continue
                mirroredSystem.addRingBand(matchingFocus.mirroredEntity,  "misc", "rings_dust0", 256f, 1, mirroredStar.lightColor, 256f, plugin.params.middleRadius, plugin.params.maxOrbitDays,
                    Terrain.RING, "Ring Band")
            }

            if (plugin is RingSystemTerrainPlugin)
            {
                if (plugin.params == null) continue
                if (plugin.ringParams == null) continue

                mirroredSystem.addRingBand(matchingFocus.mirroredEntity,  "misc", "rings_dust0", 256f, 1, Color.white, 256f, plugin.ringParams.middleRadius, plugin.entity.circularOrbitPeriod,
                    Terrain.RING, "Ring Band")
            }
        }

        var tear = generateGate(ogData, mirrorEntities.filter { it.originalEntity is PlanetAPI && !it.originalEntity.isStar && !(it.originalEntity as PlanetAPI).isGasGiant }.random(), 350f )

        mirrorEntities.add(tear)
        ogSystem.memoryWithoutUpdate.set("\$rat_mirrored_entities", mirrorEntities)


        val weights = LinkedHashMap<LocationType, Float>()
        weights[LocationType.GAS_GIANT_ORBIT] = 30f
        weights[LocationType.PLANET_ORBIT] = 100f
       // weights[LocationType.NEAR_STAR] = 1f
        var locations = getLocations(StarSystemGenerator.random, ogData.system, ogData.alreadyUsed, 100f, weights)
        if (locations == null || locations.isEmpty)
        {
            var locations = getLocations(StarSystemGenerator.random, ogData.system, null, 100f, weights)
        }

        var triResearchStation: AddedEntity? = null
        if (locations != null && !locations.isEmpty)
        {
            triResearchStation = addNonSalvageEntity(ogData.system, locations.pick(), "rat_chiral_station1", Factions.NEUTRAL)
            triResearchStation.entity.addTag(RATTags.TAG_CHIRAL_STATION1)
        }

        if (triResearchStation == null)
        {
            var plugin = tear.originalEntity.customPlugin as DimensionalGate
            plugin.active = true
            (plugin.teleportLocation!!.customPlugin as DimensionalGate).active = true

        }

        var calcData = BaseThemeGenerator.computeSystemData(mirroredSystem)
        calcData.alreadyUsed.add(tear.mirroredEntity.orbitFocus)

        generateBase(mirroredSystem, calcData)

        addDerelictShips(computeSystemData(mirroredSystem), 1f, 5, 9, createStringPicker("chirality", 1f))

        generateHullmodStation(calcData)


        return mirroredSystem
    }

    fun generateHullmodStation(data: StarSystemData)
    {
        val weights = LinkedHashMap<LocationType, Float>()
        weights[LocationType.PLANET_ORBIT] = 3f
        weights[LocationType.IN_ASTEROID_FIELD] = 3f
        weights[LocationType.NEAR_STAR] = 1f
        weights[LocationType.IN_RING] = 3f
        weights[LocationType.IN_ASTEROID_BELT] = 10f
        var locations = getLocations(StarSystemGenerator.random, data.system, data.alreadyUsed, 100f, weights)
        var station = addNonSalvageEntity(data.system, locations.pick(), "rat_chiral_station1", Factions.NEUTRAL)
        station.entity.addTag(RATTags.TAG_CHIRAL_STATION2)
        station.entity.name = "Unknown Station"
        station.entity.customDescriptionId = "rat_chiral_station2"
    }

    fun generateGate(data: StarSystemData, focus: MirrorEntity, orbitRadius: Float) : MirrorEntity
    {
        var system = data.system
        var angle = MathUtils.getRandomNumberInRange(0f, 360f)
        var ogTear = data.system.addCustomEntity("${data.system.id}_tear", "Strange Gate", "rat_dimensional_gate", Factions.NEUTRAL)
        var ogPlugin = ogTear.customPlugin
        data.system.addEntity(ogTear)
        ogTear.setCircularOrbit(focus.originalEntity, angle, orbitRadius, 200f)
        ogTear.addTag(RATTags.TAG_DIMENSIONAL_GATE)
        var mirrorTear = focus.mirroredEntity.starSystem.addCustomEntity("${focus.mirroredEntity.starSystem.id}_tear", "Strange Gate", "rat_dimensional_gate", Factions.NEUTRAL)
        var mirrorPlugin = mirrorTear.customPlugin
        focus.mirroredEntity.starSystem.addEntity(mirrorTear)
        mirrorTear.setCircularOrbit(focus.mirroredEntity, angle, orbitRadius, 200f)
        mirrorTear.addTag(RATTags.TAG_DIMENSIONAL_GATE)

        if (Global.getSettings().allShipHullSpecs.find { it.hullId == "nebula" } != null)
        {
            var derelictNebulaParams = DerelictShipEntityPlugin.createVariant("nebula_Standard", Random(), 0f)
            val nebulaWreck: CustomCampaignEntityAPI = mirrorTear.starSystem.addCustomEntity(null, SalvageEntityGeneratorOld.getSalvageSpec(Entities.WRECK).getNameOverride()
                , Entities.WRECK, Factions.NEUTRAL, derelictNebulaParams)

            nebulaWreck.setCircularOrbit(mirrorTear.orbitFocus, MathUtils.getRandomNumberInRange(0f, 360f), 300f, 100f)
            nebulaWreck.addTag(RATTags.TAG_CHIRAL_NEBULA)
        }

        if (ogPlugin is DimensionalGate)
        {
            ogPlugin.teleportLocation = mirrorTear
        }

        if (mirrorPlugin is DimensionalGate)
        {
            mirrorPlugin.teleportLocation = ogTear
        }

        data.alreadyUsed.add(focus.originalEntity)
        return MirrorEntity(ogTear, mirrorTear)
    }

    fun generateBase(mirroredSystem: StarSystemAPI, calcData: StarSystemData)
    {
        val fleet = FleetFactoryV3.createEmptyFleet("chirality", FleetTypes.BATTLESTATION, null)
        val member: FleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "station2_hightech_Standard")
        fleet.fleetData.addFleetMember(member)

        fleet.name = "Defense Station"
        //fleet.customDescriptionId = ""
        //Behaviour Memory keys
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE] = true

        fleet.addTag(Tags.NEUTRINO_HIGH)
        fleet.isStationMode = true

        fleet.clearAbilities()
        fleet.addAbility(Abilities.TRANSPONDER)
        fleet.getAbility(Abilities.TRANSPONDER).activate()
        fleet.detectedRangeMod.modifyFlat("gen", 1000f)

        fleet.ai = null

        member.repairTracker.cr = member.repairTracker.maxCR
        val activeFleets = ChiralBaseFleetManager(fleet,
            3f,
            3,
            5,
            25f,
            70,
            85,
            "chirality")

        val baseLocs = LinkedHashMap<LocationType, Float>()
        baseLocs[LocationType.IN_ASTEROID_BELT] = 10f
        baseLocs[LocationType.IN_ASTEROID_FIELD] = 5f
        //weights[LocationType.PLANET_ORBIT] = 5f
        baseLocs[LocationType.STAR_ORBIT] = 1f
        var locs = getLocations(StarSystemGenerator.random, calcData.system, calcData.alreadyUsed, 100f, baseLocs)
        mirroredSystem.addEntity(fleet)

        fleet.orbit = locs.pick().orbit

        mirroredSystem.addScript(activeFleets)
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

