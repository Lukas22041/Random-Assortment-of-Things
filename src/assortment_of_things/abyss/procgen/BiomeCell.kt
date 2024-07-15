package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

data class BiomeCell(var x: Int, var y: Int, var size: Float) {

    var isFake = false

    var spriteAngle = MathUtils.getRandomNumberInRange(0f, 360f)
    var spriteAlpha = MathUtils.getRandomNumberInRange(0.6f, 1f)

    var isBorder = false

    var biomeId = ""
    var color = Color(0, 0, 0)



    fun getTL() = AbyssUtils.getBiomeManager().getCell(x-1, y+1)
    fun getTM() = AbyssUtils.getBiomeManager().getCell(x, y+1)
    fun getTR() = AbyssUtils.getBiomeManager().getCell(x+1, y+1)

    fun getLMid() = AbyssUtils.getBiomeManager().getCell(x-1, y)
    fun getRMid() = AbyssUtils.getBiomeManager().getCell(x+1, y)

    fun getBL() = AbyssUtils.getBiomeManager().getCell(x-1, y-1)
    fun getBM() = AbyssUtils.getBiomeManager().getCell(x, y-1)
    fun getBR() = AbyssUtils.getBiomeManager().getCell(x+1, y-1)

    fun getAdjacentCells() : List<BiomeCell> {
        var cells = ArrayList<BiomeCell>()

        cells.add(getLMid())
        cells.add(getRMid())

        cells.add(getTM())
        cells.add(getBM())

        return cells
    }

    fun getAdjacentEmptyCells() : List<BiomeCell> {
        var cells = getAdjacentCells()
        return cells.filter { it.biomeId == "" && !it.isFake }
    }
    fun getAdjacentEmptyCell() : BiomeCell? = getAdjacentEmptyCells().randomOrNull()

    fun getAdjacentFilledCells() : List<BiomeCell> {
        var cells = getAdjacentCells()
        return cells.filter { it.biomeId != "" && !it.isFake }
    }
    fun getAdjacentFilledCell() : BiomeCell? = getAdjacentFilledCells().randomOrNull()

    fun getSurroundingCells() : List<BiomeCell> {
        var cells = ArrayList<BiomeCell>()

        cells.add(getTL())
        cells.add(getTM())
        cells.add(getTR())

        cells.add(getLMid())
        cells.add(getRMid())

        cells.add(getBL())
        cells.add(getBM())
        cells.add(getBR())

        return cells
    }



}