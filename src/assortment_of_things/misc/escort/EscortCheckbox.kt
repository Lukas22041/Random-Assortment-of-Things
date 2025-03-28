package assortment_of_things.misc.escort

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement

class EscortCheckbox(var value: Boolean, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var offSprite = Global.getSettings().getSprite("ui", "toggle20_off")
    var onSprite = Global.getSettings().getSprite("ui", "toggle20_on")
    var glowSprite = Global.getSettings().getSprite("ui", "toggle20_on2")


    init {
        renderBackground = false
        renderBorder = false

        onClick {
            playClickSound()
            value = !value
        }

        onHoverEnter {
            playScrollSound()
        }
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        var totalAlpha = 1f

        var alpha = 0f
        if (isHovering) alpha = 0.15f

        glowSprite.alphaMult = alpha * alphaMult * totalAlpha
        glowSprite.setSize(24f, 24f)
        glowSprite.render(x, y)

        if (value) {
            onSprite.alphaMult = alphaMult * totalAlpha
            onSprite.setSize(24f, 24f)
            onSprite.render(x, y)
        }

    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)

        offSprite.alphaMult = alphaMult
        offSprite.setSize(24f, 24f)
        offSprite.render(x, y)

    }

}