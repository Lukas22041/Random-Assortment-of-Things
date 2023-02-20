package assortment_of_things.campaign.procgen.customThemes

import assortment_of_things.misc.RATStrings
import assortment_of_things.campaign.procgen.ProcgenUtility
import assortment_of_things.misc.RATEntities
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker
import com.fs.starfarer.api.impl.campaign.procgen.Constellation
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames.MarkovNameResult
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*


class BlackmarketThemeGenerator : BaseThemeGenerator() {


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
        return 250
    }

    override fun getThemeId(): String {
        return RATStrings.THEME_BLACKMARKET
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
            val mainCandidates = ProcgenUtility.getScoredSystemsByFilter(systems) {
                it.isBlackHole && it.planets.size >= 3 && it.system.planets.find { planet -> planet.typeId.equals(StarTypes.NEUTRON_STAR) } == null
            }
            if (mainCandidates!!.isEmpty()) continue

            //Runs the generation for the main system of the constellation
            var secondarySystems = constellation.systems.toMutableList()
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
        data.system.addTag(RATStrings.THEME_BLACKMARKET)
        data.system.addTag(RATStrings.THEME_BLACKMARKET_MAIN)
        data.system.addTag(Tags.THEME_UNSAFE)
        data.system.backgroundTextureFilename = "graphics/backgrounds/blackmarket_bg.jpg"

        //required to make ARS not spawn bases in it.
        data.system.isProcgen = false

        var faction = Global.getSector().getFaction(Factions.PIRATES)

        var beacon = ProcgenUtility.addBeacon(RATEntities.BLACKMARKET_WARNING_BEACON, data.system, Color(250, 50, 50, 30), Color(250, 50, 50, 40))
        beacon!!.addTag(RATStrings.TAG_BLACKMARKET_WARNING_BEACON)

        generateBlackmarket(data)

        var picker = WeightedRandomPicker<String>()
        picker.add(Factions.PIRATES, 1f)

        if (data.system.customEntities.find { it.id == Entities.INACTIVE_GATE } == null)
        {
            addInactiveGate(data, 1f, 0.5f, 0.5f, picker)
        }

        generatePirateBase(data, 1f, linkedMapOf(LocationType.IN_ASTEROID_BELT to 5f))
        generatePirateBase(data, 0.8f, linkedMapOf(LocationType.IN_ASTEROID_BELT to 5f))

        if (!data.resourceRich.isEmpty()) {
            addMiningStations(data, 0.6f, 1, 1, createStringPicker(Entities.STATION_MINING, 10f))
        }

        if (!data.habitable.isEmpty()) {
            addHabCenters(data, 0.50f, 1, 1, createStringPicker(Entities.ORBITAL_HABITAT, 10f))
        }


        addDerelictShips(data, 1f, 3, 10, createStringPicker(faction.id, 1f))
        addShipGraveyard(data, 0.7f, 1,1, createStringPicker(faction.id, 1f))
    }

    fun populateNonMain(data: StarSystemData)
    {
        data.system.addTag(RATStrings.THEME_BLACKMARKET)
        data.system.addTag(RATStrings.THEME_BLACKMARKET_SECONDARY)
        var faction = Global.getSector().getFaction(Factions.PIRATES)

        /*val intel = PirateBaseIntel(data.system, Factions.PIRATES, PirateBaseIntel.PirateBaseTier.TIER_3_2MODULE)
        Global.getSector().intelManager.addIntel(intel)*/
        generatePirateBase(data, 0.8f, linkedMapOf(LocationType.IN_ASTEROID_BELT to 5f, LocationType.PLANET_ORBIT to 10f))



        if (!data.resourceRich.isEmpty()) {
            addMiningStations(data, 0.3f, 1, 1, createStringPicker(Entities.STATION_MINING, 10f))
        }

        if (!data.habitable.isEmpty()) {
            addHabCenters(data, 0.20f, 1, 1, createStringPicker(Entities.ORBITAL_HABITAT, 10f))
        }

        addDerelictShips(data, 1f, 1, 4, createStringPicker(faction.id, 1f))
        addShipGraveyard(data, 0.5f, 1,1, createStringPicker(faction.id, 1f))
    }

    fun generateBlackmarket(data: StarSystemData)
    {
        var name = "Spacers Gambit"

        var planets = data.planets.filter { !it.isStar && !it.isGasGiant}
        var planet = planets.random()

        planet.name = name
        planet.customDescriptionId = "rat_blackmarketPlanet"

        var market = Global.getFactory().createMarket(Misc.genUID(), name, 3)
        market.name = name
        market.setSize(5)
        market.setHidden(true)
        market.addTag(RATStrings.TAG_BLACKMARKET_PLANET)

        //market.getMemoryWithoutUpdate().set(MEM_FLAG, true)
       // market.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, true)

        market.setPrimaryEntity(planet)
        market.getTariff().modifyFlat("rat_blackmarket_increase", 4f)

        market.setSurveyLevel(SurveyLevel.FULL)

        market.setFactionId("spacers_gambit_pirates")
        market.addCondition(Conditions.POPULATION_5)

        market.addIndustry(Industries.POPULATION)
        market.addIndustry(Industries.SPACEPORT)
        //market.addIndustry(Industries.HIGHCOMMAND)
        market.addIndustry(Industries.GROUNDDEFENSES)
        market.addIndustry(Industries.FUELPROD)
        market.addIndustry(Industries.ORBITALWORKS)
        market.addIndustry(Industries.BATTLESTATION)

        //market.addSubmarket(Submarkets.SUBMARKET_OPEN)
        //market.addSubmarket(Submarkets.SUBMARKET_BLACK)
        market.addSubmarket("rat_spacersgambit_submarket")

        market.setEconGroup(market.getId())
        market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true)

        market.reapplyIndustries()

        Global.getSector().economy.addMarket(market, true)
        planet.market = market
        planet.setFaction("spacers_gambit_pirates")
    }

    fun generatePirateBase(data: StarSystemData, chanceToAddAny: Float, weights: LinkedHashMap<LocationType, Float>)
    {
        if (random.nextFloat() >= chanceToAddAny) return
        var market = Global.getFactory().createMarket(Misc.genUID(), "Pirate Base", 3)
        market.setSize(3)
        market.setHidden(true)
        //market.getMemoryWithoutUpdate().set(MEM_FLAG, true)
        market.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, true)

        market.setFactionId(Factions.PIRATES)

        market.setSurveyLevel(SurveyLevel.FULL)

        market.setFactionId(Factions.PIRATES)
        market.addCondition(Conditions.POPULATION_3)

        market.addIndustry(Industries.POPULATION)
        market.addIndustry(Industries.SPACEPORT)
        market.addIndustry(Industries.MILITARYBASE)
        market.addIndustry(Industries.ORBITALSTATION)

        market.addSubmarket(Submarkets.SUBMARKET_OPEN)
        market.addSubmarket(Submarkets.SUBMARKET_BLACK)

        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction())

        val locs = getLocations(null, data.system, null, 100f, weights)
        val loc = locs.pick()

        if (loc == null) {
            return
        }

        val added = addNonSalvageEntity(data.system, loc, Entities.MAKESHIFT_STATION, Factions.PIRATES)

        if (added == null || added.entity == null) {
            return
        }

        var entity = added.entity

        var name = generateName()
        market.setName(name)
        entity.setName(name)

        convertOrbitWithSpin(entity, -5f)

        market.setPrimaryEntity(entity)
        entity.setMarket(market)

        entity.setSensorProfile(1f)
        entity.setDiscoverable(true)
        entity.getDetectedRangeMod().modifyFlat("gen", 5000f)

        market.setEconGroup(market.getId())
        market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true)

        market.reapplyIndustries()

        Global.getSector().economy.addMarket(market, true)

        pirateOutposts.add(market)
        /*var addedListener = false
        var script = Global.getSector().addScript {
            var fleet = Misc.getStationFleet(market)
            if (fleet != null && !addedListener)
            {
                fleet.addEventListener(PirateBaseDespawner(market))
                addedListener = true
            }
        }*/
    }

    protected fun generateName(): String? {
        MarkovNames.loadIfNeeded()
        var gen: MarkovNameResult? = null
        for (i in 0..9) {
            gen = MarkovNames.generate(null)
            if (gen != null) {
                var test = gen.name
                if (test.lowercase(Locale.getDefault()).startsWith("the ")) continue
                val p = pickPostfix()
                if (p != null && !p.isEmpty()) {
                    test += " $p"
                }
                if (test.length > 22) continue
                return test
            }
        }
        return null
    }

    protected fun pickPostfix(): String? {
        val post = WeightedRandomPicker<String>()
        post.add("Asylum")
        post.add("Astrome")
        post.add("Barrage")
        post.add("Briganderie")
        post.add("Camp")
        post.add("Cover")
        post.add("Citadel")
        post.add("Den")
        post.add("Donjon")
        post.add("Depot")
        post.add("Fort")
        post.add("Freehold")
        post.add("Freeport")
        post.add("Freehaven")
        post.add("Free Orbit")
        post.add("Galastat")
        post.add("Garrison")
        post.add("Harbor")
        post.add("Haven")
        post.add("Headquarters")
        post.add("Hideout")
        post.add("Hideaway")
        post.add("Hold")
        post.add("Lair")
        post.add("Locus")
        post.add("Main")
        post.add("Mine Depot")
        post.add("Nexus")
        post.add("Orbit")
        post.add("Port")
        post.add("Post")
        post.add("Presidio")
        post.add("Prison")
        post.add("Platform")
        post.add("Corsairie")
        post.add("Refuge")
        post.add("Retreat")
        post.add("Refinery")
        post.add("Shadow")
        post.add("Safehold")
        post.add("Starhold")
        post.add("Starport")
        post.add("Stardock")
        post.add("Sanctuary")
        post.add("Station")
        post.add("Spacedock")
        post.add("Tertiary")
        post.add("Terminus")
        post.add("Terminal")
        post.add("Tortuga")
        post.add("Ward")
        post.add("Warsat")
        return post.pick()
    }
}

