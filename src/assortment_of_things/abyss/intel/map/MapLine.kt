package assortment_of_things.abyss.intel.map

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import lunalib.lunaUI.elements.LunaElement
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class MapLine(var panel: UIPanelAPI, var positions: Map<StarSystemAPI, Vector2f>, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {


    init {
        renderBorder = true
        renderBackground = false

    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        for (pos in positions)
        {
            var nb = AbyssUtils.getSystemData(pos.key).neighbours
            var neighbours = positions.filter { nb.contains(it.key) }
            for (neighbour in neighbours)
            {
                var systemData = AbyssUtils.getSystemData(pos.key)
                var c = systemData.getColor().setAlpha(75)

                renderLines(c)

                GL11.glVertex2f(panel!!.position.x  + pos.value.x + 10, panel!!.position.y + pos.value.y + panel!!.position.height + 15)
                GL11.glVertex2f(panel!!.position.x + neighbour.value.x + 10, panel!!.position.y + neighbour.value.y + panel!!.position.height + 15)

                GL11.glEnd()
                GL11.glPopMatrix()
            }
        }
    }

    fun renderLines(color: Color)
    {
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f * (1f * 0.3f))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

    }

}