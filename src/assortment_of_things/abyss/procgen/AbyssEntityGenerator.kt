package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.intel.event.DiscoveredPhotosphere
import assortment_of_things.abyss.intel.event.SignificantEntityDiscoveredFactor
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.scripts.AbyssDistantIconScript
import assortment_of_things.abyss.scripts.AbyssalDefendingFleetManager
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList

object AbyssEntityGenerator {


    var sensorRadius = hashMapOf(
        "rat_abyss_fabrication" to 12500f,
        "rat_abyss_accumalator" to 12500f,
        "rat_abyss_unknown_lab" to 12500f,
        "rat_abyss_research" to 15000f,
    )


    fun spawnMinorEntity(system: StarSystemAPI, type: String) : SectorEntityToken {
        var entity = system.addCustomEntity("${type}_${Misc.genUID()}", null, type, Factions.NEUTRAL)
        entity.getSalvageSeed()

        var sensor = 10000f
        if (sensorRadius.contains(type)) {
            sensor = sensorRadius.get(type)!!
        }

        if (entity.hasTag("major")) {
            var discoveryScript = SignificantEntityDiscoveredFactor(5, entity)
            entity.addScript(discoveryScript)
        }
        var sensorScript = AbyssDistantIconScript(entity, sensor)
        entity.addScript(sensorScript)

        var randomRotation = MathUtils.getRandomNumberInRange(0f, 1f) >= 0.5f
        entity.addScript( object : EveryFrameScript {

            override fun isDone(): Boolean {
                return false
            }


            override fun runWhilePaused(): Boolean {
                return false
            }


            override fun advance(amount: Float) {
                if (randomRotation)
                {
                    entity.facing += 0.015f
                }
                else
                {
                    entity.facing -= 0.015f
                }
            }

        })

        return entity
    }

    fun addDefenseFleetManager(defenseTarget: SectorEntityToken, max: Int, chancetoAddPer: Float)
    {
        for (i in 0 until max)
        {
            var depth = AbyssUtils.getSystemData(defenseTarget.starSystem).depth
            if (Random().nextFloat() >= chancetoAddPer) continue
            defenseTarget.containingLocation.addScript(AbyssalDefendingFleetManager(defenseTarget, depth))
        }
    }

    fun generatePhotospheres(system: StarSystemAPI, max: Int, chanceToAddPer: Float, typePicker: WeightedRandomPicker<String>? = null) {

        var data = AbyssUtils.getSystemData(system)
        var first = true
        for (i in 0 until max)
        {
            if (first) first = false
            else if (Random().nextFloat() >= chanceToAddPer) continue

            if (data.majorPoints.isEmpty()) return
            var pos = data.majorPoints.randomAndRemove()

            var photosphere = system.addCustomEntity("rat_abyss_photosphere_${Misc.genUID()}", "Photosphere", "rat_abyss_photosphere", Factions.NEUTRAL)
            photosphere.setLocation(pos.x, pos.y)
            photosphere.radius = 100f
            photosphere.addScript(DiscoveredPhotosphere(10, photosphere))

            var plugin = photosphere.customPlugin as AbyssalPhotosphere
            // plugin.radius = 15000f
            plugin.radius = MathUtils.getRandomNumberInRange(12500f, 15000f)
            plugin.color = data.color

            var picker = WeightedRandomPicker<String>()
            if (typePicker != null) {
                for (item in typePicker.items) {
                    var weight = typePicker.getWeight(item)
                    picker.add(item, weight)
                }
            }
            else {
                picker.add("rat_abyss_fabrication", 2f)
                picker.add("rat_abyss_drone", 2f)
                picker.add("rat_abyss_drone", 2f)
                picker.add("rat_abyss_drone", 2f)
                picker.add("rat_abyss_drone", 2f)
                picker.add("rat_abyss_transmitter", 2f)
            }

            var added = 0
            var orbit = MathUtils.getRandomNumberInRange(50f, 100f)
            var days = 30f
            for (i in 0 until 3)
            {
                orbit += MathUtils.getRandomNumberInRange(200f, 350f)
                days += 35
                if (Random().nextFloat() > 0.25f)
                {
                    var entityType = picker.pickAndRemove()
                    var entity = spawnMinorEntity(system, entityType)

                    entity.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), orbit, days)
                    added++
                }
            }

            if (added == 0)
            {
                var entityType = picker.pickAndRemove()
                var entity = spawnMinorEntity(system, entityType)

                entity.setCircularOrbit(photosphere, MathUtils.getRandomNumberInRange(0f, 360f), MathUtils.getRandomNumberInRange(300f, 1000f), 90f)
            }

            var defenseChance = 0.75f
            addDefenseFleetManager(photosphere, 2, defenseChance)

        }

    }

    fun addDerelictAbyssalShips(system: StarSystemAPI, max: Int, chanceToAddPer: Float) {
        for (i in 0 until max) {
            if (Random().nextFloat() >= chanceToAddPer) continue

            val faction: String = "rat_abyssals"
            val params = DerelictShipEntityPlugin.createRandom(faction, null, Random(), DerelictShipEntityPlugin.getDefaultSModProb())

            var data = AbyssUtils.getSystemData(system)
            var points = data.minorPoints

            if (params != null) {
                if (points.isEmpty()) continue

                val entity = BaseThemeGenerator.addSalvageEntity(Random(), system, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI
                entity.setDiscoverable(true)

                var loc = points.randomAndRemove()
                entity.location.set(loc)
                entity.addTag(AbyssTags.ABYSS_WRECK)
            }
        }
    }

    fun generateMinorEntity(system: StarSystemAPI, type: String, max: Int, chanceToAddPer: Float) : ArrayList<SectorEntityToken> {

        var data = AbyssUtils.getSystemData(system)

        var list = ArrayList<SectorEntityToken>()

        for (i in 0 until max) {
            if (Random().nextFloat() >= chanceToAddPer) continue

            if (data.minorPoints.isEmpty()) continue
            var point = data.minorPoints.randomAndRemove()

            var entity = spawnMinorEntity(system, type)
            entity.location.set(point)
            list.add(entity)

        }

        return list


    }

    fun generateMinorEntityWithDefenses(system: StarSystemAPI, type: String, max: Int, chanceToAddPer: Float, chanceToAddDefense: Float) {
        var entities = generateMinorEntity(system, type, max, chanceToAddPer)
        for (entity in entities) {
            addDefenseFleetManager(entity, 1, chanceToAddDefense)
        }
    }

    fun addDefenses() {

    }

}