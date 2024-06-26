package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.campaign.CoreUITabId
import org.lwjgl.input.Keyboard

class NPCExoshipInteraction : RATInteractionPlugin() {
    override fun init() {
        populateOptions()
    }

    fun populateOptions() {
        clearOptions()
        visualPanel.showImageVisual(interactionTarget.customInteractionDialogImageVisual)

        createOption("Talk to Amelie") {

        }

        createOption("Talk to Xander") {

        }

        createOption("Trade & Storage") {
            visualPanel.showCore(CoreUITabId.CARGO, interactionTarget) { }
        }
        optionPanel.setShortcut("Trade & Storage", Keyboard.KEY_I, false, false, false, false)

        createOption("Manage Fleet") {
            visualPanel.showCore(CoreUITabId.FLEET, interactionTarget) { }
        }
        optionPanel.setShortcut("Manage Fleet", Keyboard.KEY_F, false, false, false, false)

        createOption("Refit Ships") {
            visualPanel.showCore(CoreUITabId.REFIT, interactionTarget) { }
        }
        optionPanel.setShortcut("Refit Ships", Keyboard.KEY_R, false, false, false, false)

        addLeaveOption()
    }
}