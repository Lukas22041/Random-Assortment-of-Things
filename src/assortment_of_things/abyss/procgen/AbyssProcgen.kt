package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.LinkedFracture
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.misc.AbyssBackgroundWarper
import assortment_of_things.abyss.scripts.AbyssalDefendingFleetManager
import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import assortment_of_things.abyss.terrain.AbyssalDarknessTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

object AbyssProcgen {

    var SYSTEM_NAMES = arrayListOf("Tranquilility", "Serenity", "Storms", "Harmony", "Decay", "Solitude", "Sorrow", "Time", "Epidemics",
        "Crises", "Knowledge", "Serpents", "Hope", "Death", "Perseverance", "Fear", "Cold", "Clouds", "Luxury", "Hatred", "Dreams", "Honor", "Trust", "Success", "Joy")


    //Sets up important tags, like "HIDDEN" to prevent the systems from being used by other mods.
    fun setupSystem(system: StarSystemAPI, type: BaseAbyssType, depth: AbyssDepth, final: Boolean = false)
    {
        var data = AbyssUtils.getSystemData(system)
        data.depth = depth

        AbyssUtils.getAbyssData().systemsData.add(data)

        system.location.set(AbyssUtils.getAbyssData().hyperspaceLocation)
        system.addTag(AbyssUtils.SYSTEM_TAG)
        system.initNonStarCenter()
        system.generateAnchorIfNeeded()
        system.isProcgen = false
        system.addTag(Tags.THEME_HIDDEN)
        system.addTag(Tags.THEME_UNSAFE)
        system.addTag(Tags.THEME_SPECIAL)
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
        system.addTag("do_not_show_stranded_dialog")

        if (data.depth == AbyssDepth.Deep) {
            system.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_music_abyss2")
        }
        else {
            system.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_music_abyss")
        }

        system.isEnteredByPlayer = false
        system.addCustomEntity("${system.name}", "", "rat_abyss_border", Factions.NEUTRAL)


        type.setupColor(data)
        system.lightColor = data.getDarkColor()

        if (!final) {
            var warper = AbyssBackgroundWarper(system, 8, 0.33f)
            warper.overwriteColor = data.getDarkColor()
        }



        AbyssProcgen.generateAbyssTerrain(system, type.getTerrainFraction())
        AbyssProcgen.generateAbyssDarkness(system)

        if (final) {
            system.backgroundTextureFilename = "graphics/backgrounds/abyss/rat_abyss_black.jpg"
        }

    }

    fun addAbyssParticles(system: StarSystemAPI) {
        system.addCustomEntity("rat_abyss_particle_manager_${Misc.genUID()}", "", "rat_abyss_particle_spawner", Factions.NEUTRAL)
    }

    fun addAbyssStorm(system: StarSystemAPI) {
        system.addCustomEntity("rat_abyss_storm_manager_${Misc.genUID()}", "", "rat_abyss_storm_spawner", Factions.NEUTRAL)
        system.addTerrain("rat_abyss_ionicstorm", null)
    }

    fun createFractures(system1: LocationAPI, system2: LocationAPI) : LinkedFracture
    {
        var UID = Misc.genUID()
        var fracture1 = system1.addCustomEntity("abyss_fracture_" + UID + "_1", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)
        var fracture2 = system2.addCustomEntity("abyss_fracture_" + UID + "_2", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)

        if (!system1.isHyperspace && !system2.isHyperspace) {
            AbyssUtils.getSystemData(system1 as StarSystemAPI).neighbours.add(system2 as StarSystemAPI)
            AbyssUtils.getSystemData(system2).neighbours.add(system1)
        }

        linkFracture(fracture1, fracture2)
        linkFracture(fracture2, fracture1)

        if (!system1.isHyperspace) {
            var plugin1 = addLightsource(fracture1, 10000f)
            plugin1.color = AbyssUtils.getSystemData(system1 as StarSystemAPI).getColor().setAlpha(50)
        }
        else {
            var plugin1 = addLightsource(fracture1, 15000f)
            plugin1.color = AbyssUtils.ABYSS_COLOR.setAlpha(40)
        }
        var plugin2 = addLightsource(fracture2, 10000f)
        plugin2.color = AbyssUtils.getSystemData(system2 as StarSystemAPI).getColor().setAlpha(50)


        return LinkedFracture(fracture1, fracture2)
    }


    fun linkFracture(fracture: SectorEntityToken, target: SectorEntityToken) {
        (fracture.customPlugin as AbyssalFracture).connectedEntity = target
    }

    fun getConnectedFracture(fracture: SectorEntityToken) : SectorEntityToken {
        return (fracture.customPlugin as AbyssalFracture).connectedEntity!!
    }

    fun generateAbyssDarkness(system: StarSystemAPI) : AbyssalDarknessTerrainPlugin {
        val darkness = system.addTerrain("rat_depths_darkness", null)
        return (darkness as CampaignTerrainAPI).plugin as AbyssalDarknessTerrainPlugin
    }

    fun getAbyssDarknessTerrainPlugin(system: LocationAPI) : AbyssalDarknessTerrainPlugin? {

        var plugin = system.terrainCopy.find { it.plugin is AbyssalDarknessTerrainPlugin }?.plugin as AbyssalDarknessTerrainPlugin?
        return plugin
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

        //var textureChoice = MathUtils.getRandomNumberInRange(1, 2)
        system.backgroundTextureFilename = "graphics/backgrounds/abyss/Abyss2.jpg"

        val nebula = system.addTerrain("rat_depths",
            OldBaseTiledTerrain.TileParams(string.toString(),
                w,
                h,
                "rat_terrain",
                "depths1",
                4,
                4,
                null))
        nebula.id = "rat_depths_${Misc.genUID()}"
        nebula.location[0f] = 0f

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as AbyssTerrainPlugin
        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(fraction)
        editor.regenNoise()


        return nebulaPlugin
    }

