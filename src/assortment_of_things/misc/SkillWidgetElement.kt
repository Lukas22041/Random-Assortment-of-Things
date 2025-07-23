package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement
import org.lwjgl.opengl.GL11
import java.awt.Color

class SkillWidgetElement(var activated: Boolean, var canChangeState: Boolean, var iconPath: String, var color: Color, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var sprite = Global.getSettings().getSprite(iconPath)
    var inactiveBorder = Global.getSettings().getAndLoadSprite("graphics/icons/skills/rat_skillBorderInactive.png")
    var activeBorder = Global.getSettings().getAndLoadSprite("graphics/icons/skills/rat_skillBorderActive.png")

    var hoverFade = 0f
    var time = 0f

    init {
        enableTransparency = true
        backgroundAlpha = 0f
        borderAlpha = 0f

        onHoverEnter {
            playSound("ui_button_mouseover")
        }

    }

    override fun advance(amount: Float) {
        super.advance(amount)

        time += 1 * amount

        if (isHovering) {
            hoverFade += 10f * amount
        } else {
            hoverFade -= 3f * amount
        }
        hoverFade = hoverFade.coerceIn(0f, 1f)

    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

       /* var mult = 1f
        if (!activated) mult = 0.5f*/


        sprite.setNormalBlend()
        sprite.setSize(width-8, height-8)
        sprite.alphaMult = alphaMult

        //Contrast Mode

        if (!canChangeState && !activated) {
            sprite.color = Color(30, 32, 35)
        }
        else if (!activated) {
            sprite.color = Color(130, 135, 140)
        }
        else {
            sprite.color = Color(235, 245, 255)
        }

        sprite.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())

        if (activated) {

            activeBorder.setNormalBlend()
            activeBorder.color = color
            activeBorder.setSize(width, height)
            activeBorder.alphaMult = alphaMult
            activeBorder.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())


            activeBorder.setAdditiveBlend()
            activeBorder.color = color
            activeBorder.setSize(width, height)
            activeBorder.alphaMult = alphaMult * 0.2f
            activeBorder.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())

        }
        else {
            inactiveBorder.setNormalBlend()
            inactiveBorder.color = color
            inactiveBorder.setSize(width, height)
            inactiveBorder.alphaMult = alphaMult
            inactiveBorder.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())

            inactiveBorder.setAdditiveBlend()
            inactiveBorder.color = color
            inactiveBorder.setSize(width, height)
            inactiveBorder.alphaMult = alphaMult * 0.2f
            inactiveBorder.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())
        }


        sprite.setAdditiveBlend()
        sprite.setSize(width-8, height-8)
        sprite.alphaMult = alphaMult * 0.5f * hoverFade
        sprite.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())

    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)

        var backgroundColor = Color(0, 0, 0)

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)


        if (alphaMult <= 0.8f) {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        } else {
            GL11.glDisable(GL11.GL_BLEND)
        }

        GL11.glColor4f(backgroundColor.red / 255f,
            backgroundColor.green / 255f,
            backgroundColor.blue / 255f,
            backgroundColor.alpha / 255f * (alphaMult * backgroundAlpha))

        GL11.glRectf(x, y , x + width, y + height)

        GL11.glPopMatrix()
    }
}