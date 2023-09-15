package assortment_of_things.abyss.procgen.types

import assortment_of_things.abyss.procgen.AbyssSystemData

abstract class BaseAbyssType {

    abstract fun getWeight() : Float

    abstract fun pregenerate(data: AbyssSystemData)

    abstract fun generate(data: AbyssSystemData)

}