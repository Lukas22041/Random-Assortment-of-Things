package assortment_of_things.abyss

import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.intel.event.AbyssalDepthsEventIntel
import assortment_of_things.abyss.misc.AbyssBackgroundWarper
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.terrain.AbyssTerrainPlugin
import assortment_of_things.abyss.terrain.AbyssalDarknessTerrainPlugin
import assortment_of_things.abyss.terrain.SuperchargedAbyssTerrainPlugin
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList


enum class AbyssDifficulty {
    Normal, Hard
}

data class LinkedFracture(val fracture1: SectorEntityToken, var fracture2: SectorEntityToken)

object AbyssUtils {

    var ABYSS_COLOR = Color(255, 0, 50)
    var SUPERCHARGED_COLOR = Color(16, 154, 100)
    var FACTION_ID = "rat_abyssals"

    var SYSTEM_NAMES = arrayListOf("Tranquilility", "Serenity", "Storms", "Harmony", "Decay", "Solitude", "Sorrow", "Time", "Epidemics",
        "Crises", "Knowledge", "Serpents", "Hope", "Death", "Perseverance", "Fear", "Cold", "Clouds", "Luxury", "Hatred", "Dreams", "Honor", "Trust", "Success", "Joy")

    var SYSTEM_TAG = "rat_abyss_system"
    var DEFEND_STRUCTURE_TAG = "rat_defend_structure"

    var ABYSS_SYSTEMS_KEY = "\$rat_abyss_systems"


    fun addAbyssSystemToMemory(system: StarSystemAPI)
    {
        var systems = Global.getSector().memoryWithoutUpdate.get(ABYSS_SYSTEMS_KEY) as MutableList<StarSystemAPI>?
        if (systems == null)  {
            systems = ArrayList<StarSystemAPI>()
        }
        systems.add(system)
        Global.getSector().memoryWithoutUpdate.set(ABYSS_SYSTEMS_KEY, systems)
    }

    fun getAllAbyssSystems() : List<StarSystemAPI>
    {
        var systems = Global.getSector().memoryWithoutUpdate.get(ABYSS_SYSTEMS_KEY) as MutableList<StarSystemAPI>?
        if (systems == null) {
            systems = ArrayList<StarSystemAPI>()
        }
        return ArrayList(systems)
    }

    //Sets up important tags, like "HIDDEN" to prevent the systems from being used by other mods.
    fun setupTags(system: StarSystemAPI)
    {
        system.generateAnchorIfNeeded()
        system.addTag(SYSTEM_TAG)
        system.isProcgen = false
        system.addTag(Tags.THEME_HIDDEN)
        system.addTag(Tags.THEME_UNSAFE)
        system.addTag(Tags.THEME_SPECIAL)
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
        system.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_music_abyss")
        system.isEnteredByPlayer = false
    }

    fun getNeighbouringSystems(system: StarSystemAPI) : List<StarSystemAPI> {
        return system.memoryWithoutUpdate.get("\$rat_abyss_neighbours") as MutableList<StarSystemAPI>? ?: ArrayList()
    }

    fun setNeighbours(system1: StarSystemAPI, system2: StarSystemAPI) {

        var systems1 = system1.memoryWithoutUpdate.get("\$rat_abyss_neighbours") as MutableList<StarSystemAPI>? ?: ArrayList()
        systems1.add(system2)
        systems1.distinct()
        system1.memoryWithoutUpdate.set("\$rat_abyss_neighbours", systems1)

        var systems2 = system2.memoryWithoutUpdate.get("\$rat_abyss_neighbours") as MutableList<StarSystemAPI>? ?: ArrayList()
        systems2.add(system1)
        systems2.distinct()
        system2.memoryWithoutUpdate.set("\$rat_abyss_neighbours", systems2)
    }

    fun getFarNeighbouringSystems(system: StarSystemAPI) : List<StarSystemAPI> {
        return system.memoryWithoutUpdate.get("\$rat_abyss_far_neighbours") as MutableList<StarSystemAPI>? ?: ArrayList()
    }

