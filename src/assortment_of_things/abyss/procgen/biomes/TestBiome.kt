package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.BiomeDepth
import assortment_of_things.abyss.terrain.TestAbyssTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class TestBiome(var id: String, var color: Color, var darkColor: Color, var generateTerrain: Boolean) : BaseAbyssBiome() {
    override fun getBiomeID(): String {
        return id
    }

    override fun getBiomeColor(): Color {
        return color
    }

    override fun getDarkBiomeColor(): Color {
        return darkColor
    }

    override fun generateTerrain() {



        if (generateTerrain) {
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

            val nebula = system?.addTerrain("rat_abyss_test", OldBaseTiledTerrain.TileParams(string.toString(),
                    w, h,
                    "rat_terrain", "depths1",
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
}