package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.procgen.biomes.*
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class AbyssBiomeManager {

    companion object {
        var width = 52000 * 2 //*3
        var height = 52000 * 1 //*2
        var cellSize = 2000 //Might want to try 4000 instead later, or 3000 with a different width/height. Would help in places where a cell is only 1 tile big between other biomes

        var rows = width / cellSize
        var columns = height / cellSize

        var xOffset = width / 2
        var yOffset = height / 2
    }

    var biomes = ArrayList<BaseAbyssBiome>()
    fun getBiome(biomeId: String) = biomes.find { it.getBiomeID() == biomeId }
    fun getBiomeOfClass(biome: Class<*>) = biomes.find { it.javaClass.name == biome.name }

    //Get the lower left coordinate of a cell in World coordinates
    fun toWorldX(gridX: Int) = (gridX * cellSize - xOffset).toFloat()
    fun toWorldY(gridY: Int) = (gridY * cellSize - yOffset).toFloat()
    fun toWorldX(x: Float) = x * cellSize - xOffset
    fun toWorldY(y: Float) = y * cellSize - yOffset
    fun toWorld(gridX: Int, gridY: Int) = Vector2f(toWorldX(gridX), toWorldY(gridY))

    //Convert the location of something in-world to the index of the cell their in.
    fun toGridX(worldX: Float) = ((worldX + xOffset) / cellSize).toInt()
    fun toGridY(worldY: Float) = ((worldY + yOffset) / cellSize).toInt()

    private val cellArray = Array(rows) { row ->
        Array<BiomeCellData>(columns) {
            column -> BiomeCellData(this, row, column, toWorldX(row), toWorldY(column))
        }
    }
    private var cellList = cellArray.flatten()

    //var cells = ArrayList<ArrayList>

    fun getDominantBiome() : BaseAbyssBiome {
        return getBiomeLevels().maxBy { it.value }.key
    }

    fun getBiomeLevels() : HashMap<BaseAbyssBiome, Float> {
        var levels = HashMap<BaseAbyssBiome, Float>()

        var playerLoc = Global.getSector().playerFleet.location
        var playerCell = getPlayerCell()
        //var around = playerCell.getAround(3)//.filter { it.depth == BiomeDepth.BORDER }
        //var surrounding = playerCell.getSurrounding()
        var surrounding = playerCell.getSurroundingWithCenter()

        var maxLevels = HashMap<BaseAbyssBiome, Float>()

        for (cell in surrounding) {
            var biome = cell.getBiome() ?: continue //Continue if no biome

            var distance = MathUtils.getDistance(cell.getWorldCenter(), playerLoc)

            var level = distance.levelBetween(cellSize.toFloat()/**1.5f*/, 0f)

            if (level >= (maxLevels.get(biome) ?: 0f)) {
                maxLevels.put(biome, level)
            }

        }

        var totalDistance = maxLevels.values.sum()
        for (biome in biomes) {
            var value = maxLevels.get(biome)
            if (value == null) {
                levels.put(biome, 0f)
                continue
            }
            var level = (value / totalDistance)
            levels.put(biome, level)
        }

        if (maxLevels.size == 1) {
            var first = maxLevels.keys.first()
            levels.set(first, 1f)
        }

        return levels
   }

  

    fun init() {

        //TODO Remove
        biomes.clear()
        for (cell in cellList) { cell.setBiome(null) }

        biomes.add(SeaOfTranquility())
        biomes.add(SeaOfHarmony())
        biomes.add(SeaOfSolitude())
        biomes.add(SeaOfSerenity())
        biomes.add(AbyssalWastes())

        //var cells = cellArray.sumOf { it.size }


        //cellList.forEach { it.color = AbyssUtils.ABYSS_COLOR.darker().darker() }



        var starts = findStartingPoints()

        //Slight blobs to guarantee biomes having atleast some deep parts
        for (start in starts) {
            for (cell in start.getEmptyAround(3)) { //Change to 4 if generation is to off
                cell.setBiome(start.getBiome()!!)
            }
        }

        generateBiomes(/*starts*/)

        determineBordersAndDepth()

        //generateTerrain()

        findCorners()

        initBiomes()
    }

    fun initBiomes() {
        for (biome in biomes) {
            biome.init()
        }
    }

    //Searches for the cells per biome that are left, right, top or bottom
    fun findCorners() {
        for (biome in biomes) {
            biome.leftMostCell = biome.cells.minOf { it.gridX }
            biome.rightMostCell = biome.cells.maxOf { it.gridX }

            biome.bottomMostCell = biome.cells.minOf { it.gridY }
            biome.topMostCell = biome.cells.maxOf { it.gridY }

            biome.cellsWidth = biome.rightMostCell - biome.leftMostCell
            biome.cellsHeight = biome.topMostCell - biome.bottomMostCell

            biome.biomeWorldCenter = Vector2f(toWorldX(biome.leftMostCell.toFloat() + (biome.cellsWidth).toFloat()/2f) + cellSize/2, toWorldY(biome.bottomMostCell.toFloat() + (biome.cellsHeight).toFloat()/2)+ cellSize/2)
        }
    }

    /*fun generateTerrain() {

        for (biome in biomes) {
            biome.generateFogTerrain()
        }
    }*/

    fun determineBordersAndDepth() {
        for (biome in biomes) {
            for (cell in biome.cells) {
                if (cell.getSurrounding().any { it.getBiome() != cell.getBiome() }) {
                    cell.depth = BiomeDepth.BORDER
                    biome.borderCells.add(cell)
                }
            }
        }

        for (biome in biomes) {
            for (cell in biome.cells.filter { it.depth != BiomeDepth.BORDER }) {
                if (cell.getAdjacent().any { it.depth == BiomeDepth.BORDER }) {
                    cell.depth = BiomeDepth.NEAR_BORDER
                    cell.intDepth = 1
                    biome.nearCells.add(cell)
                }
            }
        }


        //Recursively go through the rest of the layers to determine the depth of all the other places.
        getDepthRecursive(1)

        //Put the 2 deepest deths in to their own list
        for (biome in biomes) {
            var deepest = biome.cells.maxOf { it.intDepth }
            var deepestCells = biome.cells.filter { it.intDepth == deepest || it.intDepth == deepest - 1 }
            biome.deepestCells.addAll(deepestCells)
        }

    }

    fun getDepthRecursive(currDepth: Int) {

        if (cellList.none { it.depth == BiomeDepth.NONE }) return

        for (biome in biomes) {
            for (cell in biome.cells.filter { it.depth == BiomeDepth.NONE }) {
                if (cell.getAdjacent().any { it.intDepth == currDepth }) {
                    cell.depth = BiomeDepth.DEEP
                    cell.intDepth = currDepth + 1
                    biome.borderCells
                }
            }
        }

        getDepthRecursive(currDepth+1)

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

            //Make far away cells from core less likely to be picked
           /* var weighted = WeightedRandomPicker<BiomeCellData>()
            for (cell in available) {
                var distance = MathUtils.getDistance(Vector2f(cell.worldX, cell.worldY), Vector2f(biome.startingCell!!.worldX, biome.startingCell!!.worldY))
                var weight = height.toFloat() * 2 - distance
                weight = MathUtils.clamp(weight, 0.1f, height.toFloat() * 2)
                weighted.add(cell, weight)
            }
            var pick = weighted.pick() ?: continue //Stop if no cells are available*/

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
            biome.startingCell = cell
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
        var biome = getBiome("abyssal_wastes")
      /*  if (biome != null) {
            cell.setBiome(biome)
        }*/
        //cell.color = Color.white
        return cell
    }
}