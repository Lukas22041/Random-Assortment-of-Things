package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.intel.event.EntityDiscoveredFactor
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.scripts.AbyssalDefendingFleetManager
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
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

    fun getLocationToPlaceAt(system: StarSystemAPI) : Vector2f
    {
        var min = 3000f;
        var max = 17500f;

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

    fun generateDomainResearchStation(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        for (i in 0 until max)
        {
            //chance to add per gets run for each attempt to spawn a station, unlike BaseThemeGenerators idea of randomly adding either all or none
            if (Random().nextFloat() >= chanceToAddPer) continue

            var station = system.addCustomEntity("rat_domain_research_${Misc.genUID()}", "Domain Research Station", "rat_abyss_research", Factions.NEUTRAL)
            var loc = getLocationToPlaceAt(system)
            station.location.set(loc)
            station.addScript(EntityDiscoveredFactor(5, station))
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

            var defenseChance = when(tier) {
                Tier.Low -> 0.50f
                Tier.Mid -> 0.60f
                Tier.High -> 0.80f
            }

            var minP = 40f
            var maxP = 60f
            if (tier == Tier.Mid) minP = 60f
            if (tier == Tier.Mid) maxP = 120f
            if (tier == Tier.High) minP = 80f
            if (tier == Tier.High) maxP = 160f
            addDefenseFleetManager(station, minP, maxP, tier, defenseChance)
            AbyssUtils.addLightsource(station, 7500f, AbyssUtils.SUPERCHARGED_COLOR.setAlpha(35))

        }
    }

    fun generateTransmitter(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        for (i in 0 until max)
        {
            if (Random().nextFloat() >= chanceToAddPer) continue

            var transmitter = system.addCustomEntity("rat_abyss_transmitter_${Misc.genUID()}", "Research Transmitter", "rat_abyss_transmitter", Factions.NEUTRAL)
            var loc = getLocationToPlaceAt(system)
            transmitter.location.set(loc)
            transmitter.addScript(EntityDiscoveredFactor(5, transmitter))
            transmitter.addTag(AbyssTags.TRANSMITTER)
            transmitter.addTag(AbyssTags.TRANSMITTER_UNLOOTED)
            transmitter.memoryWithoutUpdate.set(MemFlags.SALVAGE_SEED, Misc.genRandomSeed())
        }
    }

    fun generateCache(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        for (i in 0 until max)
        {
            if (Random().nextFloat() >= chanceToAddPer) continue

            var cache = system.addCustomEntity("rat_abyss_cache_${Misc.genUID()}", "Lost Crate", "rat_abyss_cache", Factions.NEUTRAL)
            var loc = getLocationToPlaceAt(system)
            cache.location.set(loc)
            cache.addScript(EntityDiscoveredFactor(5, cache))
            cache.addTag(AbyssTags.LOST_CRATE)
            cache.memoryWithoutUpdate.set(MemFlags.SALVAGE_SEED, Misc.genRandomSeed())
        }
    }

    fun generateOutposts(system: StarSystemAPI)
    {
        var tier = AbyssUtils.getTier(system)

        var outpost = system.addCustomEntity("rat_abyss_outpost_${Misc.genUID()}", "Abandoned Expedition Outpost", "rat_abyss_outpost", Factions.NEUTRAL)
        var loc = getLocationToPlaceAt(system)
        outpost.location.set(loc)
        outpost.addScript(EntityDiscoveredFactor(5, outpost))
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

        AbyssUtils.clearTerrainAround(outpost, 500f)

        //AbyssUtils.generateSuperchargedTerrain(outpost.starSystem, outpost.location, 300, 0.8f, false)


      /*  var token = system.createToken(outpost.location)
        system.addEntity(token)*/
        addDefenseFleetManager(outpost, 100f, 120f, tier, 1f)

        AbyssUtils.addLightsource(outpost, 7500f, AbyssUtils.SUPERCHARGED_COLOR.setAlpha(35))


    }

    fun generatePhotospheres(system: StarSystemAPI, max: Int, chanceToAddPer: Float)
    {
        var photosphere = system.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
        photosphere.setLocation(0f, 0f)
        photosphere.radius = 100f

        var plugin = photosphere.customPlugin as AbyssalPhotosphere
        plugin.radius = 15000f
        plugin.color = AbyssUtils.ABYSS_COLOR

    }

    fun addDefenseFleetByTier(defenseTarget: SectorEntityToken, tier: Tier, chanceToAdd: Float)
    {

    }

    fun addDefenseFleetManager(defenseTarget: SectorEntityToken, minPoints: Float, maxPoints: Float, quality: AbyssProcgen.Tier, chanceToAdd: Float)
    {
        if (Random().nextFloat() >= chanceToAdd) return
        defenseTarget.containingLocation.addScript(AbyssalDefendingFleetManager(defenseTarget, minPoints, maxPoints, quality))
    }


}