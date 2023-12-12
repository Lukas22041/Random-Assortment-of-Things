package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import lunalib.backend.scripts.CombatHandler
import lunalib.backend.ui.components.base.LunaUIBaseElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.util.vector.Vector2f

class PanelWithCloseButton : BaseCustomUIPanelPlugin() {

    private var closeSpriteOff = Global.getSettings().getSprite("ui", "tripad_power_button")
    private var closeSpriteOn = Global.getSettings().getSprite("ui", "tripad_power_button_glow")
    private var closeSpriteBackground = Global.getSettings().getSprite("ui", "tripad_power_button_slot")
    private var buttonAlpha = 0f

    var position: PositionAPI? = null
    private var buttonLocation = Vector2f(Global.getSettings().screenWidth - closeSpriteOff.width, Global.getSettings().screenHeight - closeSpriteOff.height)

    var onClosePress = {}

    override fun positionChanged(position: PositionAPI) {
        this.position = position
        buttonLocation =  Vector2f(position.x + position.width - closeSpriteOff.width, position.y + position.height - closeSpriteOff.height)
    }

    override fun processInput(events: MutableList<InputEventAPI>) {
        for (event in events) {
            if (event.isKeyDownEvent && event.eventValue == Keyboard.KEY_ESCAPE)
            {
                onClosePress()
                event.consume()
                break
            }
            if (event.isMouseEvent) {
                if (event.x.toFloat() in buttonLocation.x..(buttonLocation.x + closeSpriteOff.width) && event.y.toFloat() in buttonLocation.y..(buttonLocation.y + closeSpriteOff.height)) {
                    buttonAlpha = MathUtils.clamp(buttonAlpha + 0.5f, 0f, 1f)
                }
                else
                {
                    buttonAlpha = MathUtils.clamp(buttonAlpha - 0.5f, 0f, 1f)
                }
            }
            if (event.isMouseDownEvent)
            {
                if (event.x.toFloat() in buttonLocation.x..(buttonLocation.x + closeSpriteOff.width) && event.y.toFloat() in buttonLocation.y..(buttonLocation.y + closeSpriteOff.height))
                {
                    Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f)
                    onClosePress()
                    break
                }
            }
        }
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        closeSpriteBackground.render(buttonLocation.x - 32f, buttonLocation.y)
        closeSpriteBackground.alphaMult = alphaMult

        closeSpriteOff.render(buttonLocation.x, buttonLocation.y)
        closeSpriteOff.alphaMult = alphaMult

        closeSpriteOn.alphaMult = buttonAlpha * alphaMult
        closeSpriteOn.render(buttonLocation.x, buttonLocation.y)
    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)
    }



}