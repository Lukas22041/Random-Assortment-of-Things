package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI

fun UIPanelAPI.getChildrenCopy() : List<UIComponentAPI> {
    return ReflectionUtils.invoke("getChildrenCopy", this) as List<UIComponentAPI>
}

fun UIPanelAPI.getChildrenNonCopy() : List<UIComponentAPI>  {
    return ReflectionUtils.invoke("getChildrenNonCopy", this) as List<UIComponentAPI>
}

fun UIComponentAPI.getParent() : UIPanelAPI?  {
    return ReflectionUtils.invoke("getParent", this) as UIPanelAPI
}

fun TooltipMakerAPI.getParentWidget() : UIComponentAPI? {
    return ReflectionUtils.invoke("getParentWidget", this) as UIPanelAPI
}

fun UIComponentAPI.setOpacity(alpha: Float)
{
    ReflectionUtils.invoke("setOpacity", this, alpha)
}

fun TooltipMakerAPI.addWindow(to: UIPanelAPI, width: Float, height: Float, lambda: (RATWindowPlugin) -> Unit) {
    var parentPanel = Global.getSettings().createCustom(width, height, null)
    this.addCustom(parentPanel, 0f)

    var plugin = RATWindowPlugin(parentPanel, this)
    var panel = parentPanel.createCustomPanel(width, height, plugin)
    plugin.position = panel.position
    plugin.panel = panel
    parentPanel.addComponent(panel)

    parentPanel.position.rightOfMid(to, 20f)

    lambda(plugin)
}
