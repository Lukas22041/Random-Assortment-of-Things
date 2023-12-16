package assortment_of_things.frontiers.interactions

import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.interactions.panels.SettlementManagementScreen
import assortment_of_things.frontiers.ui.SiteDisplayElement
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addWindow
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.ui.CustomPanelAPI
import lunalib.lunaExtensions.openLunaCustomPanel
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin
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

        createOption("Manage Settlement") {
            var screen = SettlementManagementScreen(data)
            dialog.showCustomVisualDialog(600f, 400f, screen)
        }

        createOption("Manage Storage") {
            visualPanel.showCore(CoreUITabId.CARGO, data.delegateEntity) { }
        }
        optionPanel.setShortcut("Manage Storage", Keyboard.KEY_I, false, false, false, false)

        createOption("Manage Fleet") {
            visualPanel.showCore(CoreUITabId.FLEET, data.delegateEntity) { }
        }
        optionPanel.setShortcut("Manage Fleet", Keyboard.KEY_F, false, false, false, false)

        createOption("Refit Ships") {
            visualPanel.showCore(CoreUITabId.REFIT, data.delegateEntity) { }
        }
        optionPanel.setShortcut("Refit Ships", Keyboard.KEY_R, false, false, false, false)

        var slots = data.getFunctionalSlots().sortedBy { it.getPlugin()?.populateSettlementDialogOrder() }

        for (slot in slots) {
            slot.getPlugin()?.populateSettlementDialog(dialog, this)
        }


        createOption("Back") {
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