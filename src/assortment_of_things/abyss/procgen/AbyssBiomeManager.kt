package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbyssBiomeManager {


    var cellSize = 2000f

    var mapHorizontalSize = 56000 * 2
    var mapVerticalSize = 56000

    var cellCountHorizontal = (mapHorizontalSize / cellSize).toInt()
    var cellCountVertical = (mapVerticalSize / cellSize).toInt()

    var biomes = listOf(
        AbyssBiome("shore", "Abyssal Shore", Color(200, 0, 0), Color(200, 0, 0)),
        AbyssBiome("sea_of_tranquility", "Sea of Tranquillity", Color(155, 0, 0), Color(155, 0, 0)),
        AbyssBiome("abyssal_waste", "Abyssal Wastes", Color(40, 0, 0), Color(100, 0, 0)),
        AbyssBiome("biome5", "Sea of Serenity", Color(252, 137, 3), Color(200, 90, 3))
    )

    var grid: Array<Array<BiomeCell>> = Array(cellCountHorizontal) { x ->
        Array(cellCountVertical) {y ->
            BiomeCell(x, y, cellSize)
        }
    }

    var cells: List<BiomeCell> = grid.flatMap { it.asList() }

    fun generate() {
        var data = AbyssUtils.getAbyssData()

        for (biome in biomes) {
            createBlob(biome)
        }

        while (cells.any { it.biomeId == "" }) {

            var empties = cells.filter { it.biomeId == "" }

            for (empty in empties) {
                var adjacent = empty.getAdjacentFilledCell() ?: continue
                empty.biomeId = adjacent.biomeId
                empty.color = adjacent.color
                empty.spriteAlpha = adjacent.spriteAlpha

                var biome = getCellsBiome(empty)?.cells?.add(empty)
            }

            for (cell in cells.filter { it.biomeId != "" }) {
                var adjacent = cell.getAdjacentEmptyCell() ?: continue
                adjacent.biomeId = cell.biomeId
                adjacent.color = cell.color
                adjacent.spriteAlpha = adjacent.spriteAlpha

                var biome = getCellsBiome(adjacent)?.cells?.add(adjacent)
            }
        }

        for (biome in biomes) {
            cells = biome.cells
            var sortedX = cells.map { it.x }.sorted()
            var xSize = sortedX.size
            var medianX = (sortedX[xSize/2] + sortedX[(xSize-1)/2]) / 2

            var sortedY = cells.map { it.y }.sorted()
            var ySize = sortedY.size
            var medianY = (sortedY[ySize/2] + sortedY[(ySize-1)/2]) / 2

            biome.centralCell = getCell(medianX, medianY)

          /*  var xAverage = cells.map { it.x }.average().toInt()
            var yAverage = cells.map { it.y }.average().toInt()
            biome.centralCell = getCell(xAverage, yAverage)*/
        }
    }

    fun getCellsBiome(cell: BiomeCell) : AbyssBiome? {
        return biomes.find { it.id == cell.biomeId }
    }


    fun createBlob(biome: AbyssBiome) {
        var cellsSoFar = ArrayList<BiomeCell>()

        //var color = Color.getHSBColor(Random().nextFloat(), MathUtils.getRandomNumberInRange(0.7f, 1f), MathUtils.getRandomNumberInRange(0.7f, 1f))
        var color = biome.color
        //var max = MathUtils.getRandomNumberInRange(140, 150)
        var max = ((cellCountHorizontal * cellCountVertical) * 0.7f / biomes.count()).toInt()
        //var max = ((cellCountHorizontal * cellCountVertical) * 0.8f / biomes.count()).toInt()
        //var max = ((cellCountHorizontal * cellCountVertical) * 0.9f / biomes.count()).toInt()
        var center = cells.filter { it.biomeId == "" }.randomOrNull() ?: return

        cellsSoFar.add(center)
        center.color = color
        center.biomeId = biome.id
       // biome.centralCell = center

        biome.cells.add(center)

        var alpha = 1f
        for (i in 0 until max) {




            var continueCell = selectCellToContinueBlobFrom(Vector2f(center.x.toFloat(), center.y.toFloat()), cellsSoFar) ?: continue
            var cell = continueCell.getAdjacentEmptyCell() ?: continue

            cell.color = color
            cell.biomeId = biome.id

            //Reduce alpha further away from biome core
          /*  var level = i.toFloat().levelBetween(0f, max.toFloat())
            level *= level
            var cellAlpha = alpha - (0.9f * level)
            cell.spriteAlpha = cellAlpha*/


            biome.cells.add(cell)
            cellsSoFar.add(cell)
        }
    }

    fun selectCellToContinueBlobFrom(center: Vector2f, cellsSoFar: List<BiomeCell>) : BiomeCell? {
        var farthest = cellsSoFar.map { Vector2f(it.x.toFloat(), it.y.toFloat()) }.sortedBy { MathUtils.getDistance(it, center) }.last()
        var maxDistance = MathUtils.getDistance(center, farthest) + 1

        var cellsWithNeighbour = cellsSoFar.filter { it.getAdjacentEmptyCells().isNotEmpty() } /*?: return null*/
        var picker = WeightedRandomPicker<BiomeCell>()
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




    fun getCell(x: Int, y: Int) : BiomeCell {
        if (x in 0 until cellCountHorizontal && y in 0 until cellCountVertical) {
            return grid[x][y]
        }

        var fakeCell = BiomeCell(0, 0, 0f)
        fakeCell.isFake = true

        return fakeCell
    }


    fun getPlayerCell() : BiomeCell {
        var playerfleet = Global.getSector().playerFleet


        var horOffset = mapHorizontalSize / 2
        var verOffset = mapVerticalSize / 2

        var loc = playerfleet.location
        var x = ((loc.x+horOffset) / cellSize).toInt()
        var y = ((loc.y + verOffset) / cellSize).toInt()

       /* if (x in 0 until cellCountHorizontal && y in 0 until cellCountVertical) {
            return grid[x][y]
        }

        var fakeCell = BiomeCell(0, 0, 0f)
        fakeCell.isFake = true*/

        return getCell(x, y)
    }


}