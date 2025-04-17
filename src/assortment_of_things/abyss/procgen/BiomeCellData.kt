package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.procgen.AbyssBiomeManager.Companion.cellSize
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class BiomeCellData(var manager: AbyssBiomeManager, var gridX: Int, var gridY: Int, var worldX: Float, var worldY: Float) {

    private var worldCenter = Vector2f(worldX + cellSize / 2f, worldY + cellSize / 2f)
    private var biomePlugin: BaseAbyssBiome? = null
    var isFake = false
    //var color = Color(30, 30, 30)
    var tags = ArrayList<String>()
    var isUsed = false //Dont spawn anything if already used by something large
    var isStartingPoint = false
    var renderAngle = MathUtils.getRandomNumberInRange(0f, 360f)

    var isDiscovered = false
    var isPartialyDiscovered = false


    //Depth Indicates how towards the center the biome cell is
    //0 means its directly neighbouring another biome, 1 means its one step away from another biome, and so forth
    var depth: BiomeDepth = BiomeDepth.NONE
    var intDepth = 0

    fun isDeepest() = biomePlugin?.deepestCells?.contains(this) == true

    fun getWorldCenter() : Vector2f {
        return worldCenter
    }

    fun getBiome() = biomePlugin
    fun setBiome(plugin: BaseAbyssBiome?) {

        getBiome()?.cells?.remove(this) //Remove from prior biome if in it

        biomePlugin = plugin
        biomePlugin?.cells?.add(this)
    }

    fun getLeft() = manager.getCell(gridX-1, gridY)
    fun getRight() = manager.getCell(gridX+1, gridY)
    fun getBottom() = manager.getCell(gridX, gridY-1)
    fun getTop() = manager.getCell(gridX, gridY+1)

    fun getBottomLeft() = manager.getCell(gridX-1, gridY-1)
    fun getBottomRight() = manager.getCell(gridX+1, gridY-1)
    fun getTopLeft() = manager.getCell(gridX-1, gridY+1)
    fun getTopRight() = manager.getCell(gridX+1, gridY+1)


    /** Left, Top, Right, Bottom */
    fun getAdjacent() : List<BiomeCellData> {
        var list = ArrayList<BiomeCellData>()

        list.add(getLeft())
        list.add(getRight())

        list.add(getBottom())
        list.add(getTop())

        return list
    }

    /** All 8 surrounding tiles */
    fun getSurrounding() : List<BiomeCellData> {
        var list = ArrayList<BiomeCellData>()

        list.addAll(getAdjacent())

        list.add(getBottomLeft())
        list.add(getBottomRight())

        list.add(getTopLeft())
        list.add(getTopRight())

        return list
    }

    fun getAround(radius: Int, processed: MutableList<BiomeCellData> = mutableListOf(), toProcess: MutableList<BiomeCellData> = mutableListOf(this)) : List<BiomeCellData> {
        //var list = ArrayList<BiomeCellData>()
        if (radius == 0) {
            return processed
        }

        var prior = ArrayList(toProcess)
        toProcess.clear()

        for (cell in prior) {
            if (processed.contains(cell)) continue
            //list.add(cell)
            processed.add(cell)
            toProcess.addAll(cell.getAdjacent())
        }

        //list.addAll(getAround(radius-1, processed, toProcess))
        getAround(radius-1, processed, toProcess)

        var result = processed.distinct()

        return result
    }

    /*//Surrounding tiles based on range entered, includes itself, includes fake cells
    //TODO improve the performance a lot
    fun getAround(radius: Int, parent: BiomeCellData=this) : List<BiomeCellData> {
        var list = ArrayList<BiomeCellData>()
        if (radius == 0) {
            return list
        }

        for (cell in parent.getAdjacent()) {
            list.add(cell)
            list.addAll(getAround(radius-1, cell))
        }

       var result = list.distinct()

        return result
    }*/

    fun getEmptyAdjacent() = getAdjacent().filter { it.getBiome() == null && !it.isFake }
    fun getEmptySurrounding() = getSurrounding().filter { it.getBiome() == null && !it.isFake }
    fun getEmptyAround(radius: Int) = getAround(0).filter { it.getBiome() == null && !it.isFake }
}