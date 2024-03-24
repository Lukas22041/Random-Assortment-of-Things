package assortment_of_things.abyss.procgen.types

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.*
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class ColossalPhotosphereType : BaseAbyssType() {


    companion object {
        var LARGE_TAG = "rat_abyss_colossal"
    }

    override fun getWeight() : Float{
        return 0.40f
    }

    override fun getTerrainFraction(): Float {
        return 0.35f
    }

    override fun pregenerate(data: AbyssSystemData) {
        var system = data.system

        var colossal = system.addPlanet("rat_colossal_photosphere_${Misc.genUID()}", system.center, "Colossal Photosphere", "rat_colossal_photosphere",
            0f, 1000f, 0f, 365f)

        colossal.lightColorOverrideIfStar = data.getColor()
        colossal.spec.planetColor = data.getColor()
        colossal.spec.atmosphereColor = data.getColor()
        colossal.spec.iconColor = data.getColor()
        colossal.applySpecChanges()

        colossal.name = "Colossal Photosphere"
        colossal.addTag(Tags.NON_CLICKABLE)

        AbyssProcgen.addLightsource(colossal, 90000f, data.getColor().setAlpha(50))
        AbyssProcgen.clearTerrainAround(colossal, 1500f)

        var amount = 7

        var emptySlots = ArrayList<Vector2f>()

        var lastSlot = Vector2f(0f, 0f)
        var distance = MathUtils.getRandomNumberInRange(2500f, 3000f)
        for (i in 0 until amount)
        {
            var slot = MathUtils.getRandomPointOnCircumference(colossal.location, distance)

            lastSlot = slot
            emptySlots.add(slot)

            distance += MathUtils.getRandomNumberInRange(600f, 1200f)
        }

        for (i in 0 until 3) data.fracturePoints.add(emptySlots.randomAndRemove())
        for (i in 0 until 3) data.majorPoints.add(emptySlots.randomAndRemove())
        data.uniquePoints.add(emptySlots.randomAndRemove())

        //AbyssProcgen.generateCircularPoints(system)
        AbyssProcgen.generateMinorPoints(system)

    }

    override fun generate(data: AbyssSystemData) {
        var system = data.system
        AbyssProcgen.addAbyssParticles(system)
        system.addTag(LARGE_TAG)

        var fabricators = 2
        if (data.depth == AbyssDepth.Deep) fabricators = 3

        //AbyssEntityGenerator.generateMajorLightsource(system, 3, 0.8f)
        AbyssEntityGenerator.generateMinorEntity(system, "rat_abyss_transmitter", 1, 1f)
        AbyssEntityGenerator.generateMinorEntityWithDefenses(system, "rat_abyss_fabrication", fabricators, 0.9f, 0.7f)
        AbyssEntityGenerator.generateMinorEntity(system, "rat_abyss_drone", 6, 0.6f)

        AbyssEntityGenerator.addDerelictAbyssalShips(system, 4, 0.6f)
    }

    override fun setupColor(data: AbyssSystemData) {
        var h = MathUtils.getRandomNumberInRange(0.925f, 1f)
        if (Random().nextFloat() > 0.5f) h = MathUtils.getRandomNumberInRange(0.0f, 0.035f)
        var color = Color.getHSBColor(h, 1f, 1f)

        var depth = data.depth
        var s = 1f
        var b = 1f
        b = when (depth) {
            AbyssDepth.Shallow -> 0.3f
            AbyssDepth.Deep -> 0.2f
        }

        var darkColor = Color.getHSBColor(h, s, b)

        data.baseColor = color
        data.baseDarkColor = darkColor
    }

}