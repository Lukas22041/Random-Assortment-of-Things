package assortment_of_things.abyss.procgen.templates

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

abstract class BaseAbyssSystem(var name: String, tier: AbyssProcgen.Tier) {

    var system: StarSystemAPI

    init {
        system = Global.getSector().createStarSystem(name)

        system.addTag(AbyssUtils.SYSTEM_TAG)
        var star = system.initNonStarCenter()
        system.generateAnchorIfNeeded()

        // AbyssBackgroundWarper(system, 8, 0.33f)

        AbyssUtils.setupTags(system)
        AbyssUtils.setTier(system, tier)
        AbyssUtils.generateBaseDetails(system, tier)

        generateSlots(tier)

        //  AbyssUtils.generateAbyssTerrain(system, 0.3f)

    }

    abstract fun generate() : StarSystemAPI

    fun generateSlots(tier: AbyssProcgen.Tier) : List<Vector2f> {

        var amount = 7
        if (tier == AbyssProcgen.Tier.Low) amount = 6

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

        system.memoryWithoutUpdate.set("\$rat_abyss_emptySlots", emptySlots)
        return emptySlots
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
}