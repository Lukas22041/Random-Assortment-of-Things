package assortment_of_things.frontiers.interactions

import assortment_of_things.frontiers.SettlementData
import assortment_of_things.frontiers.interactions.panels.SettlementManagementScreen
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

        //Update facilities
        data.mananger.update()

        textPanel.addPara(data.description)

        populateOptions()
    }

    fun populateOptions() {
        clearOptions()

        var dialogPlugin = this
        createOption("Manage Settlement") {
            var screen = SettlementManagementScreen(data, dialogPlugin)
            dialog.showCustomVisualDialog(600f, 400f, screen)
        }

        var slots = data.getFunctionalSlots().sortedBy { it.getPlugin()?.populateSettlementDialogOrder() }

        for (slot in slots) {
            slot.getPlugin()?.populateSettlementDialog(dialog, this)
        }

        var storageText = "Manage Storage"
        if (data.hasFacility("trade_post")) storageText = "Manage Storage & Trade"
        createOption(storageText) {
            visualPanel.showCore(CoreUITabId.CARGO, data.settlementEntity) { }
        }
        optionPanel.setShortcut(storageText, Keyboard.KEY_I, false, false, false, false)

        createOption("Manage Fleet") {
            visualPanel.showCore(CoreUITabId.FLEET, data.settlementEntity) { }
        }
        optionPanel.setShortcut("Manage Fleet", Keyboard.KEY_F, false, false, false, false)

        createOption("Refit Ships") {
            visualPanel.showCore(CoreUITabId.REFIT, data.settlementEntity) { }
        }
        optionPanel.setShortcut("Refit Ships", Keyboard.KEY_R, false, false, false, false)



        createOption("Back") {

            if (data.autoDescend && data.primaryPlanet.market?.isPlanetConditionMarketOnly == true) {
                dialog.dismiss()
                return@createOption
            }

            clearOptions()
            dialog.interactionTarget = data.primaryPlanet
            dialog.plugin = previousPlugin
            dialog.plugin.init(dialog)
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }


    override fun optionSelected(optionText: String?, optionData: Any?) {

        var slots = data.getFunctionalSlots().sortedBy { it.getPlugin()?.populateSettlementDialogOrder() }

        for (slot in slots) {
            var pressed = slot.getPlugin()!!.optionPressDetected(optionText!!, optionData)
            if (pressed) {
                return
            }
        }

        super.optionSelected(optionText, optionData)
    }
}