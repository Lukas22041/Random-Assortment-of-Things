package assortment_of_things.exotech

import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import assortment_of_things.exotech.entities.ExoLightsource
import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.terrain.ExotechHyperNebula
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
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

        generateExoshipRemains()
    }

    fun setupPeople() {
        var data = ExoUtils.getExoData()

        //Amelie
        var amelie = data.amelie
        amelie.gender = FullName.Gender.FEMALE
        amelie.name = FullName("Amelie", "", FullName.Gender.FEMALE)
        amelie.portraitSprite = "graphics/portraits/rat_exo1.png"
        amelie.postId = "rat_exo_fleetCommander"
        amelie.setFaction("rat_exotech")

        //Xander
        var xander = data.xander
        xander.gender = FullName.Gender.MALE
        xander.name = FullName("Xander", "", FullName.Gender.MALE)
        xander.portraitSprite = "graphics/portraits/rat_exo2.png"
        xander.postId = "rat_exo_intelligence"
        xander.setFaction("rat_exotech")


    }

    fun generateExoship() {
        var systems = Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasBlackHole() && !it.hasPulsar() && !it.hasTag(
            Tags.THEME_HIDDEN)}
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
            arrayListOf(Submarkets.SUBMARKET_OPEN),
            arrayListOf(Industries.MEGAPORT, Industries.WAYSTATION, Industries.PATROLHQ, Industries.ORBITALWORKS),
            0.3f,
            false,
            false)
        market.isHidden = true

        ExoUtils.getExoData().setExoship(exoshipEntity)
        //ExoUtils.getExoData().setPlayerExoship(exoshipEntity)

        var plugin = exoshipEntity.customPlugin as ExoshipEntity
        plugin.playerModule.isPlayerOwned = false
        plugin.npcModule.isPlayerOwned = false

    }


    fun generateExoshipRemains() {

        var hyper = Global.getSector().hyperspace

        var width = Global.getSettings().getFloat("sectorWidth")
        var height = Global.getSettings().getFloat("sectorHeight")

        var exoshipEntity = hyper.addCustomEntity("exoship_${Misc.genUID()}", "Exoship Remains", "rat_exoship_broken", Factions.NEUTRAL)

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