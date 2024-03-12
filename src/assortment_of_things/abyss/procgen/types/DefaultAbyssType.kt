package assortment_of_things.abyss.procgen.types

import assortment_of_things.abyss.procgen.*
import org.lazywizard.lazylib.MathUtils
import java.awt.Color
import java.util.*

class DefaultAbyssType : BaseAbyssType() {
    override fun getWeight() : Float{
        return 1f
    }

    override fun getTerrainFraction(): Float {
        return 0.35f
    }

    override fun pregenerate(data: AbyssSystemData) {
        var system = data.system
        AbyssProcgen.generateCircularPoints(system)
        AbyssProcgen.generateMinorPoints(system)

    }

    override fun generate(data: AbyssSystemData) {
        var system = data.system
        AbyssProcgen.addAbyssParticles(system)

        var fabricators = 1
        if (data.depth == AbyssDepth.Deep) fabricators = 2




        AbyssEntityGenerator.generateMajorLightsource(system, 3, 0.8f)
        AbyssEntityGenerator.generateMinorEntity(system, "rat_abyss_transmitter", 1, 1f)
        AbyssEntityGenerator.generateMinorEntityWithDefenses(system, "rat_abyss_fabrication", fabricators, 0.9f, 0.7f)
        AbyssEntityGenerator.generateMinorEntity(system, "rat_abyss_drone", 4, 0.6f)

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