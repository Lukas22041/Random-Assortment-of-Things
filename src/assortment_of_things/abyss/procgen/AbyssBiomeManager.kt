package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.procgen.biomes.BiomeCellData
import assortment_of_things.abyss.procgen.biomes.TestBiome
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
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

    var biomes = ArrayList<BaseAbyssBiome>()

    //Get the lower left coordinate of a cell in World coordinates
    fun toWorldX(gridX: Int) = (gridX * cellSize - xOffset).toFloat()
    fun toWorldY(gridY: Int) = (gridY * cellSize - yOffset).toFloat()
    fun toWorld(gridX: Int, gridY: Int) = Vector2f(toWorldX(gridX), toWorldY(gridY))

    //Convert the location of something in-world to the index of the cell their in.
    fun toGridX(worldX: Float) = ((worldX + xOffset) / 2000).toInt()
    fun toGridY(worldY: Float) = ((worldY + yOffset) / 2000).toInt()

    private val cellArray = Array(rows) { row ->
        Array<BiomeCellData>(columns) {
            column -> BiomeCellData(this, row, column, toWorldX(row), toWorldY(column))
        }
    }
    private var cellList = cellArray.flatten()

    //var cells = ArrayList<ArrayList>

    fun init() {

        biomes.clear()

        biomes.add(TestBiome("rat_test1", Color(255, 0, 50)))
        biomes.add(TestBiome("rat_test2", Color(120, 0, 20)))
        biomes.add(TestBiome("rat_test3", Color(255, 0, 100)))
        biomes.add(TestBiome("rat_test4", Color(155, 75, 0)))
        biomes.add(TestBiome("rat_test5", Color(50, 50, 50)))

        //var cells = cellArray.sumOf { it.size }

        //TODO Remove
        //cellList.forEach { it.color = AbyssUtils.ABYSS_COLOR.darker().darker() }



        var starts = findStartingPoints()
        generateBiomes(/*starts*/)

    }

    fun determineBordersAndDepth() {

    }

    fun generateBiomes(/*cellsToExpandFrom: ArrayList<BiomeCellData>*/) /*: ArrayList<BiomeCellData>*/ {
        //var list = ArrayList<BiomeCellData>()

        //if (cellsToExpandFrom.isEmpty()) return


        /*var candidates = ArrayList<BiomeCellData>()

        for (cell in cellsToExpandFrom) {
            var adjacent = cell.getEmptyAdjacent()
            var pick = adjacent.randomOrNull()

            if (pick != null) {
                pick.setBiome(cell.getBiome()!!)
                candidates.add(pick)
            }
        }

        generateBiomes(candidates)*/




        if (cellList.none { it.getBiome() == null }) return

        for (biome in biomes) {
            var available = biome.cells.flatMap { it.getEmptyAdjacent() }
            var pick = available.randomOrNull() ?: continue //Stop if no cells are available

            pick.setBiome(biome)
        }

        generateBiomes()

        /* for (start in starts) {
             for (adjacent in start.getAdjacent()) {
                 adjacent.setBiome(start.getBiome()!!)
             }
         }*/


    }

    //Attempt to make starting cells situated far enough from eachother to give better starting situations for biome gen.
    fun findStartingPoints() : ArrayList<BiomeCellData> {
        var startingPoints = ArrayList<BiomeCellData>()

      /*  var first = cellList.random()
        //var first = getCell(rows/2, columns/2)
        startingPoints.add(first)*/


        //Pick random points,check if their distance meets the minimum, if not, lower the minimum and then check another patch of points.
        for (biome in biomes) {
            //var minDist = height /*/ 2f*/
            var minDist = width /*/ 2f*/

            var cell: BiomeCellData? = null
            if (startingPoints.isEmpty()) {
                cell = cellList.random()
            } else {
               cell = findPointRecursive(minDist.toFloat(), startingPoints)
            }

            cell.setBiome(biome)
            cell.isStartingPoint = true
            startingPoints.add(cell)
        }

        /*for (cell in startingPoints) {
            //cell.color = Misc.getPositiveHighlightColor()
        }*/

        return startingPoints
    }

    fun findPointRecursive(minDistance: Float, startingCells: ArrayList<BiomeCellData>) : BiomeCellData {
        var cell: BiomeCellData? = null

        var attempts = 10

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
        var x = toGridX(worldX)
        var y = toGridY(worldY)

        //Fixes an issue with my dumb math for when the cell is out of bounds, since cells on the left & bottom can report something from a wrong coordinate
        if (worldX < -xOffset || worldY < -yOffset) {
            var xOff = 0
            var yOff = 0
            if (worldX < -xOffset) xOff = 1
            if (worldY < -yOffset) yOff = 1

            var fake = createFakeCell(x-xOff, y-yOff)
            fake.worldX -= 2000 * xOff
            fake.worldY -= 2000 * yOff

            return fake
        }

        return getCell(x, y)
    }

    fun getCell(gridX: Int, gridY: Int) : BiomeCellData {
        var cell = cellArray.getOrNull(gridX)?.getOrNull(gridY)
        if (cell == null) cell = createFakeCell(gridX, gridY)
        return cell
    }

    fun createFakeCell(x: Int, y: Int) : BiomeCellData {
        var cell = BiomeCellData(this, x, y,toWorldX(x), toWorldY(y))
        cell.isFake = true
        //cell.color = Color.white
        return cell
    }
}