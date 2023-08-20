package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.entities.RiftEntrance
import assortment_of_things.abyss.entities.RiftExit
import assortment_of_things.abyss.intel.event.DiscoveredPhotosphere
import assortment_of_things.abyss.intel.event.SignificantEntityDiscoveredFactor
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.scripts.AbyssalDefendingFleetManager
import assortment_of_things.artifacts.ArtifactUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.starfarer.campaign.CampaignEngine
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getStorageCargo
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

object AbyssProcgen {

    enum class Tier {
        Low, Mid, High
    }

    fun getLocationToPlaceAt(system: StarSystemAPI, minDistance: Float = 1000f) : Vector2f
    {
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
                getLocationToPlaceAt(system)
            }
        }

        return pos
    }

    fun takeEmptySlot(system: StarSystemAPI) : Vector2f {
        var slots = system.memoryWithoutUpdate.get("\$rat_abyss_emptySlots") as MutableList<Vector2f>
        if (slots.isEmpty()) return Vector2f(0f, 0f)
        var slot = slots.random()
        slots.remove(slot)
        system.memoryWithoutUpdate.set("\$rat_abyss_emptySlots", slots)
        return slot
    }

    fun hasEmptySlots(system: StarSystemAPI) : Boolean {
        var slots = system.memoryWithoutUpdate.get("\$rat_abyss_emptySlots") as MutableList<Vector2f>
        return slots.isNotEmpty()
    }

    fun generateDomainResearchStations(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        for (i in 0 until max)
        {
            //chance to add per gets run for each attempt to spawn a station, unlike BaseThemeGenerators idea of randomly adding either all or none
            if (Random().nextFloat() >= chanceToAddPer) continue

            var station = generateResearchStation(system)
            var loc = getLocationToPlaceAt(system)
            station.location.set(loc)

            var tier = AbyssUtils.getTier(system)


            var defenseChance = when(tier) {
                Tier.Low -> 0.75f
                Tier.Mid -> 0.60f
                Tier.High -> 0.80f
            }

            addDefenseFleetManager(station, 1, tier, FleetTypes.PATROL_MEDIUM, defenseChance)

            if (tier != Tier.Low) {
                AbyssUtils.addLightsource(station, 4000f, AbyssUtils.SUPERCHARGED_COLOR.setAlpha(30))
            }
        }
    }

    fun generateResearchStation(system: StarSystemAPI) : SectorEntityToken {
        var station = system.addCustomEntity("rat_domain_research_${Misc.genUID()}", "Research Station", "rat_abyss_research", Factions.NEUTRAL)

        station.addScript(SignificantEntityDiscoveredFactor(10, station))
        station.addTag(AbyssTags.DOMAIN_RESEARCH)
        station.addTag(AbyssTags.LOOTABLE)
        station.memoryWithoutUpdate.set(MemFlags.SALVAGE_SEED, Misc.genRandomSeed())

        var tier = AbyssUtils.getTier(system)

        var extraChance = when(tier) {
            Tier.Low -> 0.25f
            Tier.Mid -> 0.30f
            Tier.High -> 0.35f
        }

        if (Random().nextFloat() < extraChance)
        {
            var picker = WeightedRandomPicker<String>()
            picker.add(AbyssTags.DOMAIN_RESEARCH_PRODUCTION, 1f)
            picker.add(AbyssTags.DOMAIN_RESEARCH_SURVEY, 1f)
            station.addTag(picker.pick())
        }

        return station
    }

    fun generateTransmitters(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        for (i in 0 until max)
        {
            if (Random().nextFloat() >= chanceToAddPer) continue

            var transmitter = generateTransmitter(system)
            var loc = getLocationToPlaceAt(system)
            transmitter.location.set(loc)

        }
    }

    fun generateTransmitter(system: StarSystemAPI) : SectorEntityToken {

        var transmitter = system.addCustomEntity("rat_abyss_transmitter_${Misc.genUID()}", "Research Transmitter", "rat_abyss_transmitter", Factions.NEUTRAL)

        transmitter.addTag(AbyssTags.TRANSMITTER)
        transmitter.addTag(AbyssTags.TRANSMITTER_UNLOOTED)
        transmitter.memoryWithoutUpdate.set(MemFlags.SALVAGE_SEED, Misc.genRandomSeed())

        return transmitter
    }

    fun generateCaches(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        for (i in 0 until max)
        {
            if (Random().nextFloat() >= chanceToAddPer) continue

            var cache = generateCache(system)
            var loc = getLocationToPlaceAt(system)
            cache.location.set(loc)
        }
    }

    fun generateCache(system: StarSystemAPI) : SectorEntityToken {
        var cache = system.addCustomEntity("rat_abyss_cache_${Misc.genUID()}", "Lost Crate", "rat_abyss_cache", Factions.NEUTRAL)
        cache.addTag(AbyssTags.LOST_CRATE)
        cache.memoryWithoutUpdate.set(MemFlags.SALVAGE_SEED, Misc.genRandomSeed())

        return cache
    }

    fun generateOutposts(system: StarSystemAPI)
    {
        var tier = AbyssUtils.getTier(system)

        var outpost = generateOutpost(system)
        var loc = getLocationToPlaceAt(system)
        outpost.location.set(loc)

        AbyssUtils.clearTerrainAround(outpost, 500f)

        //AbyssUtils.generateSuperchargedTerrain(outpost.starSystem, outpost.location, 300, 0.8f, false)


      /*  var token = system.createToken(outpost.location)
        system.addEntity(token)*/
        addDefenseFleetManager(outpost, 1, tier, FleetTypes.PATROL_LARGE, 1f)

        AbyssUtils.addLightsource(outpost, 4000f, AbyssUtils.SUPERCHARGED_COLOR.setAlpha(30))
    }

    fun generateOutpost(system: StarSystemAPI) : SectorEntityToken
    {
        var tier = AbyssUtils.getTier(system)

        var outpost = system.addCustomEntity("rat_abyss_outpost_${Misc.genUID()}", "Abandoned Expedition Outpost", "rat_abyss_outpost", Factions.NEUTRAL)

        outpost.addScript(SignificantEntityDiscoveredFactor(10, outpost))
        outpost.addTag(AbyssTags.OUTPOST)
        outpost.memoryWithoutUpdate.set(MemFlags.SALVAGE_SEED, Misc.genRandomSeed())

        Misc.setAbandonedStationMarket("abyss_outpost_${Misc.genUID()}", outpost)

        var cargo = outpost.market.getStorageCargo()

        var dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()
        var dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()
        var drop = SalvageEntityGenDataSpec.DropData()

        drop = SalvageEntityGenDataSpec.DropData()
        drop.group = "basic"
        drop.value = 10000
        dropValue.add(drop)

        drop = SalvageEntityGenDataSpec.DropData()
        drop.chances = 3
        drop.group = "abyss_cache_loot"
        dropRandom.add(drop)

        var salvage = SalvageEntity.generateSalvage(Random(), 1f, 1f, 1f, 1f, dropValue, dropRandom)
        cargo.addAll(salvage)
        ArtifactUtils.generateArtifactLoot(salvage, "abyss", 0.1f, 1, Random())

        return outpost
    }

    fun generatePhotospheres(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        var tier = AbyssUtils.getTier(system)

        var first = true
        for (i in 0 until max)
        {
            if (first) first = false
            else if (Random().nextFloat() >= chanceToAddPer) continue

            if (!AbyssProcgen.hasEmptySlots(system)) return
            var pos = AbyssProcgen.takeEmptySlot(system)

            var photosphere = system.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
            photosphere.setLocation(pos.x, pos.y)
            photosphere.radius = 100f
            photosphere.addScript(DiscoveredPhotosphere(10, photosphere))

            var plugin = photosphere.customPlugin as AbyssalPhotosphere
           // plugin.radius = 15000f
            plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)
            plugin.color = AbyssUtils.ABYSS_COLOR

            var picker = WeightedRandomPicker<() -> SectorEntityToken>()
            picker.add( { generateCache(system) }, 2f)
            picker.add( { generateCache(system) }, 2f)
            picker.add( { generateCache(system) }, 2f)
            picker.add( { generateCache(system) }, 2f)
            picker.add( { generateTransmitter(system) }, 2f)
            picker.add( { generateResearchStation(system) }, 2f)
            picker.add( { generateOutpost(system) }, 1f)

            var added = 0
            var orbit = MathUtils.getRandomNumberInRange(50f, 100f)
            var days = 30f
            for (i in 0 until 3)
            {
                orbit += MathUtils.getRandomNumberInRange(200f, 350f)
                days += 35
                if (Random().nextFloat() > 0.25f)
                {
                    var function = picker.pickAndRemove()
                    var entity = function()

                    entity.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), orbit, days)
                    added++
                }
            }

            if (added == 0)
            {
                var function = picker.pickAndRemove()
                var entity = function()

                entity.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), MathUtils.getRandomNumberInRange(300f, 1000f), 90f)
            }

            var defenseChance = 0.75f

            var type = FleetTypes.PATROL_LARGE
            if (tier == Tier.Low) type = FleetTypes.PATROL_MEDIUM
            addDefenseFleetManager(photosphere, 2, tier, type, defenseChance)

        }
    }

    fun addDefenseFleetByTier(defenseTarget: SectorEntityToken, tier: Tier, chanceToAdd: Float)
    {

    }

    fun addDefenseFleetManager(defenseTarget: SectorEntityToken, max: Int, tier: AbyssProcgen.Tier, type: String, chancetoAddPer: Float)
    {
        for (i in 0 until max)
        {
            if (Random().nextFloat() >= chancetoAddPer) continue
            defenseTarget.containingLocation.addScript(AbyssalDefendingFleetManager(defenseTarget, tier, type))
        }

    }

    fun addDerelictAbyssalShips(system: StarSystemAPI, max: Int, chanceToAddPer: Float) {
        for (i in 0 until max) {
            if (Random().nextFloat() >= chanceToAddPer) continue

            val faction: String = "rat_abyssals"
            val params = DerelictShipEntityPlugin.createRandom(faction, null, Random(), DerelictShipEntityPlugin.getDefaultSModProb())

            if (params != null) {
                val entity = BaseThemeGenerator.addSalvageEntity(Random(), system, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI
                entity.setDiscoverable(true)

                var loc = getLocationToPlaceAt(system, 250f)
                entity.location.set(loc)
                entity.addTag(AbyssTags.ABYSS_WRECK)
            }
        }
    }

    fun createRift(system: StarSystemAPI, location: Vector2f) : StarSystemAPI {
        var riftEntrance = system.addCustomEntity("rift_entrance_${Misc.genUID()}", "Abyssal Rift", "rat_abyss_rift_entrance", Factions.NEUTRAL)
        riftEntrance.location.set(location)
        AbyssUtils.clearTerrainAround(riftEntrance, 1000f)

        var riftEntrancePlugin = riftEntrance.customPlugin as RiftEntrance


        var riftSystem = Global.getSector().createStarSystem("${system.baseName} Rift")
        riftSystem.lightColor = Color.DARK_GRAY

        riftSystem.addTag(AbyssUtils.RIFT_TAG)

        AbyssUtils.setNeighbours(system, riftSystem)
        AbyssUtils.setTier(riftSystem, Tier.High)
        AbyssUtils.setupTags(riftSystem)
        var color = AbyssUtils.generateAbyssColor(riftSystem, Tier.High)

        var terrain = AbyssUtils.generateAbyssTerrain(riftSystem, 0f)
        terrain.color = color
        var darkness = AbyssUtils.generateAbyssDarkness(riftSystem)

        riftSystem.backgroundTextureFilename = "graphics/backgrounds/abyss/darkness.jpg"



        var riftExit = riftSystem.addCustomEntity("rift_entrance_${Misc.genUID()}", "Abyssal Rift", "rat_abyss_rift_exit", Factions.NEUTRAL)

        var riftExitPlugin = riftExit.customPlugin as RiftExit

        riftEntrance.addScript(object: EveryFrameScript {

            var playSound = false

            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {
                var player = Global.getSector().playerFleet

                if (playSound) {
                    Global.getSoundPlayer().playSound("jump_point_open", 1f, 0.8f, Global.getSector().playerFleet.location, Vector2f() )

                    playSound = false
                }

                if (player.containingLocation != riftEntrance.containingLocation) return
                if (MathUtils.getDistance(player.location, riftEntrance.location) < riftEntrancePlugin.radius) {

                    var playerFleet = Global.getSector().playerFleet
                    var currentLocation = playerFleet.containingLocation

                    var angle = Misc.getAngleInDegrees(riftEntrance.location, playerFleet.location)
                    var radius = riftExitPlugin.radius - playerFleet.radius
                    var point = MathUtils.getPointOnCircumference(riftExit.location, radius, angle)


                    currentLocation.removeEntity(playerFleet)
                    riftSystem.addEntity(playerFleet)
                    Global.getSector().setCurrentLocation(riftSystem)


                    playerFleet.setLocation(point.x, point.y)
                    playSound = true

                    CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                }
            }
        })

        riftExit.addScript(object: EveryFrameScript {

            var playSound = false


            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {
                var player = Global.getSector().playerFleet

                if (playSound) {
                    Global.getSoundPlayer().playSound("jump_point_close", 1f, 0.8f, Global.getSector().playerFleet.location, Vector2f() )

                    playSound = false
                }

                if (player.containingLocation != riftExit.containingLocation) return
                if (MathUtils.getDistance(player, riftExit) > riftExitPlugin.radius - player.radius) {

                    var playerFleet = Global.getSector().playerFleet
                    var currentLocation = playerFleet.containingLocation

                    var angle = Misc.getAngleInDegrees(riftExit.location, playerFleet.location)
                    var radius = riftEntrancePlugin.radius + playerFleet.radius + 20f
                    var point = MathUtils.getPointOnCircumference(riftEntrance.location, radius, angle)

                    currentLocation.removeEntity(playerFleet)
                    system.addEntity(playerFleet)
                    Global.getSector().setCurrentLocation(system)

                    playerFleet.setLocation(point.x, point.y)

                    playSound = true

                    Global.getSector().campaignUI
                    CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                }
            }
        })








        return riftSystem

    }

}