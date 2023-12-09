package assortment_of_things.misc

import assortment_of_things.frontiers.ui.SiteDisplayElement
import assortment_of_things.misc.addWindow
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.ui.CustomPanelAPI

abstract class BaseCustomDialogDelegateWithRefresh : BaseCustomDialogDelegate() {

    private lateinit var basePanel: CustomPanelAPI
    lateinit var panel: CustomPanelAPI
    var width = 0f
    var height = 0f

    override fun createCustomDialog(panel: CustomPanelAPI, callback: CustomDialogDelegate.CustomDialogCallback?) {
        basePanel = panel
        width = basePanel.position.width
        height = basePanel.position.height

        onInit(basePanel)

        this.panel = basePanel.createCustomPanel(width, height, null)
        basePanel.addComponent(this.panel)

        refresh()
    }

    fun refresh() {
        basePanel.removeComponent(panel)
        panel = basePanel.createCustomPanel(width, height, null)
        basePanel.addComponent(panel)

        onRefresh()
    }

    fun onInit(basePanel: CustomPanelAPI) {

    }

    abstract fun onRefresh()

}