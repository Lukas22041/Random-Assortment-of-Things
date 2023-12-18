package assortment_of_things.eastereggs.ut

import assortment_of_things.misc.BaseCustomVisualDialogDelegateWithRefresh
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import org.lwjgl.input.Keyboard

class UTPanelDelegate() : BaseCustomVisualDialogDelegateWithRefresh() {

    var utPanel = UTPanel()

    override fun onRefresh() {
        utPanel.init()
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin? {
        return object : CustomUIPanelPlugin {

            override fun positionChanged(position: PositionAPI) {
                utPanel.updatePanelLocation(position)
            }

            override fun renderBelow(alphaMult: Float) {

            }

            override fun render(alphaMult: Float) {
                utPanel.render(alphaMult)
            }

            override fun advance(amount: Float) {
                utPanel.advance(amount)
            }

            override fun processInput(events: MutableList<InputEventAPI>) {
                utPanel.input(events)

                for (event in events) {
                    if (event.isConsumed) continue
                    if (event.isKeyDownEvent && event.eventValue == Keyboard.KEY_ESCAPE) {
                        callbacks.dismissDialog()
                    }
                }
            }

            override fun buttonPressed(buttonId: Any?) {

            }
        }
    }
}