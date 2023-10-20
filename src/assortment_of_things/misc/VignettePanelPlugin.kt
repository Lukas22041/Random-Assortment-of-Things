package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class VignettePanelPlugin : CustomUIPanelPlugin {


    var alpha = 0f
    var maxAlpha = 0.6f
    var reverse = true
    var reachedMax = false
    var decreaseVignette = false

    var position: PositionAPI? = null
    var darken = Global.getSettings().getSprite("graphics/fx/rat_black.png")
    var vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette_reversed.png")

    override fun positionChanged(position: PositionAPI?) {
        this.position = position
    }


    override fun renderBelow(alphaMult: Float) {

    }


    override fun render(alphaMult: Float) {
        if (position == null) return

        darken.color = Color(0, 0, 0)
        darken.alphaMult = alphaMult * alpha * 0.5f
        darken.setSize(position!!.width, position!!.height)
        darken.render(position!!.x, position!!.y)

        vignette.color = Color(50, 0, 0)
        vignette.alphaMult = alphaMult * alpha


        var offset = 200
        vignette.setSize(position!!.width + offset, position!!.height + offset)
        vignette.render(position!!.x - (offset * 0.5f), position!!.y - (offset * 0.5f))

    }


    override fun advance(amount: Float) {

        if (alpha <= maxAlpha && !reachedMax) {
            alpha += 0.3f * amount

            if (alpha >= maxAlpha) {
                reachedMax = true
            }
        }
        else if (decreaseVignette) {
            alpha -= 0.1f * amount
        }
        else {
            if (reverse) {
                alpha -= 0.25f * amount

                if (alpha < maxAlpha * 0.5f) {
                    reverse = false
                }
            }
            else {
                alpha += 0.2f * amount

                if (alpha >= maxAlpha) {
                    reverse = true
                }
            }
        }


        alpha = MathUtils.clamp(alpha, 0f, maxAlpha)

    }

    override fun processInput(events: MutableList<InputEventAPI>?) {

    }

    override fun buttonPressed(buttonId: Any?) {

    }

}