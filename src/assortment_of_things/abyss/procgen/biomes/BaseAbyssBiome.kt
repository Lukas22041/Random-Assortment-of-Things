package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

abstract class BaseAbyssBiome {

    abstract fun getBiomeID() : String

    abstract fun getBiomeColor() : Color

}