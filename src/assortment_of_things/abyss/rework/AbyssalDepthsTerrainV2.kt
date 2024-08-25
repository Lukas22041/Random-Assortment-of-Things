package assortment_of_things.abyss.rework

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class AbyssalDepthsTerrainV2 : BaseTerrain() {

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

    override fun getRenderRange(): Float {
        return 10000000f
    }


    override fun renderOnMap(factor: Float, alphaMult: Float) {
        super.renderOnMap(factor, alphaMult)

        renderBackground(alphaMult, factor)


       /* GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        var x = 0f * factor
        var y = 0f * factor

        var size = 5000f * factor



        GL11.glBegin(GL11.GL_POLYGON)

        //Bottom Left
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glVertex2f(x, y)

        //Bottom Mid
        GL11.glColor4f(1f, 0f, 0f, 1f)
        GL11.glVertex2f(x + size / 2, y)

        //Bottom Right
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glVertex2f(x + size, y)

        //Mid Right
        GL11.glColor4f(1f, 0f, 0f, 1f)
        GL11.glVertex2f(x + size, y + size / 2)

        //Top Right
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glVertex2f(x + size, y + size)

        //Top Mid
        GL11.glColor4f(1f, 0f, 0f, 1f)
        GL11.glVertex2f(x + size / 2, y + size)

        //Top Left
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glVertex2f(x, y + size)

        //Mid Left
        GL11.glColor4f(1f, 0f, 0f, 1f)
        GL11.glVertex2f(x, y + size / 2)

        GL11.glEnd()

        //GL11.glRectf(x, y, x+ size, y + size)

        GL11.glPopMatrix()*/

        for ((x, map) in AbyssBiomeGenerator.biomeGrid) {
            for ((y, cell) in map) {
                renderCells(cell, factor, alphaMult)
            }
        }
    }

    fun renderBackground(factor: Float, alphaMult: Float) {
        var color = Color(10, 0, 0, 255)

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)


        GL11.glDisable(GL11.GL_BLEND)


        /* GL11.glEnable(GL11.GL_CULL_FACE)
         GL11.glCullFace(GL11.GL_FRONT)
         GL11.glFrontFace(GL11.GL_CW)*/
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4f(color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f * alphaMult)

        var x = -50000f
        var y = -50000f

        var size = 100000f

        GL11.glRectf(x, y , x + size, y + size)

        GL11.glPopMatrix()
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)


        /*for (region in AbyssGeneratorV2.biomeRegions) {
            if (layer == CampaignEngineLayers.ABOVE) {
                renderBorder(region.polygon, region.color)
            }
        }*/

        /*for ((x, map) in AbyssGeneratorV2.biomeGrid) {
            for ((y, cell) in map) {
                if (layer == CampaignEngineLayers.ABOVE) {
                    renderCells(cell, 1f)
                }
            }
        }*/
    }

    override fun getEffectCategory(): String {
        return "rat_abyss_v2"
    }

    override fun getTerrainId(): String {
        return super.getTerrainId()
    }



    fun renderCells(cell: AbyssBiomeGenerator.BiomeGridCell, factor: Float, alphaMult: Float) {
        var color = cell.color
        //var alpha = 0.5f
        //return
        var alpha = 0.5f * alphaMult
        //if (cell.biomeID != "") alpha = 0.5f


        var x = (cell.locX) * factor
        var y = (cell.locY) * factor
        var size = (cell.size) * 1.05f * factor

        var playerfleetLocation = Global.getSector().playerFleet.location

        if (playerfleetLocation.x in x..(x + size)) {
            if (playerfleetLocation.y in y..(y + size)) {
                color = Misc.getPositiveHighlightColor()
                alpha = 1f
            }
        }

        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)



       /* GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)




        GL11.glVertex2f(x, y)
        GL11.glVertex2f(x, y + size)
        GL11.glVertex2f(x + size, y + size)
        GL11.glVertex2f(x + size, y)
        GL11.glVertex2f(x, y)

        GL11.glEnd()*/


        var topLeft = cell.colorTopLeft
        var topRight = cell.colorTopRight
        var bottomLeft = cell.colorBottomLeft
        var bottomRight = cell.colorBottomRight


        GL11.glBegin(GL11.GL_POLYGON)

        var alphaM = 0.15f * alpha


        //Bottom Left
        GL11.glColor4f(bottomLeft!!.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f,alphaM)
        GL11.glVertex2f(x, y)

        //Bottom Right
        GL11.glColor4f(bottomRight!!.red / 255f, bottomRight.green / 255f, bottomRight.blue / 255f,alphaM)
        GL11.glVertex2f(x + size, y)

        //Top Right
        GL11.glColor4f(topRight!!.red / 255f, topRight.green / 255f, topRight.blue / 255f,alphaM)
        GL11.glVertex2f(x + size, y + size)

        //Top Left
        GL11.glColor4f(topLeft!!.red / 255f, topLeft.green / 255f, topLeft.blue / 255f,alphaM)
        GL11.glVertex2f(x, y + size)

        GL11.glEnd()




       /* //Bottom Left
        GL11.glColor4f(bottomLeft!!.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f,
            color.alpha / 255f * alpha * 0.5f)

        GL11.glRectf(x, y, x+ size, y + size)


        // Bottom Right
        GL11.glColor4f(bottomRight!!.red / 255f, bottomRight.green / 255f, bottomRight.blue / 255f,
            color.alpha / 255f * alpha * 0.5f)

        GL11.glRectf(x + size, y, x+ size * 2, y + size)

        // Top Left
        GL11.glColor4f(topRight!!.red / 255f, topRight.green / 255f, topRight.blue / 255f,
            color.alpha / 255f * alpha * 0.5f)

        GL11.glRectf(x + size, y + size, x+ size * 2, y + size * 2)

        // Top Left
        GL11.glColor4f(topLeft!!.red / 255f, topLeft.green / 255f, topLeft.blue / 255f,
            color.alpha / 255f * alpha * 0.5f)

        GL11.glRectf(x , y + size, x+ size , y + size * 2)*/


       /* GL11.glColor4f(color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f * alpha * 0.5f)

        GL11.glRectf(x, y, x+ size, y + size)*/

        GL11.glPopMatrix()
    }

}