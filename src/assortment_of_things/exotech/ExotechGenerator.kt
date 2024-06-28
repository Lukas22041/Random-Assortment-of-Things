package assortment_of_things.exotech

import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import assortment_of_things.exotech.entities.ExoLightsource
import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.terrain.ExotechHyperNebula
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.levelBetween
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

object ExotechGenerator {

    fun setup() {
        setupPeople()

        generateExoship()

        generateHideout()

        generateBeacons()

        generateExoshipRemains()
    }

    fun setupPeople() {
        var data = ExoUtils.getExoData()

        //Amelie
        var amelie = data.amelie
        amelie.gender = FullName.Gender.FEMALE
        amelie.name = FullName("Amelie", "", FullName.Gender.FEMALE)
        amelie.portraitSprite = "graphics/portraits/rat_exo1.png"
        amelie.rankId = "spaceAdmiral"
        amelie.postId = "rat_exo_fleetCommander"
        amelie.setFaction("rat_exotech")

        //Xander
        var xander = data.xander
        xander.gender = FullName.Gender.MALE
        xander.name = FullName("Xander", "", FullName.Gender.MALE)
        xander.portraitSprite = "graphics/portraits/rat_exo2.png"
        xander.rankId = "specialAgent"
        xander.postId = "rat_exo_intelligence"
        xander.setFaction("rat_exotech")


    }

    fun generateExoship() {
        var systems = Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag(Tags.THEME_HIDDEN)}
        var system = systems.random()
        var location = BaseThemeGenerator.getLocations(Random(), system, MathUtils.getRandomNumberInRange(300f, 400f), linkedMapOf(
            BaseThemeGenerator.LocationType.STAR_ORBIT to 5f, BaseThemeGenerator.LocationType.OUTER_SYSTEM to 1f)).pick()

        var exoshipEntity = system.addCustomEntity("exoship_${Misc.genUID()}", "Daybreak", "rat_exoship", "rat_exotech")
        exoshipEntity.orbit = location.orbit



        var market = addMarketplace("rat_exotech",
            exoshipEntity,
            arrayListOf(),
            "Daybreak",
            4,
            arrayListOf(Conditions.OUTPOST),
            arrayListOf("rat_exoship_market"),
            arrayListOf(Industries.MEGAPORT),
            0.3f,
            false,
            false)
        market.isHidden = true

        ExoUtils.getExoData().setExoship(exoshipEntity)
        //ExoUtils.getExoData().setPlayerExoship(exoshipEntity)

        var plugin = exoshipEntity.customPlugin as ExoshipEntity
        plugin.playerModule.isPlayerOwned = false
        plugin.npcModule.isPlayerOwned = false

