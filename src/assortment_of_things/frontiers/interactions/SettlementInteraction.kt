package assortment_of_things.frontiers.interactions

import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import org.lwjgl.input.Keyboard

class SettlementInteraction(var data: SettlementData) : RATInteractionPlugin() {

    lateinit var previousPlugin: InteractionDialogPlugin
    var dontReAddLargePlanet = false

    override fun init() {
        if (!dontReAddLargePlanet) {
            dialog.visualPanel.showLargePlanet(data.primaryPlanet)
        }
        textPanel.addPara("Test")

        createOption("Manage Settlement") {

        }

        createOption("Manage Storage") {
            visualPanel.showCore(CoreUITabId.CARGO, data.delegateEntity) {

            }
        }
        optionPanel.setShortcut("Manage Storage", Keyboard.KEY_I, false, false, false, false)
        optionPanel.setShortcut("Manage Storage", Keyboard.KEY_F, false, false, false, true)


        createOption("Back") {
            clearOptions()
            dialog.interactionTarget = data.primaryPlanet
            dialog.plugin = previousPlugin
            dialog.plugin.init(dialog)
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }

}