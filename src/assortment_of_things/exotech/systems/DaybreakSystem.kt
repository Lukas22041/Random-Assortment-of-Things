package assortment_of_things.exotech.systems

import assortment_of_things.misc.RATAccretionDiskGen
import assortment_of_things.misc.ReflectionUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor
import com.fs.starfarer.api.impl.campaign.procgen.StarAge
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAllPlanetsSurveyed
import java.awt.Color

object DaybreakSystem {


    fun generate() : StarSystemAPI {

        var system = Global.getSector().createStarSystem("Daybreak")

        system.lightColor = Color(235, 140, 52, 150)
        system.backgroundTextureFilename = "graphics/backgrounds/exo/rat_daybreak_system.jpg"
        system.isProcgen = false
        system.addTag(Tags.THEME_CORE_POPULATED)

        //Blackhole
        var star = system.initStar("rat_daybreak_main_star", "rat_exo_black_hole", 150f, 0f)
        val starData = Global.getSettings().getSpec(StarGenDataSpec::class.java, star.spec.planetType, false) as StarGenDataSpec
        val corona = star.radius * (starData.coronaMult + starData.coronaVar) * 1.5f
        val eventHorizon = system.addTerrain(Terrain.EVENT_HORIZON,
            CoronaParams(star.radius + corona,
                (star.radius + corona) / 2f,
                star,
                -4f,
                (starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * StarSystemGenerator.random.nextFloat()) as Float,
                starData.getCrLossMult()))

        eventHorizon.setCircularOrbit(star, 0f, 0f, 100f)


        // Accreation Disk

        RATAccretionDiskGen().generate(star, 3500f)

        //Generators

        var angle = 0f
        var generators = 3
        for (i in 0 until generators) {
            var generator = system.addCustomEntity("rat_exo_collector_$i", "Plasma Generator", "rat_exo_plasma_generator", "rat_exotech")
            generator.setCircularOrbitPointingDown(star, angle, 300f, 90f)
            angle += 360 / generators
        }

        //Disk Station

        var daybreakStation = system.addCustomEntity("rat_daybreak_disk_station", "Daybreak Station", "station_side06", "rat_exotech")
        daybreakStation.customDescriptionId = "rat_exo_daybreak_station"
        daybreakStation.setCircularOrbit(star, MathUtils.getRandomNumberInRange(0f, 360f), 2100f, -120f)
        addMarketplace("rat_exotech", daybreakStation, ArrayList(), "Daybreak Station", 4,
            arrayListOf(),
            arrayListOf(Submarkets.SUBMARKET_OPEN, Submarkets.SUBMARKET_BLACK),
            arrayListOf(Industries.POPULATION, Industries.SPACEPORT, Industries.LIGHTINDUSTRY, Industries.WAYSTATION, Industries.REFINING, Industries.ORBITALSTATION, Industries.GROUNDDEFENSES, Industries.PATROLHQ),
            0.3f, true, false)


        //Unfinished Exoship

        var unfinishedExoship = system.addCustomEntity("rat_unfinished_exoship", "Incomplete Exoship", "rat_unfinished_exoship", "rat_exotech")
        unfinishedExoship.setCircularOrbit(star, MathUtils.getRandomNumberInRange(0f, 360f), 2700f, -140f)


        //First Jumpoint

        var jumppoint = Global.getFactory().createJumpPoint("rat_daybreak_jumppoint", "")
        system.addEntity(jumppoint)
        jumppoint.setCircularOrbit(star, MathUtils.getRandomNumberInRange(0f, 360f), 3800f, 180f)

        //Nova

        var arcPlanet = system.addPlanet("rat_daybreak_planet", star, "Arc", "rat_exo_terran", MathUtils.getRandomNumberInRange(0f, 360f),
            150f, 5500f, 240f)

        arcPlanet.customDescriptionId = "rat_exo_arc_planet"

        var arcMarket = addMarketplace("rat_exotech", arcPlanet, ArrayList(), "Arc", 6,
        arrayListOf(Conditions.HABITABLE, Conditions.FARMLAND_BOUNTIFUL, Conditions.ORE_RICH, Conditions.RARE_ORE_ABUNDANT, Conditions.ORGANICS_COMMON),
        arrayListOf(Submarkets.SUBMARKET_OPEN, Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_BLACK),
        arrayListOf(Industries.POPULATION, Industries.MEGAPORT, "rat_orbital_receivers", Industries.FARMING, Industries.MINING, Industries.WAYSTATION, Industries.STARFORTRESS_HIGH, Industries.HEAVYBATTERIES, Industries.ORBITALWORKS, Industries.HIGHCOMMAND),
        0.3f, true, true)

        arcMarket.getIndustry(Industries.MEGAPORT).specialItem = SpecialItemData("fullerene_spool", null)

        //Belt
        var beltRadius = 7000f
        system.addAsteroidBelt(star, 100, beltRadius, 256f, 380f, 380f)
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color(255, 255, 255), 256f, beltRadius, 380f)
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 3, Color(255, 255, 255), 256f, beltRadius, 380f)
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color(255, 255, 255), 256f, beltRadius, 380f)



        //Commy Relay
        var relay = system.addCustomEntity("rat_daybreak_relay", "Comm-Relay", "comm_relay", "rat_exotech")
        relay.setCircularOrbit(star, MathUtils.getRandomNumberInRange(0f, 360f), 8000f, 400f)



        //Location
        system.location.set(Vector2f(22000f, 26500f))
        system.autogenerateHyperspaceJumpPoints(true, false)
        system.generateAnchorIfNeeded()


        //Hyperspace Cleanup
        val hyper = Misc.getHyperspaceTerrain().plugin as HyperspaceTerrainPlugin
        val editor = NebulaEditor(hyper)
        editor.clearArc(system.location.x, system.location.y, 0f, 1000f, 0f, 360f)
        editor.clearArc(system.location.x,system.location.y, 0f, 1000f, 0f, 360f, 0.25f)

        //Finish
        system.setAllPlanetsSurveyed(true)

        return system
    }

    //Utility Method from Tahlan-Shipworks by NiaTahl
    fun addMarketplace(factionID: String?, primaryEntity: SectorEntityToken, connectedEntities: ArrayList<SectorEntityToken>?, name: String?,
        size: Int, marketConditions: ArrayList<String>, submarkets: ArrayList<String>?, industries: ArrayList<String>, tarrif: Float,
        freePort: Boolean, withJunkAndChatter: Boolean): MarketAPI {

        val globalEconomy = Global.getSector().economy
        val planetID = primaryEntity.id
        val marketID = planetID + "_market"
        val newMarket = Global.getFactory().createMarket(marketID, name, size)
        newMarket.factionId = factionID
        newMarket.primaryEntity = primaryEntity
        newMarket.tariff.modifyFlat("generator", tarrif)

        //Adds submarkets
        if (null != submarkets) {
            for (market in submarkets) {
                newMarket.addSubmarket(market)
            }
        }

        //Adds market conditions
        for (condition in marketConditions) {
            newMarket.addCondition(condition)
        }

        //Add market industries
        for (industry in industries) {
            newMarket.addIndustry(industry)
        }

        //Sets us to a free port, if we should
        newMarket.isFreePort = freePort

        //Adds our connected entities, if any
        if (null != connectedEntities) {
            for (entity in connectedEntities) {
                newMarket.connectedEntities.add(entity)
            }
        }
        globalEconomy.addMarket(newMarket, withJunkAndChatter)
        primaryEntity.market = newMarket
        primaryEntity.setFaction(factionID)
        if (null != connectedEntities) {
            for (entity in connectedEntities) {
                entity.market = newMarket
                entity.setFaction(factionID)
            }
        }

        //Finally, return the newly-generated market
        return newMarket
    }
}

