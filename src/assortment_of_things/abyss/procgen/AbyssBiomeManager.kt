package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import java.awt.Color

class AbyssBiomeManager {

    var width = 52000 * 3
    var height = 52000 * 2
    var cellSize = 2000

    var rows = width / cellSize
    var columns = height / cellSize

    var xOffset = width / 2
    var yOffset = height / 2

    class BiomeCellData(var worldX: Float, var worldY: Float) {
        var isFake = false
        var color = AbyssUtils.ABYSS_COLOR.darker().darker()

    }

    private val cellArray = Array(rows) { row ->
        Array<BiomeCellData>(columns) {
            column -> BiomeCellData((row * cellSize - xOffset).toFloat(), (column * cellSize - yOffset).toFloat())
        }
    }
    private var cellList = cellArray.flatten()

    //var cells = ArrayList<ArrayList>

    fun init() {

        var cells = cellArray.sumOf { it.size }

        var test = ""

    }


    fun getCells() = cellList

    fun getPlayerCell() : BiomeCellData {
        var playerFleet = Global.getSector().playerFleet
        var loc = playerFleet.location
        return getCell(loc.x, loc.y)
    }

    fun getCell(worldX: Float, worldY: Float) : BiomeCellData {
        var x = ((worldX + xOffset) / 2000).toInt()
        var y = ((worldY + yOffset) / 2000).toInt()
        return getCell(x, y)
    }

    fun getCell(x: Int, y: Int) : BiomeCellData {
        var cell = cellArray.getOrNull(x)?.getOrNull(y)
        if (cell == null) cell = createFakeCell()
        return cell
    }

    fun createFakeCell() : BiomeCellData {
        var cell = BiomeCellData(0f, 0f)
        cell.isFake = true
        cell.color = Color.white
        return cell
    }
}