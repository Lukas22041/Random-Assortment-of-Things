package assortment_of_things.campaign.procgen.customThemes

import assortment_of_things.campaign.procgen.ProcgenUtility
import assortment_of_things.strings.RATEntities
import assortment_of_things.strings.RATTags
import assortment_of_things.scripts.FactionBaseFleetManager
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.MusicPlayerPlugin
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.Faction
import lunalib.lunaDelegates.LunaMemory
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*

class OutpostThemeGenerator : BaseThemeGenerator() {

    companion object {
        @JvmStatic
        var minOutpostConstellations = 2
        @JvmStatic
        var maxOutpostConstellations = 3

        var blacklistedFactions = mutableListOf(Factions.PLAYER, Factions.PIRATES, Factions.LUDDIC_PATH, Factions.INDEPENDENT)
    }

    var addedTrainingStation = false

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
        return 1000005
    }

    override fun getThemeId(): String {
        return RATTags.OUTPOST_THEME_ID
    }

    override fun generateForSector(context: ThemeGenContext?, allowedSectorFraction: Float) {
        val total: Float = (context!!.constellations.size - context.majorThemes.size).toFloat() * allowedSectorFraction
        if (total <= 0) return

        var num = StarSystemGenerator.getNormalRandom(minOutpostConstellations.toFloat(), maxOutpostConstellations.toFloat()).toInt()

        if (num > total) num = total.toInt()

        var constellations = context!!.constellations
        var factions: MutableList<String> = ArrayList()
        factions.addAll(listOf(Factions.HEGEMONY, Factions.TRITACHYON, Factions.LUDDIC_CHURCH, Factions.DIKTAT, Factions.PERSEAN))

        var count = 0
        for (constellation in constellations!!)
        {
            if (count >= num) break
            if (factions.isEmpty()) break

            val systems: List<StarSystemData> = constellation!!.systems.map { computeSystemData(it) }

            val mainCandidates = ProcgenUtility.getScoredSystemsByFilter(systems) {
                    it.system.hasTag(Tags.THEME_MISC)
                    && it.planets.size >= 2
                    && it.system.planets.find { planet -> planet.typeId.equals(StarTypes.NEUTRON_STAR) } == null
            }
            if (mainCandidates!!.isEmpty()) continue


            var faction = Global.getSector().getFaction(factions.random())
            //factions.remove(faction.id)
            Global.getLogger(this::class.java).info("RAT: Creating \"Outpost\" theme with faction ${faction.id}")


            var main = mainCandidates.get(0)
            populateMain(main, faction)
            count++
        }
    }

    fun populateMain(data: StarSystemData, faction: FactionAPI)
    {
        data.system.addTag(RATTags.THEME_OUTPOST)
        data.system.addTag(RATTags.THEME_OUTPOST_MAIN)
        data.system.addTag(Tags.THEME_UNSAFE)

        //required to make ARS not spawn bases in it.
        data.system.isProcgen = false

        var beacon = ProcgenUtility.addBeacon(RATEntities.OUTPOST_WARNING_BEACON, data.system, faction.baseUIColor, faction.brightUIColor)
        beacon!!.setFaction(faction.id)
        beacon.addTag(RATTags.TAG_OUTPOST_WARNING_BEACON)

        generateBase(data, faction)

        addResearchStations(data, 0.85f, 1, 1, createStringPicker(Entities.STATION_RESEARCH, 1f))
        addCaches(data, 0.65f, 1,2, createStringPicker(
            Entities.SUPPLY_CACHE, 4f,
            Entities.SUPPLY_CACHE_SMALL, 10f,
            Entities.EQUIPMENT_CACHE, 4f,
            Entities.EQUIPMENT_CACHE_SMALL, 10f))

        if (!data.resourceRich.isEmpty()) {
            addMiningStations(data, 0.3f, 1, 1, createStringPicker(Entities.STATION_MINING, 10f))
        }

        if (!data.habitable.isEmpty()) {
            addHabCenters(data, 0.50f, 1, 1, createStringPicker(Entities.ORBITAL_HABITAT, 10f))
        }

        addDerelictShips(data, 1f, 2, 6, createStringPicker(faction.id, 1f))
        addShipGraveyard(data, 0.5f, 1,1, createStringPicker(faction.id, 1f))

        addCommRelay(data, 0.8f)

        if (!addedTrainingStation)
        {
            generateTrainingStation(data, faction)
        }
    }

    fun generateTrainingStation(data: StarSystemData, faction: FactionAPI)
    {
        val weights = LinkedHashMap<LocationType, Float>()
        //weights[LocationType.GAS_GIANT_ORBIT] = 3f
        weights[LocationType.PLANET_ORBIT] = 10f
        weights[LocationType.NEAR_STAR] = 1f
        val locations = getLocations(StarSystemGenerator.random, data.system, data.alreadyUsed, 100f, weights)
        val location = locations.pick()

        var added = addNonSalvageEntity(data.system, location, "station_side03", faction.id) ?: return
        var station = added.entity

        addedTrainingStation = true
        data.system.addTag(RATTags.TAG_OUTPOST_TRAINING)
        station.addTag(RATTags.TAG_OUTPOST_TRAINING_FACILITY)

        station.name = "Training Facility"
        station.customDescriptionId = "rat_training_facility"


        var params = FleetParamsV3(null, station.location, Factions.DERELICT, 1f, FleetTypes.PATROL_MEDIUM,
            MathUtils.getRandomNumberInRange(80f, 120f),
            0f,0f,0f,0f,1f, 1f)
        params.averageSMods = 1
        var defenderFleet = FleetFactoryV3.createFleet(params)

        station.memoryWithoutUpdate.set("\$defenderFleet", defenderFleet)
        var seed: Long? by LunaMemory("rat_skill_seed", Misc.genRandomSeed(), station.memoryWithoutUpdate)
        seed = Misc.genRandomSeed()

    }

    fun generateBase(data: StarSystemData, faction: FactionAPI)
    {
        val weights = LinkedHashMap<LocationType, Float>()
        //weights[LocationType.GAS_GIANT_ORBIT] = 3f
        weights[LocationType.PLANET_ORBIT] = 10f
        val locations = getLocations(StarSystemGenerator.random, data.system, data.alreadyUsed, 100f, weights)
        val location = locations.pick()

        if (location != null) {

            var shipVariantID = when (faction.id)
            {
                Factions.HEGEMONY -> "station2_Standard"
                Factions.TRITACHYON -> "station2_hightech_Standard"
                Factions.PERSEAN -> "station2_midline_Standard"
                Factions.LUDDIC_CHURCH -> "station2_midline_Standard"
                Factions.DIKTAT -> "station2_midline_Standard"
                else -> "station2_Standard"
            }

            val fleet = FleetFactoryV3.createEmptyFleet(faction.id, FleetTypes.BATTLESTATION, null)
            val member: FleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipVariantID)
            fleet.fleetData.addFleetMember(member)

            fleet.name = "Outpost Defense Station"
            fleet.customDescriptionId = "rat_outpost_defense_station"

            //Behaviour Memory keys
            fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = true
            fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true
            fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE] = true
            fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_LOW_REP_IMPACT] = true

            fleet.addTag(Tags.NEUTRINO_HIGH)
            fleet.isStationMode = true

            fleet.clearAbilities()
            fleet.addAbility(Abilities.TRANSPONDER)
            fleet.getAbility(Abilities.TRANSPONDER).activate()
            fleet.detectedRangeMod.modifyFlat("gen", 1000f)

            fleet.ai = null

            member.repairTracker.cr = member.repairTracker.maxCR
            val activeFleets = FactionBaseFleetManager(fleet,
                3f,
                2,
                4,
                25f,
                50,
                70,
                faction.id)

            data.system.addEntity(fleet)
            data.system.addScript(activeFleets)

            var random = StarSystemGenerator.random

            if (location.orbit != null) {
                fleet.orbit = location.orbit
                location.orbit.setEntity(fleet)
                location.orbit.focus.addTag(RATTags.TAG_OUTPOST_PLANET)
                location.orbit.focus.customDescriptionId = "rat_outpost_planet"

                var entity = location.orbit.focus

               /* val params = FleetParamsV3(null,
                    null,
                    faction.id,
                    3f,
                    FleetTypes.PATROL_LARGE,
                    MathUtils.getRandomNumberInRange(180f, 200f),  // combatPts
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
                entity.memoryWithoutUpdate.set("\$defenderFleet", defenderFleet)*/
                entity.memoryWithoutUpdate.set("\$defenderStation", fleet)

                val planet = fleet!!.orbitFocus
                data.alreadyUsed.add(planet)
                planet.setFaction(faction.id)
                convertOrbitPointingDown(fleet)

            } else {
                fleet.orbit = null
                fleet.location.set(location.location)
            }
        }
    }
}