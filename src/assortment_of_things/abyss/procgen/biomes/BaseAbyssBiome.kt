package assortment_of_things.abyss.procgen.biomes

import java.awt.Color

abstract class BaseAbyssBiome {

    var startingCell: BiomeCellData? = null
    var cells = ArrayList<BiomeCellData>()

    var borderCells = ArrayList<BiomeCellData>()
    var nearCells = ArrayList<BiomeCellData>()
    var deepCells = ArrayList<BiomeCellData>() //Also includes deepest cells

    var deepestCells = ArrayList<BiomeCellData>() //Should be worked with first as to be reserved for important things


    var gridAlphaMult = 1f

    abstract fun getBiomeID() : String

    abstract fun getBiomeColor() : Color
    abstract fun getDarkBiomeColor() : Color

    open fun generateTerrain() { }

}