package assortment_of_things.misc

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.ui.CustomPanelAPI

abstract class BaseCustomVisualDialogDelegateWithRefresh(var background: String? = null) : CustomVisualDialogDelegate {

    private lateinit var basePanel: CustomPanelAPI
    lateinit var panel: CustomPanelAPI
    var width = 0f
    var height = 0f



    lateinit var callbacks: CustomVisualDialogDelegate.DialogCallbacks

    override fun init(panel: CustomPanelAPI, callbacks: CustomVisualDialogDelegate.DialogCallbacks) {
        this.callbacks = callbacks

        basePanel = panel
        width = basePanel.position.width
        height = basePanel.position.height

        onInit(basePanel)

        this.panel = basePanel.createCustomPanel(width, height, null)
        basePanel.addComponent(this.panel)

        refresh()
    }

     override fun getCustomPanelPlugin(): CustomUIPanelPlugin? {
         var plugin = PanelWithCloseButtonAndBackground(background)
         plugin.onClosePress = {
             callbacks.dismissDialog()
         }
        return plugin
     }

     override fun getNoiseAlpha(): Float {
         return 0.8f
     }

     override fun advance(amount: Float) {

     }

     override fun reportDismissed(option: Int) {

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