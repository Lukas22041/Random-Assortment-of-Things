package assortment_of_things.abyss

import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import assortment_of_things.abyss.terrain.SuperchargedAbyssTerrainPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

data class LinkedFracture(val fracture1: SectorEntityToken, var fracture2: SectorEntityToken)

object AbyssUtils {

    var ABYSS_COLOR = Color(255, 0, 50)
    var SUPERCHARGED_COLOR = Color(16, 154, 100)

    var SYSTEM_NAMES = arrayListOf("Tranquil", "Serene", "Storm", "Edge", "Harmony", "Decay", "Solitude", "Sorrow", "Time", "Wave",
        "Island", "Crises", "Knowledge", "Serpent", "Hope", "Death", "Perseverance", "Fear")

    var SYSTEM_TAG = "rat_abyss_system"

    var ABYSS_SYSTEMS_KEY = "\$rat_abyss_systems"


    fun addAbyssSystemToMemory(system: StarSystemAPI)
    {
        var systems = Global.getSector().memoryWithoutUpdate.get(ABYSS_SYSTEMS_KEY) as MutableList<StarSystemAPI>?
        if (systems == null)  {
            systems = ArrayList<StarSystemAPI>()
        }
        systems.add(system)
    }

    fun getAllAbyssSystems() : List<StarSystemAPI>
    {
        var systems = Global.getSector().memoryWithoutUpdate.get(ABYSS_SYSTEMS_KEY) as MutableList<StarSystemAPI>?
        if (systems == null) {
            systems = ArrayList<StarSystemAPI>()
        }
        return systems
    }

    //Sets up important tags, like "HIDDEN" to prevent the systems from being used by other mods.
    fun setupTags(system: StarSystemAPI)
    {
        system.addTag(SYSTEM_TAG)
        system.isProcgen = false
        system.addTag(Tags.THEME_HIDDEN)
        system.addTag(Tags.THEME_UNSAFE)
        system.addTag(Tags.THEME_SPECIAL)
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
    }

    fun createFractures(system1: StarSystemAPI, system2: StarSystemAPI) : LinkedFracture
    {

        var UID = Misc.genUID()
        var fracture1 = system1.addCustomEntity("abyss_fracture_" + UID + "_1", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)
        var fracture2 = system2.addCustomEntity("abyss_fracture_" + UID + "_2", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)

        linkFracture(fracture1, fracture2)
        linkFracture(fracture2, fracture1)

        return LinkedFracture(fracture1, fracture2)
    }

    fun linkFracture(fracture: SectorEntityToken, target: SectorEntityToken) {
        (fracture.customPlugin as AbyssalFracture).connectedEntity = target
    }

    fun getConnectedFracture(fracture: SectorEntityToken) : SectorEntityToken {
        return (fracture.customPlugin as AbyssalFracture).connectedEntity!!
    }

    fun getTexturesForSystem()
    {
        var pairs = mutableMapOf("Abyss1" to "depths1", "Abyss2" to "depths2")
    }

    fun generateAbyssTerrain(system: StarSystemAPI, fraction: Float) : AbyssTerrainPlugin
    {
        val w = 250
        val h = 250

        val string = StringBuilder()
        for (y in h - 1 downTo 0) {
            for (x in 0 until w) {
                string.append("x")
            }
        }

        var textureChoice = MathUtils.getRandomNumberInRange(1, 2)
        system.backgroundTextureFilename = "graphics/backgrounds/abyss/Abyss$textureChoice.jpg"

        val nebula = system.addTerrain("rat_depths", BaseTiledTerrain.TileParams(string.toString(), w, h, "rat_terrain", "depths$textureChoice", 4, 4, null))
        nebula.id = "rat_depths_${Misc.genUID()}"
        nebula.location[0f] = 0f

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as AbyssTerrainPlugin
        val editor = NebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(fraction)
        editor.regenNoise()

        return nebulaPlugin
    }

    fun getAbyssTerrainPlugin(system: StarSystemAPI) : AbyssTerrainPlugin {

        var plugin = system.terrainCopy.find { it.plugin is AbyssTerrainPlugin }?.plugin as AbyssTerrainPlugin?
        if (plugin == null)
        {
            plugin = generateAbyssTerrain(system, 0.3f)
        }
        return plugin
    }

    fun generateRareNebula(system: StarSystemAPI, location: Vector2f, range: Int, fraction: Float, clearArea: Boolean) : SuperchargedAbyssTerrainPlugin
    {
        val w = range / 100
        val h = range / 100

        val string = StringBuilder()
        for (y in h - 1 downTo 0) {
            for (x in 0 until w) {
                string.append("x")
            }
        }


        val nebula = system.addTerrain("rat_depths_siphon", BaseTiledTerrain.TileParams(string.toString(), w, h, "rat_terrain", "depths_siphon", 4, 4, null))
        nebula.id = "rat_depths_siphon_${Misc.genUID()}"
        nebula.location.set(location)

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as SuperchargedAbyssTerrainPlugin
        val editor = NebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(fraction)
        editor.regenNoise()

        if (clearArea)
        {
            clearTerrainAround(nebula, range.toFloat() + 100f)
        }

        return nebulaPlugin
    }


    fun clearTerrainAroundFractures(fracture: LinkedFracture) {
        clearTerrainAroundFractures(fracture.fracture1, fracture.fracture2)
    }

    fun clearTerrainAroundFractures(fracture1: SectorEntityToken, fracture2: SectorEntityToken) {
        clearTerrainAround(fracture1, 500f)
        clearTerrainAround(fracture2, 500f)
    }

    fun clearTerrainAround(entity: SectorEntityToken, radius: Float)
    {
        var nebulaPlugin = getAbyssTerrainPlugin(entity.starSystem)
        val editor = NebulaEditor(nebulaPlugin)

        editor.clearArc(entity.location.x, entity.location.y, 0f, radius, 0f, 360f)
        editor.clearArc(entity.location.x, entity.location.y, 0f, radius, 0f, 360f, 0.25f)
    }

    fun getSiphonPerDay() : Float
    {
        var membersWithSiphon = Global.getSector().playerFleet.fleetData.membersListCopy.filter { it.variant.hasHullMod("rat_abyss_siphon") }
        var siphon = 0f

        for (member in membersWithSiphon)
        {
            siphon += when(member.hullSpec.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 5f
                ShipAPI.HullSize.DESTROYER -> 10f
                ShipAPI.HullSize.CRUISER -> 20f
                ShipAPI.HullSize.CAPITAL_SHIP -> 50f
                else -> 0f
            }
        }

        return siphon
    }

    fun getMaxShielding() : Float
    {
        var membersWithShielding = Global.getSector().playerFleet.fleetData.membersListCopy.filter { it.variant.hasHullMod("rat_abyss_shielding") }
        var shielding = 0f

        for (member in membersWithShielding)
        {
            shielding += when(member.hullSpec.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 20f
                ShipAPI.HullSize.DESTROYER -> 30f
                ShipAPI.HullSize.CRUISER -> 50f
                ShipAPI.HullSize.CAPITAL_SHIP -> 100f
                else -> 0f
            }
        }

        return shielding
    }

    fun getLossPerDay() : Float
    {
        var membersWithShielding = Global.getSector().playerFleet.fleetData.membersListCopy
        var cost = 0f

        for (member in membersWithShielding)
        {
            cost += when(member.hullSpec.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 0.05f
                ShipAPI.HullSize.DESTROYER -> 0.1f
                ShipAPI.HullSize.CRUISER -> 0.3f
                ShipAPI.HullSize.CAPITAL_SHIP -> 0.5f
                else -> 0f
            }
        }

        return cost
    }


}