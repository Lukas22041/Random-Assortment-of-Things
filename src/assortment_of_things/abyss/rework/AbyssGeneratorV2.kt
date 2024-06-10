package assortment_of_things.abyss.rework

import assortment_of_things.misc.ReflectionUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.starfarer.campaign.BaseLocation
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object AbyssGeneratorV2 {


    //data class BiomeGridCell(var x: Int, var y: Int, var size: Float, var biomeID: String, var color: Color, var innateOffset: Vector2f)

    var cellsPerRow = 40
    //var cellSize = 4000f
    var cellSize = 400f

    var maxCells = cellsPerRow * cellsPerRow
    var biomes = 6

    var offset = Vector2f(0 - cellSize * cellsPerRow / 2, 0 - cellSize * cellsPerRow / 2)

    //var biomeGrid = HashMap<Int, HashMap<Int, BiomeGridCell>>()
    //var biomeGridList = ArrayList<BiomeGridCell>()

    fun generate() {
        var system = Global.getSector().createStarSystem("The Abyssal Depths")

        system.initNonStarCenter()

        var playerFleet = Global.getSector().playerFleet
        var currentLocation = playerFleet.containingLocation
        var targetSystem = system

        currentLocation.removeEntity(playerFleet)
        targetSystem.addEntity(playerFleet)
        Global.getSector().setCurrentLocation(targetSystem)

        system.addTerrain("rat_abyssal_depthsV2", null)


        //var list = biomeGrid.flatMap { it.value.map { it.value } }

        AbyssBiomeGenerator.generate()

       /* biomeGrid.clear()
        biomeGridList.clear()


        var cells = ArrayList<Vector2f>()
        var cellStart = Vector2f(0f - cellSize * cellsPerRow / 2, 0f - cellSize * cellsPerRow / 2)
        for (x in 0 until  cellsPerRow) {
            var yMap = HashMap<Int, BiomeGridCell>()
            for (y in 0 until cellsPerRow) {
                var loc = Vector2f(x.toFloat() * cellSize, y.toFloat() * cellSize)
                loc = loc.plus(cellStart)
                var offsetX = MathUtils.getRandomNumberInRange(0.1f, 0.9f)
                var offsetY = MathUtils.getRandomNumberInRange(0.1f, 0.9f)
                var cell = BiomeGridCell(x, y, cellSize, "", Misc.getTextColor(), Vector2f(offsetX, offsetY))
                yMap.put(y, cell)
                biomeGridList.add(cell)
            }
            biomeGrid.put(x, yMap)
        }

        for (i in 0 until biomes) {
            createBlob()
        }


        while (biomeGridList.any { it.biomeID == "" }) {

            var empties = biomeGridList.filter { it.biomeID == "" }

            for (empty in empties) {
                var adjacent = getAdjacentFilledCell(empty) ?: continue
                empty.biomeID = adjacent.biomeID
                empty.color = adjacent.color
            }
            *//*for (cell in biomeGridList.filter { it.biomeID != "" }) {
                var adjacent = getAdjacentEmptyCell(cell) ?: continue
                adjacent.biomeID = cell.biomeID
                adjacent.color = cell.color
            }*//*
        }*/

    }

    /*fun getAdjacent(cell: BiomeGridCell, loc: Vector2f) : BiomeGridCell {
        var x = loc.x.toInt()
        var y = loc.y.toInt()
        return getAdjacent(cell, x, y)
    }

    fun getAdjacent(cell: BiomeGridCell, x: Int, y: Int) : BiomeGridCell {
        var adjacent = biomeGrid.get(cell.x + x)?.get(cell.y + y)
        if (adjacent == null) {
            adjacent = BiomeGridCell(cell.x + x , cell.y + y, cell.size, "", Misc.getHighlightColor(), Vector2f())
        }
        return adjacent
    }

    fun addBoundsFromCell(cell: BiomeGridCell, direction: Vector2f, bounds: ArrayList<Vector2f>) {
        //Make sure to later on edit in the offset
        var loc = Vector2f(cell.x.toFloat(), cell.y.toFloat())
        var left = getAdjacent(cell, Vector2f(direction).rotate(90f))

        *//*var currentLoc = Vector2f((cell.x.toFloat() + cell.innateOffset.x) * cellSize + offset.x, (cell.y.toFloat() + cell.innateOffset.y) * cellSize + offset.y)
        var leftLoc = Vector2f((left.x.toFloat() + left.innateOffset.x) * cellSize + offset.x, (left.y.toFloat() + left.innateOffset.y) * cellSize + offset.y)*//*

        var currentLoc = Vector2f(cell.x.toFloat() * cellSize + offset.x + (cellSize / 2), cell.y.toFloat() * cellSize + offset.y + (cellSize / 2))
        var leftLoc = Vector2f(left.x.toFloat() * cellSize + offset.x + (cellSize / 2), left.y.toFloat() * cellSize + offset.y + (cellSize / 2))

        var angle = Misc.getAngleInDegrees(currentLoc, leftLoc)
        var point = MathUtils.getPointOnCircumference(currentLoc, cellSize / 2, angle)

        bounds.add(point)
        //bounds.add(currentLoc)
    }

    //Gets all connected cells from other biomes
    fun getOtherBiomeCells(cell: BiomeGridCell) : List<BiomeGridCell> {
        var x = cell.x
        var y = cell.y

        var offsets = ArrayList<Pair<Int, Int>>()
        offsets.add(Pair(-1, 0))
        offsets.add(Pair(0, 1))
        offsets.add(Pair(1, 0))
        offsets.add(Pair(0, -1))

        var cells = ArrayList<BiomeGridCell>()
        for ((xp,yp) in offsets) {
            var other = biomeGrid.get(x + xp)?.get(y + yp)

            if (other == null) {
                other = BiomeGridCell(x + xp, y + yp, cell.size, "", Misc.getHighlightColor(), Vector2f())
            }

            if (other.biomeID == cell.biomeID) continue
            cells.add(other)
        }
        return cells
    }

    fun getSurroundingCells(cell: BiomeGridCell) : List<BiomeGridCell> {
        var x = cell.x
        var y = cell.y

        var offsets = ArrayList<Pair<Int, Int>>()
        offsets.add(Pair(-1, 0))
        offsets.add(Pair(0, 1))
        offsets.add(Pair(1, 0))
        offsets.add(Pair(0, -1))

        offsets.add(Pair(-1, 1))
        offsets.add(Pair(1, 1))
        offsets.add(Pair(1, -1))
        offsets.add(Pair(-1, -1))

        *//*var offsets = mapOf(
            -1 to 0, 0 to 1, 1 to 0, 0 to -1,
            -1 to 1, 1 to 1, 1 to -1, -1 to -1)*//*

        var cells = ArrayList<BiomeGridCell>()
        for ((xp,yp) in offsets) {
            var other = biomeGrid.get(x + xp)?.get(y + yp) ?: continue
            cells.add(other)
        }
        return cells
    }


    //Grid Generation Start
    fun createBlob() {
        var cellsSoFar = ArrayList<BiomeGridCell>()
        var color = Color.getHSBColor(Random().nextFloat(), MathUtils.getRandomNumberInRange(0.7f, 1f), MathUtils.getRandomNumberInRange(0.7f, 1f))
        var id = "Test_${Misc.genUID()}"
        //var max = MathUtils.getRandomNumberInRange(140, 150)
        var max = (maxCells * 0.8f / biomes).toInt()
        var center = biomeGridList.filter { it.biomeID == "" }.randomOrNull() ?: return

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

        *//*for (cell in cellsSoFar) {
            var adjacent = getAdjacentEmptyCell(cell) ?: continue
            adjacent.biomeID = cell.biomeID
            adjacent.color = cell.color
        }*//*
    }

    fun selectCellToContinueFrom(center: Vector2f, cellsSoFar: List<BiomeGridCell>) : BiomeGridCell? {
        *//*var farthest = cellsSoFar.map { Vector2f(it.x.toFloat(), it.y.toFloat()) }.sortedBy { MathUtils.getDistance(it, center) }.last()
        var maxDistance = MathUtils.getDistance(center, farthest) + 1*//*

        var cellsWithNeighbour = cellsSoFar.filter { hasAdjacentEmptyCell(it) } ?: return null
        var picker = WeightedRandomPicker<BiomeGridCell>()
        for (cell in cellsWithNeighbour) {
            *//*  var loc = Vector2f(cell.x.toFloat(), cell.y.toFloat())
              var distance = MathUtils.getDistance(loc, center)
              distance + 1
              var level = distance / maxDistance
              level = 1 - level + 0.2f*//*
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
    }*/

}