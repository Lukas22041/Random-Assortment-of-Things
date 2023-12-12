package assortment_of_things.misc

import assortment_of_things.frontiers.ui.SiteDisplayElement
import assortment_of_things.misc.addWindow
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.ui.CustomPanelAPI
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin
import org.lwjgl.util.vector.Vector2f

abstract class BaseCustomVisualDialogDelegateWithRefresh : CustomVisualDialogDelegate {

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
         var plugin = PanelWithCloseButton()
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