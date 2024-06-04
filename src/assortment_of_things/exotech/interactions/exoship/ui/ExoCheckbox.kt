package assortment_of_things.exotech.interactions.exoship.ui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exerelin.campaign.ui.NexLunaElement

class ExoCheckbox(var value: Boolean, tooltip: TooltipMakerAPI, width: Float, height: Float) : NexLunaElement(tooltip, width, height) {

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

        var alpha = 0f
        if (isHovering) alpha = 0.15f

        glowSprite.alphaMult = alpha * alphaMult
        glowSprite.setSize(24f, 24f)
        glowSprite.render(x, y)

        if (value) {
            onSprite.alphaMult = alphaMult
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