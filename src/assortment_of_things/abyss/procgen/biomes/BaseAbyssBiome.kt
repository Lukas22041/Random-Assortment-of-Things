package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.terrain.TestAbyssTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

abstract class BaseAbyssBiome {

    var startingCell: BiomeCellData? = null
    var cells = ArrayList<BiomeCellData>()

    var borderCells = ArrayList<BiomeCellData>()
    var nearCells = ArrayList<BiomeCellData>()
    var deepCells = ArrayList<BiomeCellData>() //Also includes deepest cells

    var deepestCells = ArrayList<BiomeCellData>() //Should be worked with first as to be reserved for important things


    var gridAlphaMult = 1f

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

        val w = AbyssBiomeManager.width / 200
        val h = AbyssBiomeManager.height / 200

        /* val w = 250
         val h = 250*/

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
        nebula!!.location[0f] = 0f

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as TestAbyssTerrainPlugin
        nebulaPlugin.biome = this

        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(0.60f) //0.35
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