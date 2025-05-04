package assortment_of_things.abyss.interactions.primordial

import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement
import java.awt.Color

class CheckmarkButton(var sprite: SpriteAPI, var color: Color, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var extraAlpha = 0f

    init {
        enableTransparency = true

        onHoverEnter {
            playScrollSound()
        }
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        sprite.setNormalBlend()
        sprite.alphaMult = alphaMult
        sprite.color = color
        sprite.setSize(width, height)
        sprite.renderAtCenter(x+width/2, y+height/2)

        sprite.setAdditiveBlend()
        sprite.alphaMult = extraAlpha * alphaMult
        sprite.renderAtCenter(x+width/2, y+height/2)
    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)

    }
}