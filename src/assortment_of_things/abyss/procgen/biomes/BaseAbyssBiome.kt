package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

abstract class BaseAbyssBiome {

    var cells = ArrayList<BiomeCellData>()

    abstract fun getBiomeID() : String

    abstract fun getBiomeColor() : Color

}