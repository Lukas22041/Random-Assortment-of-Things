package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.floor

open class BaseFogTerrain : OldHyperspaceTerrainPlugin(), BiomeTerrain {

    var biomePlugin: BaseAbyssBiome? = null
    override fun getBiome(): BaseAbyssBiome? {
        return biomePlugin
    }

    override fun hasTooltip(): Boolean {
        return false
    }

    override fun getRenderColor(): Color {
        if (RATSettings.brighterAbyss!!) return getBiome()?.getDarkBiomeColor()?.brighter()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
        return getBiome()?.getDarkBiomeColor()?.setAlpha(225) ?: AbyssUtils.DARK_ABYSS_COLOR
    }

    //Try fixing the thing where stencil cuts break when things are rendered to far from outside of this.
   /* override fun renderOnRadar(radarCenter: Vector2f?, factor: Float, alphaMult: Float) {
        currLayer = null

        //if (true) return;
        val radius = Global.getSettings().getFloat("campaignRadarRadius") + 2000

        GL11.glPushMatrix()
        GL11.glTranslatef(-radarCenter!!.x * factor, -radarCenter!!.y * factor, 0f)


        //super.renderOnMap(factor, alphaMult);
        preMapRender(alphaMult)


        //GL11.glDisable(GL11.GL_TEXTURE_2D);
        val samples = 10

        var x = entity.location.x
        var y = entity.location.y
        val size = tileSize
        val renderSize = tileRenderSize

        val w = tiles.size * size
        val h = tiles[0].size * size
        x -= w / 2f
        y -= h / 2f
        val extra = (renderSize - size) / 2f + 100f

        val llx = radarCenter!!.x - radius
        val lly = radarCenter!!.y - radius
        val vw = radius * 2f
        val vh = radius * 2f

        if (llx > x + w + extra) {
            GL11.glPopMatrix()
            return
        }
        if (lly > y + h + extra) {
            GL11.glPopMatrix()
            return
        }
        if (llx + vw + extra < x) {
            GL11.glPopMatrix()
            return
        }
        if (lly + vh + extra < y) {
            GL11.glPopMatrix()
            return
        }

        var xStart = ((llx - x - extra) / size).toInt().toFloat()
        if (xStart < 0) xStart = 0f
        var yStart = ((lly - y - extra) / size).toInt().toFloat()
        if (yStart < 0) yStart = 0f

        var xEnd = (((llx + vw - x + extra) / size).toInt() + 1).toFloat()
        if (xEnd >= tiles.size) xEnd = (tiles.size - 1).toFloat()
        var yEnd = (((lly + vw - y + extra) / size).toInt() + 1).toFloat()
        if (yEnd >= tiles.size) yEnd = (tiles[0].size - 1).toFloat()

        xStart = (floor((xStart / samples).toDouble()).toInt() * samples).toFloat()
        xEnd = (floor((xEnd / samples).toDouble()).toInt() * samples).toFloat()
        yStart = (ceil((yStart / samples).toDouble()).toInt() * samples).toFloat()
        yEnd = (ceil((yEnd / samples).toDouble()).toInt() * samples).toFloat()

        mapTexture.bindTexture()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        renderSubArea(xStart, xEnd, yStart, yEnd, factor, samples, alphaMult)

        GL11.glPopMatrix()
    }*/

   /* override fun containsEntity(other: SectorEntityToken?): Boolean {
        return false
    }

    override fun containsPoint(test: Vector2f?, r: Float): Boolean {
        return false
    }*/

    //Name and Speed boost handled by central terrain
    override fun getTerrainName(): String? {
        /* val player = Global.getSector().playerFleet
         val inCloud = this.isInClouds(player)

         var name = biomePlugin?.getDisplayName() ?: ""
         if (isInStorm(player)) return "$name (Storm)"
         if (inCloud) return "$name (Fog)"
         return name*/

        return null
    }

    fun save()
    {
        params.tiles = null
        savedTiles = encodeTiles(tiles)

        savedActiveCells.clear()

        for (i in activeCells.indices) {
            for (j in activeCells[0].indices) {
                val curr = activeCells[i][j]
                if (curr != null && isTileVisible(i, j)) {
                    savedActiveCells.add(curr)
                }
            }
        }
    }

}