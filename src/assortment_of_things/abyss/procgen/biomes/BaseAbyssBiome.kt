package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import assortment_of_things.abyss.procgen.BiomeCellData
import assortment_of_things.abyss.procgen.BiomeParticleManager
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldBaseTiledTerrain
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldNebulaEditor
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

abstract class BaseAbyssBiome {

    var startingCell: BiomeCellData? = null
    var cells = ArrayList<BiomeCellData>()

    var borderCells = ArrayList<BiomeCellData>()
    var nearCells = ArrayList<BiomeCellData>()
    var deepCells = ArrayList<BiomeCellData>() //Also includes deepest cells

    var deepestCells = ArrayList<BiomeCellData>() //Should be worked with first as to be reserved for important things


    //Generated before Init. Mostly required to create appropiate grid sizes for the nebula terrain.
    var leftMostCell: Int = 0
    var rightMostCell: Int = 0
    var topMostCell: Int = 0
    var bottomMostCell: Int = 0
    var cellsWidth: Int = 0
    var cellsHeight: Int = 0
    var biomeWorldCenter: Vector2f = Vector2f()

    //Can be different types of terrain depending on the biome
    var terrain: BaseTerrain? = null


    abstract fun getBiomeID() : String
    abstract fun getDisplayName() : String

    abstract fun getBiomeColor() : Color
    abstract fun getDarkBiomeColor() : Color

    open fun shouldGenerateBiome() : Boolean = true

    open fun getShiftedColor() : Color = Color(255, 255, 255)
    open fun getSaturation() : Float = 1f
    open fun getBackgroundColor() = getDarkBiomeColor()
    open fun getParticleColor() = getBiomeColor()

    open fun getMaxDarknessMult() = 0.75f

    open fun getGridAlphaMult() = 1f

    //If null, will not change the music while within the biome and continue the prior ones.
    open fun getMusicKeyId() : String? = "rat_music_abyss_merged"

    /* Called before any cells have been generated, used mostly for minor biomes to take their cells */
    open fun preGenerate() { }

    /** Called after all cells are generated */
    abstract fun init()

    fun addBiomeTooltip(tooltip: TooltipMakerAPI) {

    }

    open fun spawnParticlesForCell(particleManager: BiomeParticleManager, cell: BiomeCellData) {

        var count = 3
        var fadeInOverwrite = false
        if (particleManager.particles.size <= 50) {
            count *= 4
            fadeInOverwrite = true
        }

        for (i in 0 until count) {
            var velocity = Vector2f(0f, 0f)
            velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(75f, 125f), MathUtils.getRandomNumberInRange(0f, 360f)))

            var color = getParticleColor()

            //var spawnLocation = Vector2f(Global.getSector().playerFleet.location)
            var spawnLocation = cell.getWorldCenter()

            var spread = AbyssBiomeManager.cellSize * 0.75f
            var randomX = MathUtils.getRandomNumberInRange(-spread, spread)
            var randomY = MathUtils.getRandomNumberInRange(-spread, spread)

            spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

            var fadeIn = MathUtils.getRandomNumberInRange(1f, 3f)
            if (fadeInOverwrite) fadeIn = 0.05f
            var duration = MathUtils.getRandomNumberInRange(2f, 5f)
            var fadeOut = MathUtils.getRandomNumberInRange(1f, 3f)

            var size = MathUtils.getRandomNumberInRange(25f, 50f)

            var alpha = MathUtils.getRandomNumberInRange(0.15f, 0.25f)

            particleManager.particles.add(BiomeParticleManager.AbyssalLightParticle(
                this,
                fadeIn,duration, fadeOut,
                color, alpha, size, spawnLocation, velocity,
                IntervalUtil(0.5f, 0.75f), MathUtils.getRandomNumberInRange(-5f, 5f), -20f, 20f))
        }
    }


    fun generateFogTerrain(terrainId: String, tileTexCat: String, tileTexKey: String, fogFraction: Float) {

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
        val w = (cellsWidth * AbyssBiomeManager.cellSize / tileSize).toInt() + 2 //slightly larger than supposed to, enables a bit of overlap
        val h = (cellsHeight * AbyssBiomeManager.cellSize / tileSize).toInt() + 2 //slightly larger than supposed to, enables a bit of overlap

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

        val nebulaPlugin = (nebula as CampaignTerrainAPI).plugin as BaseFogTerrain
        terrain = nebulaPlugin
        nebulaPlugin.biomePlugin = this

        val editor = OldNebulaEditor(nebulaPlugin)
        editor.regenNoise()
        editor.noisePrune(fogFraction) //0.35
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

        nebulaPlugin.save()

    }

}