    fun getAbyssTerrainPlugin(system: LocationAPI) : AbyssTerrainPlugin? {

        var plugin = system.terrainCopy.find { it.plugin is AbyssTerrainPlugin }?.plugin as AbyssTerrainPlugin?
        return plugin
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
        val editor = OldNebulaEditor(nebulaPlugin)

        editor.clearArc(entity.location.x, entity.location.y, 0f, radius, 0f, 360f)
        editor.clearArc(entity.location.x, entity.location.y, 0f, radius, 0f, 360f, 0.25f)
    }

    fun clearTerrainAround(system: LocationAPI, location: Vector2f, radius: Float)
    {
        var nebulaPlugin = getAbyssTerrainPlugin(system)
        val editor = OldNebulaEditor(nebulaPlugin)

        editor.clearArc(location.x, location.y, 0f, radius, 0f, 360f)
        editor.clearArc(location.x, location.y, 0f, radius, 0f, 360f, 0.25f)
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

    fun generateCircularPoints(system: StarSystemAPI) {

        var amount = 7

        var emptySlots = ArrayList<Vector2f>()

        var lastSlot = Vector2f(0f, 0f)
        for (i in 0 until amount)
        {
            var slot = generateSlot(lastSlot, 0)

            lastSlot = slot
            emptySlots.add(slot)

            /*   var photosphere = system.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
               photosphere.setLocation(slot.x, slot.y)
               photosphere.radius = 100f

               var plugin = photosphere.customPlugin as AbyssalPhotosphere
               plugin.radius = 15000f
               plugin.color = AbyssUtils.ABYSS_COLOR*/
        }

        var data = AbyssUtils.getSystemData(system)
        for (i in 0 until 3) data.fracturePoints.add(emptySlots.randomAndRemove())
        for (i in 0 until 3) data.majorPoints.add(emptySlots.randomAndRemove())
        data.uniquePoints.add(emptySlots.randomAndRemove())
    }




    private fun generateSlot(lastSlot: Vector2f, attempts: Int) : Vector2f
    {
        var lastSlotsDistanceFromCenter = MathUtils.getDistance(lastSlot, Vector2f(0f, 0f))

        var averageDistance = 3000f
        if (lastSlotsDistanceFromCenter < 10000) averageDistance = 3000f
        else if (lastSlotsDistanceFromCenter < 20000) averageDistance = 2000f

        var spacing = 3000f
        if (lastSlotsDistanceFromCenter < 10000) spacing = 2900f
        else if (lastSlotsDistanceFromCenter < 20000) spacing = 8000f

        var distanceFromlast = lastSlotsDistanceFromCenter + averageDistance

        var pos = Misc.getPointAtRadius(Vector2f(0f, 0f), distanceFromlast)

        var mult = 3f
        if (lastSlotsDistanceFromCenter < 1000)  mult = 0.9f
        else if (lastSlotsDistanceFromCenter < 4000)  mult = 2f

        var newPosDistanceFromLast = MathUtils.getDistance(lastSlot, pos)
        if (newPosDistanceFromLast < spacing)
        {
            if (attempts > 30)
            {
                return pos
            }
            generateSlot(lastSlot,attempts + 1)
        }

        return pos
    }

    //Generates small slots in random locations and around major points
    fun generateMinorPoints(system: StarSystemAPI) {
        var data = AbyssUtils.getSystemData(system)

        for (slot in data.fracturePoints) {
            var point = MathUtils.getPointOnCircumference(slot, MathUtils.getRandomNumberInRange(1000f, 2000f), MathUtils.getRandomNumberInRange(0f, 360f))
            data.minorPoints.add(point)
        }

        for (i in 0 until 20) {
            var point = getMinorLocationToPlaceAt(system)
            if (!point.equals(Vector2f(0f, 0f))) {
                data.minorPoints.add(point)
            }
        }
    }

    private fun getMinorLocationToPlaceAt(system: StarSystemAPI, minDistance: Float = 750f) : Vector2f
    {
        var data = AbyssUtils.getSystemData(system)
        var min = 3000f;
        var max = 16000f;

        var range = MathUtils.getRandomNumberInRange(min, max)
        // var pos = Misc.getPointAtRadius(Vector2f(0f, 0f), range)

        var angle = MathUtils.getRandomNumberInRange(0f, 360f)

        var pos = MathUtils.getPointOnCircumference(Vector2f(0f, 0f), range, angle)

        var existingEntities = system.customEntities
        for (existing in existingEntities)
        {
            var distance = MathUtils.getDistance(existing.location, pos)
            if (distance < minDistance)
            {
                getMinorLocationToPlaceAt(system)
            }
        }

        var existingPoints = data.minorPoints + data.majorPoints + data.fracturePoints + data.uniquePoints
        for (existing in existingPoints)
        {
            var distance = MathUtils.getDistance(existing, pos)
            if (distance < minDistance)
            {
                getMinorLocationToPlaceAt(system)
            }
        }

        return pos
    }

    fun addDefenseFleetManager(defenseTarget: SectorEntityToken, max: Int, depth: AbyssDepth, chancetoAddPer: Float)
    {
        for (i in 0 until max)
        {
            if (Random().nextFloat() >= chancetoAddPer) continue
            defenseTarget.containingLocation.addScript(AbyssalDefendingFleetManager(defenseTarget, depth))
        }

    }


}