package assortment_of_things.abyss.procgen.types

import assortment_of_things.abyss.procgen.*

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





        AbyssEntityGenerator.generatePhotospheres(system, 3, 0.8f)
        AbyssEntityGenerator.generateMinorEntity(system, "rat_abyss_transmitter", 1, 1f)
        AbyssEntityGenerator.generateMinorEntityWithDefenses(system, "rat_abyss_fabrication", fabricators, 0.9f, 0.7f)
        AbyssEntityGenerator.generateMinorEntity(system, "rat_abyss_drone", 4, 0.6f)

        AbyssEntityGenerator.addDerelictAbyssalShips(system, 4, 0.6f)
    }
}