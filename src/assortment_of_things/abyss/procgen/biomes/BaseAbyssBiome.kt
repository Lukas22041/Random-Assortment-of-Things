package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.terrain.TestAbyssTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

abstract class BaseAbyssBiome {

    var startingCell: BiomeCellData? = null
    var cells = ArrayList<BiomeCellData>()

    var borderCells = ArrayList<BiomeCellData>()
    var nearCells = ArrayList<BiomeCellData>()
    var deepCells = ArrayList<BiomeCellData>() //Also includes deepest cells

    var deepestCells = ArrayList<BiomeCellData>() //Should be worked with first as to be reserved for important things

    var gridAlphaMult = 1f

    //Generated before Init. Mostly required to create appropiate grid sizes for the nebula terrain.
    var leftMostCell: Int = 0
    var rightMostCell: Int = 0
    var topMostCell: Int = 0
    var bottomMostCell: Int = 0
    var cellsWidth: Int = 0
    var cellsHeight: Int = 0
    var biomeWorldCenter: Vector2f = Vector2f()

    abstract fun getBiomeID() : String

    abstract fun getBiomeColor() : Color
    abstract fun getDarkBiomeColor() : Color

    /** Called after all cells are generated */
    abstract fun init()

    fun generateFogTerrain(terrainId: String, tileTexCat: String, tileTexKey: String) {

        var data = AbyssUtils.getData()
        var system = data.system
        var manager = data.biomeManager
        var otherBiomes = manager.biomes.filter { it != this }

       /* val w = AbyssBiomeManager.width / 200
        val h = AbyssBiomeManager.height / 200*/

        //Try to match the nebula perfectly to the max corners of the biome, this is to reduce the total amount of tiles, since a full sized system would be a ton of tiles to update
        var loc = Vector2f()
        loc = biomeWorldCenter
        //Needs to be converted to the units used for the terrains tiles since their different
        var tileSize = OldHyperspaceTerrainPlugin.TILE_SIZE
        val w = (cellsWidth * AbyssBiomeManager.cellSize / tileSize).toInt()
        val h = (cellsHeight * AbyssBiomeManager.cellSize / tileSize).toInt()

        val string = StringBuilder()
        for (y in h - 1 downTo 0) {
            for (x in 0 until w) {
                string.append("x")
            }
        }

        val nebula = system?.addTerrain(terrainId, OldBaseTiledTerrain.TileParams(string.toString(),
            w, h,
            tileTexCat/*"rat_terrain"*/, tileTexKey/*"depths1"*/,
            4,4,
            null))

        nebula!!.id = "rat_depths_${Misc.genUID()}"
        nebula!!.location.set(loc)

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as TestAbyssTerrainPlugin
        nebulaPlugin.biome = this

        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(0.6f) //0.35
        editor.regenNoise()


        for (other in otherBiomes) {

            for (cell in other.cells) {
                //if (cell.depth != BiomeDepth.BORDER) {

                if (cell.getAdjacent().none { it.getBiome() == this }) {
                    editor.clearArc(cell.getWorldCenter().x, cell.getWorldCenter().y, 0f, AbyssBiomeManager.cellSize.toFloat() * 1.05f, 0f, 360f)
                }

                //}
            }
        }

    }

}