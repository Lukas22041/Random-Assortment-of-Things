package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.intel.event.DiscoveredPhotosphere
import assortment_of_things.abyss.intel.event.SignificantEntityDiscoveredFactor
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.scripts.AbyssalDefendingFleetManager
import assortment_of_things.artifacts.ArtifactUtils
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getStorageCargo
import org.magiclib.kotlin.setAlpha
import java.util.*
import kotlin.random.asJavaRandom

object AbyssProcgen {

    enum class Tier {
        Low, Mid, High
    }

    fun getLocationToPlaceAt(system: StarSystemAPI) : Vector2f
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
            if (distance < 1000)
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
}