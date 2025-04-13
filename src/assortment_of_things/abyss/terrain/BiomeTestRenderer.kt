package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssBiomeManager
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.util.*

class BiomeTestRenderer : BaseTerrain() {


    /*override fun renderOnRadar(radarCenter: Vector2f?, factor: Float, alphaMult: Float) {
        //super.renderOnRadar(radarCenter, factor, alphaMult)
    }*/

    override fun renderOnMap(factor: Float, alphaMult: Float) {
        super.renderOnMapAbove(factor, alphaMult)

        var data = AbyssUtils.getData()
        var manager = data.biomeManager
        var cells = manager.getCells()

        var playerCell = manager.getPlayerCell()
        var surrounding = playerCell.getAround(3)

        var alpha = 0.5f

        for (cell in cells) {
            var color = cell.color
            if (surrounding.contains(cell)) color = Misc.getHighlightColor()
            if (cell == playerCell) color = Misc.getBasePlayerColor()

            var x = cell.worldX * factor
            var y = cell.worldY * factor
            var size = AbyssBiomeManager.cellSize * factor

            var renderOutlines = true
            if (renderOutlines) {
                var c = color
                GL11.glPushMatrix()

                GL11.glTranslatef(0f, 0f, 0f)
                GL11.glRotatef(0f, 0f, 0f, 1f)

                GL11.glDisable(GL11.GL_TEXTURE_2D)

                //GL11.glDisable(GL11.GL_BLEND)

                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


                GL11.glColor4f(c.red / 255f,
                    c.green / 255f,
                    c.blue / 255f,
                    c.alpha / 255f * (alphaMult * alpha))



                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glBegin(GL11.GL_LINE_STRIP)

                GL11.glVertex2f(x, y)
                GL11.glVertex2f(x, y + size)
                GL11.glVertex2f(x + size, y + size)
                GL11.glVertex2f(x + size, y)
                GL11.glVertex2f(x, y)

                GL11.glEnd()
                GL11.glPopMatrix()
            }

            var renderBackgrounds = true
            if (renderBackgrounds) {

                GL11.glPushMatrix()
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_CULL_FACE)


                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

                /* GL11.glEnable(GL11.GL_CULL_FACE)
                 GL11.glCullFace(GL11.GL_FRONT)
                 GL11.glFrontFace(GL11.GL_CW)*/
                //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glColor4f(color.red / 255f,
                    color.green / 255f,
                    color.blue / 255f,
                    color.alpha / 255f * (alphaMult * alpha * 0.5f))

                GL11.glRectf(x, y , x + size, y + size)

                GL11.glPopMatrix()
            }


        }

    }


    override fun getEffectCategory(): String {
        return "rat_map_renderer"
    }

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        Global.getSettings().getSpec(StarGenDataSpec::class.java, "", true)

        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

    override fun getRenderRange(): Float {
        return 10000000f
    }

}