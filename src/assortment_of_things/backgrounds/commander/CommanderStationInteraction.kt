package assortment_of_things.backgrounds.commander

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.ui.CustomPanelAPI
import lunalib.lunaExtensions.addLunaTextfield
import org.lwjgl.input.Keyboard

class CommanderStationInteraction : RATInteractionPlugin() {

    var scrollerPosition = 0f
    lateinit var listener: CommanderStationListener

    override fun init() {

        textPanel.addPara("A small station in ${interactionTarget.starSystem.nameWithNoType} commanded by yourself. Traffic within and near the station is low but steady.")
        listener = interactionTarget.memoryWithoutUpdate.get("\$rat_commander_listener") as CommanderStationListener

        addMainOptions()
    }

    fun addMainOptions() {

        clearOptions()

        visualPanel.showImageVisual(interactionTarget.customInteractionDialogImageVisual)

        createOption("Rename Station") {
            dialog.showCustomDialog(300f, 30f, CommanderNameSelector(interactionTarget))
        }

        // optionPanel.addOption("Access Cargo Storage", "CARGO")

        //Production
        createOption("Order ship & weapon production") {
            dialog.showCustomProductionPicker(CommanderCustomProduction(interactionTarget.market, listener))
        }

        optionPanel.setTooltip("Order ship & weapon production", "Request custom production from the station. Orders will be delivered to this stations cargo at the end of the month. 50.000 credits are allocated each month, up to a maximum of 500.000")

        //Fleet & Storage
        createOption("Access Cargo Storage") {
            visualPanel.showCore(CoreUITabId.CARGO, interactionTarget) { }
        }
        optionPanel.setShortcut("Access Cargo Storage", Keyboard.KEY_I, false, false, false, false)

        createOption("Inspect your Fleet") {
            visualPanel.showCore(CoreUITabId.FLEET, interactionTarget) { }
        }
        optionPanel.setShortcut("Inspect your Fleet", Keyboard.KEY_F, false, false, false, false)

        createOption("Use the local facilities to refit your fleet") {
            visualPanel.showCore(CoreUITabId.REFIT, interactionTarget) { }
        }
        optionPanel.setShortcut("Use the local facilities to refit your fleet", Keyboard.KEY_R, false, false, false, false)


        addLeaveOption()
    }
}

class CommanderNameSelector(var station: SectorEntityToken) : BaseCustomDialogDelegate() {


    var text = station.name

    override fun createCustomDialog(panel: CustomPanelAPI, callback: CustomDialogDelegate.CustomDialogCallback?) {
        var element = panel.createUIElement(panel.position.width, panel.position.height, false)
        panel.addUIElement(element)

        element.addLunaTextfield(text, false, panel.position.width, 30f).apply {
            enableTransparency = true
            advance {
                text = this.getText()
            }
        }
    }

    override fun customDialogConfirm() {
        station.name = text
    }

}