package assortment_of_things.exotech.interactions.exoship.ui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import java.awt.Color

class ExoResourceSlider(tooltip: TooltipMakerAPI, width: Float, height: Float, var value: Float, var minValue: Float, var maxValue: Float) : LunaElement(tooltip, width, height) {

    var sliderPosX = 0f
    var level = 0f
    var sliderWidth = 10f

    init {
        enableTransparency = true
        renderBackground = false
        renderBorder = false

        setSliderPositionByValue(value)

        onClick {event ->
            if (event.eventValue == 0 && event.x.toFloat() in (position!!.centerX - width)..(position!!.centerX + width) && event.y.toFloat() in (y)..(y + height ))
            {
                Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f)
            }
        }
        onHoverEnter {
            borderColor = Misc.getDarkPlayerColor().brighter().brighter()
            Global.getSoundPlayer().playUISound("ui_number_scrolling", 1f, 0.8f)
        }
        onHoverExit {
            borderColor = Misc.getDarkPlayerColor().brighter()
        }
        onHeld {event ->
            if (event.isMouseEvent && event.x.toFloat() in (position!!.centerX - width)..(position!!.centerX + width) && event.y.toFloat() in (y)..(y + height ))
            {
                sliderPosX = event.x.toFloat() - position!!.centerX
                event.consume()
            }
        }


    }

    fun setSliderPositionByValue(curValue: Float)
    {
        var min = position.centerX - width / 2 + sliderWidth
        var max = position.centerX + width / 2 - sliderWidth

        var level = (curValue - minValue) / (maxValue - minValue)
        level -= 0.5f
        var scale = max - min
        sliderPosX = ((scale  * level).toFloat())

    }

    override fun advance(amount: Float) {
        super.advance(amount)

    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)

        var color = backgroundColor

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glColor4f(color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f * (alphaMult * backgroundAlpha))

        GL11.glRectf(x, y + height * 0.33f , x + width, y + height * 0.66f)

        GL11.glPopMatrix()
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        var color = borderColor

        var sliderPos = position.centerX + sliderPosX
        var min = position.centerX - width / 2 + sliderWidth
        var max = position.centerX + width / 2 - sliderWidth
        sliderPos = MathUtils.clamp(sliderPos, min, max)
        level = (sliderPos - min) / (max - min)

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glColor4f(color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f * (alphaMult * backgroundAlpha))

        GL11.glRectf(sliderPos - sliderWidth, y , sliderPos + sliderWidth, y + height)

        GL11.glPopMatrix()
    }
}