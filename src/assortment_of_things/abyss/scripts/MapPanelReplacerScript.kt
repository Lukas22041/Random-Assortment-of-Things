package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.ui.AbyssalMapScreen
import assortment_of_things.misc.*
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.campaign.CampaignState
import com.fs.state.AppDriver

class MapPanelReplacerScript : EveryFrameScript {

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {

        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        var core: UIPanelAPI? = null

        var dialog = ReflectionUtils.invoke("getEncounterDialog", state)
        if (dialog != null)
        {
            core = ReflectionUtils.invoke("getCoreUI", dialog) as UIPanelAPI?
        }

        if (core == null) {
            core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
        }

        if (core == null) return

        var corePanels = core.getChildrenCopy().filter { it is UIPanelAPI } as List<UIPanelAPI>
        var innerPanels = corePanels.map { it.getChildrenCopy().find { children -> ReflectionUtils.hasMethodOfName("getMap", children) }}
        var panel = innerPanels.filterNotNull().firstOrNull() ?: return
        var parent = panel.getParent() ?: return

        parent.removeComponent(panel)

        var newPanel = Global.getSettings().createCustom(parent.getWidth(), parent.getHeight(), null)
        parent.addComponent(newPanel)
        var element = newPanel.createUIElement(parent.getWidth(), parent.getHeight(), false)
        newPanel.addUIElement(element)

        parent.position.inTL(50f, 50f)


        var screen = AbyssalMapScreen(element, parent.getWidth() - 100, 650f)


       /* var scData = SCUtils.getSCData()
        var skillPanel = SCSkillMenuPanel(parent, scData, docked)
        skillPanel.init()*/
    }

}