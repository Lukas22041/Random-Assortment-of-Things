package assortment_of_things.abyss.interactions.primordial

import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import java.awt.Color

class LongIconedButton(var sprite: SpriteAPI, var text: String, var color: Color, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    lateinit var para: LabelAPI

    var extraAlpha = 0f

    init {
        enableTransparency = true

        onHoverEnter {
            playScrollSound()
        }

        innerElement.setParaFont(Fonts.ORBITRON_12)
        //innerElement.setParaFontVictor14()
        para = innerElement.addPara(text, 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
        para.position.inTL(5+width/2 - para.computeTextWidth(para.text) / 2, height/2 - para.computeTextHeight(para.text) / 2)
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)


        sprite.setNormalBlend()
        sprite.alphaMult = alphaMult
        sprite.color = color
        sprite.setSize(24f, 24f)
        sprite.renderAtCenter(x+15f, y+height/2)


        sprite.setAdditiveBlend()
        sprite.alphaMult = extraAlpha * alphaMult
        sprite.renderAtCenter(x+15f, y+height/2)

    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)

    }

}