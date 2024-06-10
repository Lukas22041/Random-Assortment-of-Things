package assortment_of_things.abyss.rework

import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object AbyssBiomeGenerator {

    data class BiomeGridCell(var x: Int, var y: Int, var locX: Float, var locY: Float, var size: Float, var biomeID: String, var color: Color) {
        var colorTopLeft = Color(20, 0, 0)
        var colorTopRight = Color(20, 0, 0)
        var colorBottomLeft = Color(20, 0, 0)
        var colorBottomRight = Color(20, 0, 0)
    }

    var biomeGrid = HashMap<Int, HashMap<Int, BiomeGridCell>>()

    fun getBiomeCellList() = biomeGrid.flatMap { it.value.map { it.value } }

    var cellSize = 1600f

    var mapHorizontalSize = 56000 * 2
    var mapVerticalSize = 56000

    var cellCountHorizontal = (mapHorizontalSize / cellSize).toInt()
    var cellCountVertical = (mapVerticalSize / cellSize).toInt()
    //var cellSize = 4000f

    var biomes = 5

    var biomeColors = listOf(
        Color(200, 0, 0),
        Color(155, 0, 0),
        Color(40, 0, 0),
        Color(150, 0, 150),
        Color(120, 56, 13),
        )

    fun generate() {
        biomeGrid.clear()

        var offsetX = (mapHorizontalSize / 2)
        var offsetY = (mapVerticalSize / 2)

        for (x in 0 until  cellCountHorizontal) {
            var yMap = HashMap<Int, BiomeGridCell>()
            for (y in 0 until cellCountVertical) {

                var locX = cellSize * x - offsetX
                var locY = cellSize * y - offsetY

                var cell = BiomeGridCell(x, y, locX, locY, cellSize, "", Misc.getTextColor())
                yMap.put(y, cell)
            }
            biomeGrid.put(x, yMap)
        }

        for (i in 0 until biomes) {
            createBlob(i)
        }

        var biomeCellList = getBiomeCellList()

        while (biomeCellList.any { it.biomeID == "" }) {

            var empties = biomeCellList.filter { it.biomeID == "" }

            for (empty in empties) {
                var adjacent = getAdjacentFilledCell(empty) ?: continue
                empty.biomeID = adjacent.biomeID
                empty.color = adjacent.color
            }
           for (cell in biomeCellList.filter { it.biomeID != "" }) {
               var adjacent = getAdjacentEmptyCell(cell) ?: continue
               adjacent.biomeID = cell.biomeID
               adjacent.color = cell.color
           }
        }

        for (cell in biomeCellList) {

            if (cell.biomeID == "") continue

            var color = cell.color

            cell.colorTopLeft = color
            cell.colorTopRight = color
            cell.colorBottomLeft = color
            cell.colorBottomRight = color

            if (getAdjacent(cell, 0, 1).biomeID != cell.biomeID) {
                cell.colorTopLeft = Color(20, 0, 0, 255)
                cell.colorTopRight = Color(20, 0, 0, 255)
            }

            if (getAdjacent(cell, 0, -1).biomeID != cell.biomeID) {
                cell.colorBottomLeft = Color(20, 0, 0, 255)
                cell.colorBottomRight = Color(20, 0, 0, 255)
            }

            if (getAdjacent(cell, -1, 0).biomeID != cell.biomeID) {
                cell.colorTopLeft = Color(20, 0, 0, 255)
                cell.colorBottomLeft = Color(20, 0, 0, 255)
            }

            if (getAdjacent(cell, 1, 0).biomeID != cell.biomeID) {
                cell.colorTopRight = Color(20, 0, 0, 255)
                cell.colorBottomRight = Color(20, 0, 0, 255)
            }

            if (getAdjacent(cell, 1, 1).biomeID != cell.biomeID) {
                cell.colorTopRight = Color(20, 0, 0, 255)
            }

            if (getAdjacent(cell, 1, -1).biomeID != cell.biomeID) {
                cell.colorBottomRight = Color(20, 0, 0, 255)
            }

            if (getAdjacent(cell, -1, 1).biomeID != cell.biomeID) {
                cell.colorTopLeft = Color(20, 0, 0, 255)
            }

            if (getAdjacent(cell, -1, -1).biomeID != cell.biomeID) {
                cell.colorBottomLeft = Color(20, 0, 0, 255)
            }

           /* cell.colorTopLeft = Misc.interpolateColor(getAdjacent(cell, -1, 0).color, getAdjacent(cell, 0, 1).color, 0.5f)
            cell.colorTopRight = Misc.interpolateColor(getAdjacent(cell, 1, 0).color, getAdjacent(cell, 0, 1).color, 0.5f)
            cell.colorBottomLeft = Misc.interpolateColor(getAdjacent(cell, -1, 0).color, getAdjacent(cell, 0, -1).color, 0.5f)
            cell.colorBottomRight = Misc.interpolateColor(getAdjacent(cell, 1, 0).color, getAdjacent(cell, 0, -1).color, 0.5f)*/
        }
    }

    fun createBlob(biomeNr: Int) {
        var cellsSoFar = ArrayList<BiomeGridCell>()
        //var color = Color.getHSBColor(Random().nextFloat(), MathUtils.getRandomNumberInRange(0.7f, 1f), MathUtils.getRandomNumberInRange(0.7f, 1f))
        var color = biomeColors.get(biomeNr)
        var id = "Test_${Misc.genUID()}"
        //var max = MathUtils.getRandomNumberInRange(140, 150)
        var max = ((cellCountHorizontal * cellCountVertical) * 0.9f / biomes).toInt()
        var center = getBiomeCellList().filter { it.biomeID == "" }.randomOrNull() ?: return

        cellsSoFar.add(center)
        center.color = color
        center.biomeID = id

        var index = 0
        for (i in 0 until max) {
            var continueCell = selectCellToContinueFrom(Vector2f(center.x.toFloat(), center.y.toFloat()), cellsSoFar) ?: continue
            var cell = getAdjacentEmptyCell(continueCell) ?: continue

            cell.color = color
            cell.biomeID = id

            cellsSoFar.add(cell)
        }
    }

    fun getAdjacent(cell: BiomeGridCell, loc: Vector2f) : BiomeGridCell {
        var x = loc.x.toInt()
        var y = loc.y.toInt()
        return getAdjacent(cell, x, y)
    }

    fun getAdjacent(cell: BiomeGridCell, x: Int, y: Int) : BiomeGridCell {
        var adjacent = biomeGrid.get(cell.x + x)?.get(cell.y + y)
        if (adjacent == null) {
            adjacent = BiomeGridCell(cell.x + x , cell.y + y, 0f, 0f, cell.size, "", Color(0, 0, 0))
        }
        return adjacent
    }

    fun selectCellToContinueFrom(center: Vector2f, cellsSoFar: List<BiomeGridCell>) : BiomeGridCell? {
        var farthest = cellsSoFar.map { Vector2f(it.x.toFloat(), it.y.toFloat()) }.sortedBy { MathUtils.getDistance(it, center) }.last()
        var maxDistance = MathUtils.getDistance(center, farthest) + 1

        var cellsWithNeighbour = cellsSoFar.filter { hasAdjacentEmptyCell(it) } ?: return null
        var picker = WeightedRandomPicker<BiomeGridCell>()
        for (cell in cellsWithNeighbour) {
            var loc = Vector2f(cell.x.toFloat(), cell.y.toFloat())
            var distance = MathUtils.getDistance(loc, center)
            distance + 1
            var level = distance / maxDistance
            level = 1 - level + 0.2f
                    picker.add(cell, 1f)
        }
        var pick = picker.pick()
        return pick
    }

    fun hasAdjacentEmptyCell(cell: BiomeGridCell) : Boolean {
        return getAdjacentEmptyCell(cell) != null
    }

    fun getAdjacentEmptyCell(cell: BiomeGridCell) : BiomeGridCell? {
        var x = cell.x
        var y = cell.y

        var picker = WeightedRandomPicker<BiomeGridCell>()

        var left = biomeGrid.get(x - 1)?.get(y)
        if (left != null && left.biomeID == "") {
            picker.add(left)
        }

        var right = biomeGrid.get(x + 1)?.get(y)
        if (right != null && right.biomeID == "") {
            picker.add(right)
        }

        var top = biomeGrid.get(x)?.get(y + 1)
        if (top != null && top.biomeID == "") {
            picker.add(top)
        }

        var bottom = biomeGrid.get(x)?.get(y - 1)
        if (bottom != null && bottom.biomeID == "")  {
            picker.add(bottom)
        }

        var pick = picker.pick()
        return pick
    }

    fun getAdjacentFilledCell(cell: BiomeGridCell) : BiomeGridCell? {
        var x = cell.x
        var y = cell.y

        var picker = WeightedRandomPicker<BiomeGridCell>()

        var left = biomeGrid.get(x - 1)?.get(y)
        if (left != null && left.biomeID != "") {
            picker.add(left)
        }

        var right = biomeGrid.get(x + 1)?.get(y)
        if (right != null && right.biomeID != "") {
            picker.add(right)
        }

        var top = biomeGrid.get(x)?.get(y + 1)
        if (top != null && top.biomeID != "") {
            picker.add(top)
        }

        var bottom = biomeGrid.get(x)?.get(y - 1)
        if (bottom != null && bottom.biomeID != "")  {
            picker.add(bottom)
        }

        var pick = picker.pick()
        return pick
    }
}