package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.*
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbyssBiomeManager : EveryFrameScript {


    var cellSize = 2000f

    var mapHorizontalSize = 56000 * 2
    var mapVerticalSize = 56000

    var cellCountHorizontal = (mapHorizontalSize / cellSize).toInt()
    var cellCountVertical = (mapVerticalSize / cellSize).toInt()

    var biomes = listOf<BaseAbyssBiome>(
        /*BaseAbyssBiome("shore", "Abyssal Shore", Color(200, 0, 0), Color(200, 0, 0)),
        BaseAbyssBiome("sea_of_tranquility", "Sea of Tranquillity", Color(155, 0, 0), Color(155, 0, 0)),
        BaseAbyssBiome("abyssal_waste", "Abyssal Wastes", Color(40, 0, 0), Color(100, 0, 0)),
        BaseAbyssBiome("biome5", "Sea of Serenity", Color(252, 137, 3), Color(200, 90, 3))*/
        SeaOfTranquillity(),
        OceanOfStorms(),
        AbyssalWastes(),
        SeaOfSerenity(),
    )

    var grid: Array<Array<BiomeCell>> = Array(cellCountHorizontal) { x ->
        Array(cellCountVertical) {y ->
            BiomeCell(x, y, cellSize)
        }
    }

    var cells: List<BiomeCell> = grid.flatMap { it.asList() }

    var interval = IntervalUtil(0.1f, 0.1f)

    override fun advance(amount: Float) {

        interval.advance(amount)

        if (interval.intervalElapsed()) {
            if (!Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG)) return

            var playerCell = getPlayerCell()
          /*  var adjacentPlayerCells = playerCell.getAdjacentCells()
            var surroundingPlayerCells = playerCell.getSurroundingCells()

            var combined = surroundingPlayerCells.plus(playerCell)*/

            var cellsInArea = getCellsInRadius(playerCell, 2)

            for (cell in cellsInArea) {
                cell.isDiscovered = true

                if (playerCell == cell) {
                    var biome = getCellsBiome(cell)

                    if (!biome!!.isDiscovered) {
                        Global.getSector().campaignUI.addMessage("New Biome Discovered:\n" +
                                " - ${biome.getName()}",
                        Misc.getTextColor(), "${biome.getName()}", "", biome.getLabelColor(), Misc.getHighlightColor())
                    }

                    biome!!.isDiscovered = true


                }

            }

            for (biome in biomes) {
                if (biome.isFullyDiscovered) continue

                var count = biome.cells.count { it.isDiscovered }
                if (count >= biome.cells.count() * 0.8f) {
                    biome.cells.forEach { it.isDiscovered = true }
                    biome.isFullyDiscovered = true

                    Global.getSector().campaignUI.addMessage("The ${biome.getName()} biome has been fully mapped!",
                        Misc.getTextColor(), "${biome.getName()}", "", biome.getLabelColor(), Misc.getHighlightColor())
                }
            }
        }
    }

    fun getCellsInRadius(center: BiomeCell, radius: Int) : Set<BiomeCell> {
        var list = HashSet<BiomeCell>()

        list.add(center)

        var centerX = center.x
        var centerY = center.y

        for (i in 0 until radius) {
            for (cell in HashSet(list)) {
                list.add(cell.getTM())
                list.add(cell.getLMid())
                list.add(cell.getRMid())
                list.add(cell.getBM())
            }
        }

        list = list.filter { !it.isFake }.toHashSet()

        return list
    }

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

        for (biome in biomes) {
            findBorders(biome)
        }

        for (biome in biomes) {
            biome.generate()
        }
    }





    fun createBlob(biome: BaseAbyssBiome) {
        var cellsSoFar = ArrayList<BiomeCell>()

        //var color = Color.getHSBColor(Random().nextFloat(), MathUtils.getRandomNumberInRange(0.7f, 1f), MathUtils.getRandomNumberInRange(0.7f, 1f))
        var color = biome.getColor()
        //var max = MathUtils.getRandomNumberInRange(140, 150)
        var max = ((cellCountHorizontal * cellCountVertical) * 0.85f / biomes.count()).toInt()
        //var max = ((cellCountHorizontal * cellCountVertical) * 0.8f / biomes.count()).toInt()
        //var max = ((cellCountHorizontal * cellCountVertical) * 0.9f / biomes.count()).toInt()
        var center = cells.filter { it.biomeId == "" && it.getAdjacentFilledCells().isEmpty() /*No Cell directly next to biomes, prevents spawning in gaps*/ }.randomOrNull() ?: return

        cellsSoFar.add(center)
        center.color = color
        center.biomeId = biome.getId()
       // biome.centralCell = center

        biome.cells.add(center)

        var alpha = 1f
        for (i in 0 until max) {




            var continueCell = selectCellToContinueBlobFrom(Vector2f(center.x.toFloat(), center.y.toFloat()), cellsSoFar) ?: continue
            var cell = continueCell.getAdjacentEmptyCell() ?: continue

            cell.color = color
            cell.biomeId = biome.getId()

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



    fun findBorders(biome: BaseAbyssBiome) {
        var bounds = ArrayList<Vector2f>()
        var biomeCells = biome.cells

        var direction = Vector2f(0.1f, -1f)
        var start = biomeCells.filter { getAdjacent(it, 1, 0).biomeId != it.biomeId }.random()
        var current = start

        var safetyBreak = 1000
        while (true) {

            current.isBorder = true

            safetyBreak--
            if (safetyBreak <= 1) {
                break
            }

            var next = getAdjacent(current, direction)
            //next.isBorder = true

            if (next == start) {
                break
            }

            var nextFront = getAdjacent(next, direction)
            var nextLeft = getAdjacent(next, Vector2f(direction).rotate(90f))
            if (next.biomeId == current.biomeId) {


                //If facing a tile that doesnt have a biome in front or the left, turn to the left
                if (nextLeft.biomeId == next.biomeId) {
                    current = nextLeft
                    if (current == start) { break }
                    direction = direction.rotate(90f)
                    continue
                }

                //Check if the tile left of the next tile is another biome, if so move forward
                if (nextLeft.biomeId != current.biomeId) {
                    current = next
                    if (current == start) { break }
                    continue
                }
            }
            if (next.biomeId != current.biomeId) {
                direction = direction.rotate(270f)
                continue
            }
        }
    }



    fun getCellsBiome(cell: BiomeCell) : BaseAbyssBiome? {
        return biomes.find { it.getId() == cell.biomeId }
    }


    fun getCell(x: Int, y: Int) : BiomeCell {
        if (x in 0 until cellCountHorizontal && y in 0 until cellCountVertical) {
            return grid[x][y]
        }

        var fakeCell = BiomeCell(x, y, cellSize)
        fakeCell.biomeId = ""
        fakeCell.isFake = true

        return fakeCell
    }

    fun getAdjacent(cell: BiomeCell, loc: Vector2f) : BiomeCell {
        var x = loc.x.toInt()
        var y = loc.y.toInt()
        return getAdjacent(cell, x, y)
    }

    fun getAdjacent(cell: BiomeCell, x: Int, y: Int) : BiomeCell {
        return getCell(cell.x + x, cell.y + y)
    }



    fun toRealCoordinate(cell: BiomeCell) : Vector2f {

        var horOffset = mapHorizontalSize / 2
        var verOffset = mapVerticalSize / 2

        var cellSize = cellSize

        var locX = cell.x.toFloat() * cellSize - horOffset
        var locY = cell.y.toFloat() * cellSize - verOffset

        var realLoc = Vector2f(locX, locY)
        return realLoc
    }

    fun getCellFromRealCoordinate(loc: Vector2f) : BiomeCell {
        var horOffset = mapHorizontalSize / 2
        var verOffset = mapVerticalSize / 2

        var x = ((loc.x+horOffset) / cellSize).toInt()
        var y = ((loc.y + verOffset) / cellSize).toInt()

        return getCell(x.toInt(), y.toInt())
    }

    fun getPlayerCell() : BiomeCell {
        var playerfleet = Global.getSector().playerFleet
        return getCellFromRealCoordinate(Global.getSector().playerFleet.location)
    }

    override fun isDone(): Boolean {
       return false
    }


    override fun runWhilePaused(): Boolean {
        return false
    }



}