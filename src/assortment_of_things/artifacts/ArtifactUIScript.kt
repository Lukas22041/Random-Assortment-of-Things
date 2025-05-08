package assortment_of_things.artifacts

import assortment_of_things.artifacts.ui.ArtifactDisplayElement
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.getChildrenCopy
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.ui.UITable
import com.fs.state.AppDriver
import lunalib.lunaExtensions.addLunaElement
import second_in_command.misc.clearChildren
import second_in_command.misc.getWidth
import java.awt.Color

class ArtifactUIScript : EveryFrameScript {

    @Transient
    var panel: CustomPanelAPI? = null

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {

        if (!Global.getSector().isPaused) return
        if (Global.getSector().campaignUI.currentCoreTab != CoreUITabId.FLEET) return

        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        var core: UIPanelAPI? = null

        var docked = false

        var dialog = ReflectionUtils.invoke("getEncounterDialog", state)
        if (dialog != null)
        {
            docked = true
            core = ReflectionUtils.invoke("getCoreUI", dialog) as UIPanelAPI?
        }

        if (core == null) {
            core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
        }

        if (core == null) return

        var fleetPanel = ReflectionUtils.invoke("getCurrentTab", core) as UIPanelAPI? ?: return
        var leftPanel = fleetPanel.getChildrenCopy().find { if (it is UIPanelAPI && it.getChildrenCopy().any { it is LabelAPI }) true else false } as UIPanelAPI ?: return

        var children = leftPanel.getChildrenCopy()
        if (panel != null && children.contains(panel!!)) {
            return
        }

        //fleetPanel.position.inTL(50f, 50f)
        //var last = corePanels.lastOrNull() ?: return
        //last.position.inTL(50f, 50f)



        //panel!!.position.inTL(-250f, 50f)


        var table = children.find { it is UITable } as UIComponentAPI

        var w = table.getWidth()
        var h = 60f

        table.position.setYAlignOffset(-h-20f)

        panel = Global.getSettings().createCustom(300f, h, null)
        leftPanel.addComponent(panel)
        panel!!.position.aboveLeft(table, 10f)



        //element.addPara("Test")
        recreate(panel!!, w, h)


        var last = children.filter { it is LabelAPI }.lastOrNull()
        if (last is UIComponentAPI) {
            last.position.setYAlignOffset(-10f)
        }


        //panel!!.position.rightOfMid(table, 0f)


       // element.addPara("Test")


       /* var innerPanels = corePanels.map { it.getChildrenCopy().find { children -> ReflectionUtils.hasMethodOfName("canReassign", children) }}
        var panel = innerPanels.filterNotNull().firstOrNull() as UIPanelAPI? ?: return
        var parent = panel.getParent() ?: return

        parent.removeComponent(panel)

        *//* var panelChildren = panel.getChildrenCopy()
         var seedTextElement = panelChildren.find { ReflectionUtils.hasMethodOfName("createStoryPointsLabel", it) }
         var seedElement = panelChildren.find { ReflectionUtils.hasMethodOfName("getTextLabel", it) }
         var copyButton = panelChildren.find { it is ButtonAPI && it.text == "copy" }*//*

        var scData = SCUtils.getPlayerData()
        var skillPanel = SCSkillMenuPanel(parent, scData, false,*//* seedTextElement as LabelAPI, seedElement as UIComponentAPI, copyButton as UIComponentAPI*//*)
        skillPanel.init()*/
    }

    fun recreate(panel: CustomPanelAPI, w: Float, h: Float) {
        panel.clearChildren()

        var element = panel!!.createUIElement(300f, h, false)
        panel!!.addUIElement(element)

        var container = element.addLunaElement(w, h).apply {
            enableTransparency = true
            borderAlpha = 0.7f
            backgroundAlpha = 0.6f
            backgroundColor = Color(0, 0, 0)

            onHoverEnter {
                playSound("ui_button_mouseover", 1f, 1f)
                borderAlpha = 1f
                backgroundColor = Color(10, 10, 20)
                //backgroundAlpha = 0.7f
            }
            onHoverExit {
                borderAlpha = 0.7f
                backgroundColor = Color(0, 0, 0)
               // backgroundAlpha = 0.5f
            }

        }
        container.elementPanel.position.setXAlignOffset(0f)

        var inner = container.innerElement



        var artifact = ArtifactUtils.getActiveArtifact()

        if (artifact == null) {
            var inactivePara = inner.addPara("No Artifact Active", 0f, Misc.getGrayColor(), Misc.getTextColor())
            inactivePara.position.inTL(w/2-inactivePara.computeTextWidth(inactivePara.text)/2, h/2-inactivePara.computeTextHeight(inactivePara.text)/2)
        } else {
            var plugin = ArtifactUtils.getPlugin(artifact!!)
            var display = ArtifactDisplayElement(artifact, inner, 40f, 40f)

            display.position.inTL(10f, h/2 - display.height/2)

            var designType = artifact.designType
            var designColor = Misc.getDesignTypeColor(designType)

            var title =  inner.addTitle("Artifact", Misc.getBasePlayerColor())
            var para = inner.addTitle("${artifact.name}", designColor)
            //para.position.inTL(w/2-para.computeTextWidth(para.text)/2+display.width/2, h/2-para.computeTextHeight(para.text)/2)

            title.position.inTL(display.width +20f, h/2-title.computeTextHeight(title.text)/2-para.computeTextHeight(para.text)/2)
            para.position.belowLeft(title as UIComponentAPI, 0f)
        }



    }

}