    fun setFarNeighbours(system1: StarSystemAPI, system2: StarSystemAPI) {

        var systems1 = system1.memoryWithoutUpdate.get("\$rat_abyss_far_neighbours") as MutableList<StarSystemAPI>? ?: ArrayList()
        systems1.add(system2)
        systems1.distinct()
        system1.memoryWithoutUpdate.set("\$rat_abyss_far_neighbours", systems1)

        var systems2 = system2.memoryWithoutUpdate.get("\$rat_abyss_far_neighbours") as MutableList<StarSystemAPI>? ?: ArrayList()
        systems2.add(system1)
        systems2.distinct()
        system2.memoryWithoutUpdate.set("\$rat_abyss_far_neighbours", systems2)
    }

    fun playerInNeighbourOrSystem(system: StarSystemAPI) : Boolean
    {
        var player = Global.getSector().playerFleet
        return player.containingLocation == system || getNeighbouringSystems(system).any { player.containingLocation == it }
    }

    fun createFractures(system1: StarSystemAPI, system2: StarSystemAPI) : LinkedFracture
    {

        var UID = Misc.genUID()
        var fracture1 = system1.addCustomEntity("abyss_fracture_" + UID + "_1", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)
        var fracture2 = system2.addCustomEntity("abyss_fracture_" + UID + "_2", "Abyssal Fracture", "rat_abyss_fracture", Factions.NEUTRAL)

        linkFracture(fracture1, fracture2)
        linkFracture(fracture2, fracture1)

        addLightsource(fracture1, 10000f)
        addLightsource(fracture2, 10000f)

        return LinkedFracture(fracture1, fracture2)
    }


    fun linkFracture(fracture: SectorEntityToken, target: SectorEntityToken) {
        (fracture.customPlugin as AbyssalFracture).connectedEntity = target
    }

    fun getConnectedFracture(fracture: SectorEntityToken) : SectorEntityToken {
        return (fracture.customPlugin as AbyssalFracture).connectedEntity!!
    }

    fun generateBaseDetails(system: StarSystemAPI, tier: AbyssProcgen.Tier)
    {
        var color = generateAbyssColor(system, tier)

        var fraction = when(tier) {
            AbyssProcgen.Tier.Low -> 0.35f
            AbyssProcgen.Tier.Mid -> 0.35f
            AbyssProcgen.Tier.High -> 0.4f
        }

        var terrain = AbyssUtils.generateAbyssTerrain(system, fraction)
        var darkness = generateAbyssDarkness(system)
        terrain.color = color
        var warper = AbyssBackgroundWarper(system, 8, 0.33f)
        warper.overwriteColor = color
    }

    fun generateAbyssDarkness(system: StarSystemAPI) : AbyssalDarknessTerrainPlugin{
        val darkness = system.addTerrain("rat_depths_darkness", null)
        return (darkness as CampaignTerrainAPI).plugin as AbyssalDarknessTerrainPlugin
    }

    fun getAbyssDarknessTerrainPlugin(system: LocationAPI) : AbyssalDarknessTerrainPlugin? {

        var plugin = system.terrainCopy.find { it.plugin is AbyssalDarknessTerrainPlugin }?.plugin as AbyssalDarknessTerrainPlugin?
        return plugin
    }

    fun generateAbyssColor(system: StarSystemAPI, tier: AbyssProcgen.Tier) : Color{

        var h = MathUtils.getRandomNumberInRange(0.935f, 1f)
        if (Random().nextFloat() > 0.75f) h = MathUtils.getRandomNumberInRange(0.0f, 0.025f)

        var lightColor = Color.WHITE

        var s = 1f
        var b = 1f
        when (tier) {
            AbyssProcgen.Tier.Low -> {
                lightColor = Color.WHITE
                b = 0.8f
            }
            AbyssProcgen.Tier.Mid -> {
                lightColor = Color.gray
                b = 0.3f
            }
            AbyssProcgen.Tier.High -> {
                lightColor = Color.DARK_GRAY
                b = 0.2f
            }
        }

        system.lightColor = lightColor

        var color = Color.getHSBColor(h, s, b)

        system.memoryWithoutUpdate.set("\$rat_abyss_color", color)
        return color
    }

    fun getSystemColor(system: LocationAPI) = system.memoryWithoutUpdate.get("\$rat_abyss_color") as Color

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

        val nebula = system.addTerrain("rat_depths", BaseTiledTerrain.TileParams(string.toString(), w, h, "rat_terrain", "depths1", 4, 4, null))
        nebula.id = "rat_depths_${Misc.genUID()}"
        nebula.location[0f] = 0f

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as AbyssTerrainPlugin
        val editor = NebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(fraction)
        editor.regenNoise()