        market.stability.modifyFlat("rat_exoship", 10f, "Exoship")

    }

    fun generateHideout() {
        var systems = Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag(
            Tags.THEME_HIDDEN)}

        var filtered = systems.filter { system ->
            system.planets.none { Global.getSector().economy.marketsCopy.contains(it.market) } && system.planets.filter { !it.isStar }.isNotEmpty()
        }

        var system = filtered.randomOrNull()
        var planet = system!!.planets.filter { !it.isStar }.randomOrNull()

        var hideout = system.addCustomEntity("rat_hideout_${Misc.genUID()}", "Abandoned Station", "orbital_habitat", Factions.NEUTRAL)
        hideout.setCircularOrbitPointingDown(planet, MathUtils.getRandomNumberInRange(0f, 360f), 50 + hideout.radius + planet!!.radius, 90f)

        hideout.customDescriptionId = "rat_exo_hideout"
        hideout.addTag("rat_exo_hideout")

        ExoUtils.getExoData().hideout = hideout

        var fleet = spawnHideoutFleet()
        hideout.memoryWithoutUpdate.set("\$defenderFleet", fleet)
    }

    fun spawnHideoutFleet() : CampaignFleetAPI {
        var fleet = Global.getFactory().createEmptyFleet("rat_exotech", "Defectors", true)

        var officer = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson()
        officer.portraitSprite = "graphics/portraits/rat_exo3.png"

        officer.stats.level = 7
        officer.stats.setSkillLevel(Skills.HELMSMANSHIP, 2f)
        officer.stats.setSkillLevel(Skills.COMBAT_ENDURANCE, 2f)
        officer.stats.setSkillLevel(Skills.IMPACT_MITIGATION, 2f)
        officer.stats.setSkillLevel(Skills.DAMAGE_CONTROL, 2f)
        officer.stats.setSkillLevel(Skills.FIELD_MODULATION, 2f)
        officer.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
        officer.stats.setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2f)

        var arkas = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_arkas_Strike")
        arkas.fixVariant()
        arkas.variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE)

        arkas.captain = officer

        fleet.fleetData.addFleetMember(arkas)



        val params = FleetParamsV3(null,
            fleet.locationInHyperspace,
            "rat_exotech",
            5f,
            FleetTypes.PATROL_MEDIUM,
            120f,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )

        var secondFleet = FleetFactoryV3.createFleet(params)
        for (member in secondFleet.fleetData.membersListCopy) {
            fleet.fleetData.addFleetMember(member)
        }

        for (member in fleet.fleetData.membersListCopy) {
            member.repairTracker.cr = 0.7f
        }
        fleet.inflateIfNeeded()
        fleet.ai = null

        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true)
        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true)
        fleet.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true)
        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true)

        fleet.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true)


        return fleet
    }

    fun generateBeacons() {
        var beacons = 2

        var systemsUnfiltered = Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_HIDDEN)}
        var systems = Global.getSector().starSystems.filter { it.planets.filter { planet -> !planet.isStar }.isNotEmpty() && !it.hasTag(Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag(Tags.THEME_HIDDEN)}.toMutableList()


        var count = systemsUnfiltered.count()
        var level = count.toFloat().levelBetween(250f, 600f)
        var extra = 4f * level

        beacons += extra.toInt()

        for (i in 0 until beacons) {
            var system = systems.randomAndRemove()
            var planet = system.planets.filter { !it.isStar }.randomOrNull() ?: return
            var beacon = system.addCustomEntity("hypernavbeacon${Misc.genUID()}", null, "rat_hypernavigational_beacon", "rat_exotech")

            beacon.setCircularOrbitWithSpin(planet, MathUtils.getRandomNumberInRange(0f, 360f,), planet.radius + beacon.radius + 50f, 60f, 10f, 15f)
        }
    }

    fun generateExoshipRemains() {

        var data = ExoUtils.getExoData()

        var hyper = Global.getSector().hyperspace

        var width = Global.getSettings().getFloat("sectorWidth")
        var height = Global.getSettings().getFloat("sectorHeight")

        var exoshipEntity = hyper.addCustomEntity("exoship_${Misc.genUID()}", "Exoship Remains", "rat_exoship_broken", Factions.NEUTRAL)

        data.exoshipRemainsEntity = exoshipEntity

        var loc = Vector2f((width /2) * 0.55f, -height/2 - 6000)
        exoshipEntity.location.set(loc)
        exoshipEntity.facing = 35f

        val w = 100
        val h = 100

        val string = StringBuilder()
        for (y in h - 1 downTo 0) {
            for (x in 0 until w) {
                string.append("x")
            }
        }


        val nebula = hyper.addTerrain("rat_exo_nebula",
            OldBaseTiledTerrain.TileParams(string.toString(),
                w,
                h,
                "rat_terrain",
                "depths1",
                4,
                4,
                null))
        nebula.id = "rat_depths_in_hyper_${Misc.genUID()}"
        nebula.location.set(loc)

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as ExotechHyperNebula
        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(0.65f)
        editor.regenNoise()

        //Clear all but a part on the right to make it less even
        editor.clearArc(loc.x, loc.y, nebulaPlugin.range, 100000f, 0f, 360f)
        editor.clearArc(loc.x, loc.y, 0f, nebulaPlugin.centerClearRadius, 0f, 360f)

        var clearRandom = MathUtils.getRandomPointOnCircumference(loc, 250f)
        editor.clearArc(clearRandom.x, clearRandom.y, 0f, 450f, 0f, 360f)

        var lightsource = hyper.addCustomEntity("rat_lightsource_${Misc.genUID()}", "", "rat_exo_lightsource", Factions.NEUTRAL)
        lightsource.setCircularOrbit(exoshipEntity, 0f, 0f, 1000f)

        var plugin = lightsource.customPlugin as ExoLightsource
        plugin.radius = 10000f
        plugin.color = Color(130, 64, 1, 75)
    }


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
        //globalEconomy.addMarket(newMarket, withJunkAndChatter)
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