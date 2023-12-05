package assortment_of_things.frontiers.interactions

import assortment_of_things.frontiers.ui.SiteSelectionPickerElement
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.ui.CustomPanelAPI

class CreateSettlementInteraction : RATInteractionPlugin() {

    lateinit var previousPlugin: InteractionDialogPlugin

    override fun init() {
        textPanel.addPara("Test")

        createOption("Select Site") {
            dialog.showCustomDialog(600f, 600f, object: BaseCustomDialogDelegate() {
                override fun createCustomDialog(panel: CustomPanelAPI?, callback: CustomDialogDelegate.CustomDialogCallback?) {
                    var element = panel!!.createUIElement(600f, 600f, false)
                    panel.addUIElement(element)
                    SiteSelectionPickerElement(element, 600f, 600f)
                }
            })
        }

        addLeaveOption()
    }
}