        return nebulaPlugin
    }

    fun getAbyssTerrainPlugin(system: StarSystemAPI) : AbyssTerrainPlugin? {

        var plugin = system.terrainCopy.find { it.plugin is AbyssTerrainPlugin }?.plugin as AbyssTerrainPlugin?
        return plugin
    }

    fun getSuperchargedAbyssTerrainPlugin(system: StarSystemAPI) : SuperchargedAbyssTerrainPlugin? {

        var plugin = system.terrainCopy.find { it.plugin is SuperchargedAbyssTerrainPlugin }?.plugin as SuperchargedAbyssTerrainPlugin?
        return plugin
    }

    fun generateSuperchargedTerrain(system: StarSystemAPI, location: Vector2f, range: Int, fraction: Float, clearArea: Boolean) : SuperchargedAbyssTerrainPlugin
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

    fun createSensorIcon(entity: SectorEntityToken, maxRange: Float, color: Color)
    {
        var player = Global.getSector().playerFleet
        if (player.containingLocation != entity.containingLocation) return
        if (!entity.isDiscoverable) return
        var distance = MathUtils.getDistance(player, entity)

        if (distance > maxRange) return

        var abyssPlugin = getAbyssTerrainPlugin(entity.starSystem)
        var superchargedPlugin = getSuperchargedAbyssTerrainPlugin(entity.starSystem)


        var inStorm = false

        if (abyssPlugin != null && abyssPlugin.isInStorm(player)) inStorm = true
        if (superchargedPlugin != null && superchargedPlugin.isInStorm(player)) inStorm = true

       // if (inStorm)
     //   {
            if (entity.containingLocation.customEntities.any { it.id ==  "${entity.id}_icon"} ) return

            var icon = entity.containingLocation.addCustomEntity("${entity.id}_icon", "Unknown Entity", "rat_abyss_icon", Factions.NEUTRAL)

            icon.location.set(entity.location)
            icon.setCircularOrbit(entity, 0f, 0f, 0f)

            Global.getSoundPlayer().playUISound("ui_discovered_entity", 0.5f, 1f)

            val custom = CampaignPingSpec()
            custom.color = AbyssUtils.SUPERCHARGED_COLOR
            custom.width = 10f
            custom.minRange = player.radius
            custom.range = player.radius + 300
            custom.duration = 2f
            custom.alphaMult = 1f
            custom.inFraction = 0.1f
            custom.num = 1
            custom.isInvert = true

            Global.getSector().addPing(player, custom)
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

    fun getShieldLossPerDay() : Float
    {
        var membersWithShielding = Global.getSector().playerFleet.fleetData.membersListCopy
        var cost = 0f

        for (member in membersWithShielding)
        {
            if (member.variant.hasHullMod("rat_abyssal_core")) continue

            cost += when(member.hullSpec.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 0.05f
                ShipAPI.HullSize.DESTROYER -> 0.1f
                ShipAPI.HullSize.CRUISER -> 0.2f
                ShipAPI.HullSize.CAPITAL_SHIP -> 0.4f
                else -> 0f
            }
        }


        if (AbyssalDepthsEventIntel.get()?.isStageActive(AbyssalDepthsEventIntel.Stage.RESOURCEFULNESS) == true)
        {
            cost *= 0.5f
        }

        return cost
    }

    fun restoreShielding(amount: Float) {
        var memory = Global.getSector().memoryWithoutUpdate
        var current = memory.get("\$rat_abyss_currentShielding") as Float?
        if (current == null) {
            current = 0f
        }
        current += amount
        memory.set("\$rat_abyss_currentShielding", current)
    }

    fun setPreviousSystem(current: StarSystemAPI, previous: StarSystemAPI) {
        current.memoryWithoutUpdate.set("\$rat_abyss_previous", previous)
    }

    fun getPreviousSystem(system: StarSystemAPI) : StarSystemAPI? {
       return system.memoryWithoutUpdate.get("\$rat_abyss_previous") as StarSystemAPI?
    }

    fun setSystemLocation(system: StarSystemAPI, location: Vector2f) {
        system.memoryWithoutUpdate.set("\$rat_abyss_location", location)
    }

    fun getSystemLocation(system: StarSystemAPI) : Vector2f {
        var location = system.memoryWithoutUpdate.get("\$rat_abyss_location") as Vector2f?
        if (location == null) location = Vector2f(0f, 0f)
        return location
    }

    fun setTier(system: StarSystemAPI, tier: AbyssProcgen.Tier)  {
        system.memoryWithoutUpdate.set("\$rat_abyss_tier", tier)
    }

    fun getTier(system: LocationAPI) : AbyssProcgen.Tier {
        return system.memoryWithoutUpdate.get("\$rat_abyss_tier") as AbyssProcgen.Tier
    }

    fun anyNearbyFleetsHostileAndAware() : Boolean {
        val playerFleet = Global.getSector().playerFleet

        for (fleet in playerFleet.containingLocation.fleets) {
            if (fleet.ai == null) continue
            if (fleet.isStationMode) continue

            if (fleet.visibilityLevelOfPlayerFleet == SectorEntityToken.VisibilityLevel.SENSOR_CONTACT ||
                fleet.visibilityLevelOfPlayerFleet == SectorEntityToken.VisibilityLevel.COMPOSITION_DETAILS ||
                fleet.visibilityLevelOfPlayerFleet == SectorEntityToken.VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
                return true
            }
        }

        return false
    }

    fun addLightsource(entity: SectorEntityToken, radius: Float, color: Color? = ABYSS_COLOR.setAlpha(50)) {
        var lightsource = entity.containingLocation.addCustomEntity("rat_lightsource_${Misc.genUID()}", "", "rat_lightsource", Factions.NEUTRAL)
        lightsource.setCircularOrbit(entity, 0f, 0f, 1000f)

        var plugin = lightsource.customPlugin as AbyssalLightsource
        plugin.radius = radius
        plugin.color = color
    }

    fun addLightsource(system: LocationAPI, location: Vector2f, radius: Float, color: Color? = ABYSS_COLOR.setAlpha(50)) {
        var lightsource = system.addCustomEntity("rat_lightsource_${Misc.genUID()}", "", "rat_lightsource", Factions.NEUTRAL)
        lightsource.location.set(location)

        var plugin = lightsource.customPlugin as AbyssalLightsource
        plugin.radius = radius
        plugin.color = color
    }

    fun getDifficulty() : AbyssDifficulty {
        if (RATSettings.abyssDifficulty == "Hard") return AbyssDifficulty.Hard
        return AbyssDifficulty.Normal
    }

    fun addAlterationsToFleet(fleet: CampaignFleetAPI, chancePerShip: Float, random: Random) {

        var members = fleet.fleetData.membersListCopy

        fleet.inflateIfNeeded()
        for (member in members)
        {
            if (random.nextFloat() > chancePerShip) continue

            var alterations = mutableMapOf("rat_qualityAssurance" to 1f, "rat_timegear" to 1f, "rat_overloaded_systems" to 1f, "rat_preperation" to 1f)

            var hasBay = member.variant.fittedWings.size != 0

            var hasBallistic = false
            var hasEnergy = false
            var hasMissile = false

            var weapons = member.variant.fittedWeaponSlots.map { member.variant.getWeaponSpec(it) }
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.BALLISTIC }) hasBallistic = true
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.ENERGY }) hasEnergy = true
            if (weapons.any { it.mountType == WeaponAPI.WeaponType.MISSILE }) hasMissile = true

            if (hasBay) alterations.putAll(mapOf("rat_temporalAssault" to 2f, "rat_perseverance" to 4f, "rat_magneticStorm" to 4f, "rat_plasmaticShield" to 4f))

            if (hasBallistic) alterations.putAll(mapOf("rat_ballistic_focus" to 1.5f))
            if (hasEnergy) alterations.putAll(mapOf("rat_energy_focus" to 1.5f))
            if (hasMissile) alterations.putAll(mapOf("rat_missile_reserve" to 0.5f))

            var picker = WeightedRandomPicker<String>()
            alterations.forEach { picker.add(it.key, it.value) }

            var pick = picker.pick()

            if (member.variant.source != VariantSource.REFIT)
            {
                var variant = member.variant.clone();
                variant.originalVariant = null;
                variant.hullVariantId = Misc.genUID()
                variant.source = VariantSource.REFIT
                member.setVariant(variant, false, true)
            }
            member.variant.addPermaMod(pick, true)
            member.updateStats()

        }
        fleet.inflateIfNeeded()

    }
}