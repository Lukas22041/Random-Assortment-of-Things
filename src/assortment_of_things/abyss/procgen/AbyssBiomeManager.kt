package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbyssBiomeManager {

    companion object {
        var width = 52000 * 2 //*3
        var height = 52000 * 1 //*2
        var cellSize = 2000

        var rows = width / cellSize
        var columns = height / cellSize

        var xOffset = width / 2
        var yOffset = height / 2
    }


    class BiomeCellData(var worldX: Float, var worldY: Float) {
        var isFake = false
        var color = AbyssUtils.ABYSS_COLOR.darker().darker()

        fun getCenter() : Vector2f {
            return Vector2f(worldX + cellSize / 2f, worldY + cellSize / 2f)
        }
    }

    private val cellArray = Array(rows) { row ->
        Array<BiomeCellData>(columns) {
            column -> BiomeCellData((row * cellSize - xOffset).toFloat(), (column * cellSize - yOffset).toFloat())
        }
    }
    private var cellList = cellArray.flatten()

    //var cells = ArrayList<ArrayList>

    fun init() {

        //var cells = cellArray.sumOf { it.size }

        //TODO Remove
        //cellList.forEach { it.color = AbyssUtils.ABYSS_COLOR.darker().darker() }


        var points = findStartingPoints()

        var test = ""

    }

    //Attempt to make starting cells situated far enough from eachother to give better starting situations for biome gen.
    fun findStartingPoints() : ArrayList<BiomeCellData> {
        var startingPoints = ArrayList<BiomeCellData>()

        var first = cellList.random()
        //var first = getCell(rows/2, columns/2)
        startingPoints.add(first)

        var points = 4

        //Pick random points,check if their distance meets the minimum, if not, lower the minimum and then check another patch of points.
        for (i in 0 until points) {
            //var minDist = height /*/ 2f*/
            var minDist = width /*/ 2f*/
            var cell = findPointRecursive(minDist.toFloat() ,startingPoints)
            startingPoints.add(cell)
        }

        /*for (i in 0 until points) {
            //Cell, Distance
            //var cells = ArrayList<Pair<BiomeCellData, Float>>()
            var cells = WeightedRandomPicker<BiomeCellData>()

            for (cell in cellList) {
                if (startingPoints.contains(cell)) continue //Skip starting points

                var distance = 0f
                var toClose = false
                for (start in startingPoints) {
                    var dist = MathUtils.getDistance(Vector2f(start.worldX, start.worldY), Vector2f(cell.worldX, cell.worldY))
                    distance += dist
                    if (dist <= 5000) {
                        toClose = true
                    }
                }
                if (!toClose) {
                    cells.add(cell, distance)
                }
            }

            var toRemove = ArrayList<BiomeCellData>()
            var sorted = ArrayList(cells.items.sortedBy { cells.getWeight(it)  })
            for (cell in sorted) {
                var index = sorted.indexOf(cell)
                if (index <= cells.items.size / 2) {
                    var weight = cells.getWeight(cell)
                    toRemove.add(cell)
                }
            }

            toRemove.forEach { cells.remove(it) }

            var pick = cells.pick()
            startingPoints.add(pick)
        } */

        for (cell in startingPoints) {
            cell.color = Misc.getPositiveHighlightColor()
        }

        return startingPoints
    }

    fun findPointRecursive(minDistance: Float, startingCells: ArrayList<BiomeCellData>) : BiomeCellData {
        var cell: BiomeCellData? = null

        var attempts = 10

       /* for (i in 0 until 10) {
            cell = cellList.filter { !startingCells.contains(it) }.random()

            for (start in startingCells) {
                var dist = MathUtils.getDistance(Vector2f(start.worldX, start.worldY), Vector2f(cell.worldX, cell.worldY))
                if (dist )
            }
        }*/

        cell = cellList.filter { !startingCells.contains(it) }.random()

        var failed = false
        for (start in startingCells) {
            var dist = MathUtils.getDistance(Vector2f(start.worldX, start.worldY), Vector2f(cell.worldX, cell.worldY))
            if (dist <= minDistance) {
                failed = true
                break
            }
        }
        if (failed) {
            cell = findPointRecursive(minDistance-2000f, startingCells)
        }

        return cell
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