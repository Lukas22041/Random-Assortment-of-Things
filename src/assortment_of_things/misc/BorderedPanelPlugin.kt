package assortment_of_things.misc

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11

class BorderedPanelPlugin : CustomUIPanelPlugin {

    var position: PositionAPI? = null

    var renderBackground = false
    var backgroundColor = Misc.getDarkPlayerColor()
    var alpha = 0.2f

    override fun positionChanged(position: PositionAPI?) {
        this.position = position
    }

    override fun renderBelow(alphaMult: Float) {
        if (renderBackground && position != null) {
            var color = backgroundColor

            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_CULL_FACE)


            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glColor4f(color.red / 255f,
                color.green / 255f,
                color.blue / 255f,
                color.alpha / 255f * (alphaMult * alpha))

            GL11.glRectf(position!!.x, position!!.y, position!!.x + position!!.width, position!!.y + position!!.height)

            GL11.glPopMatrix()
        }
    }

    override fun render(alphaMult: Float) {
       if (position != null)
       {
           var c = Misc.getDarkPlayerColor()
           GL11.glPushMatrix()

           GL11.glTranslatef(0f, 0f, 0f)
           GL11.glRotatef(0f, 0f, 0f, 1f)

           GL11.glDisable(GL11.GL_TEXTURE_2D)


           GL11.glEnable(GL11.GL_BLEND)
           GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


           GL11.glColor4f(c.red / 255f,
               c.green / 255f,
               c.blue / 255f,
               c.alpha / 255f * (alphaMult))

           GL11.glEnable(GL11.GL_LINE_SMOOTH)
           GL11.glBegin(GL11.GL_LINE_STRIP)

           GL11.glVertex2f(position!!.x, position!!.y)
           GL11.glVertex2f(position!!.x,  position!!.y +  position!!.height)
           GL11.glVertex2f(position!!.x +  position!!.width,  position!!.y +  position!!.height)
           GL11.glVertex2f(position!!.x +  position!!.width,  position!!.y)
           GL11.glVertex2f(position!!.x,  position!!.y)

           GL11.glEnd()
           GL11.glPopMatrix()
       }
    }

    override fun advance(amount: Float) {

    }

    override fun processInput(events: MutableList<InputEventAPI>?) {

    }

    override fun buttonPressed(buttonId: Any?) {

    }

}