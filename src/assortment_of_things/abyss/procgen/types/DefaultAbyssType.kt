package assortment_of_things.abyss.procgen.types

import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.procgen.AbyssSystemData

class DefaultAbyssType : BaseAbyssType() {
    override fun getWeight() : Float{
        return 1f
    }

    override fun pregenerate(data: AbyssSystemData) {
        var system = data.system
        AbyssProcgen.generateCircularSlots(system)

    }

    override fun generate(data: AbyssSystemData) {
        var system = data.system
        AbyssProcgen.addAbyssParticles(system)
    }

}