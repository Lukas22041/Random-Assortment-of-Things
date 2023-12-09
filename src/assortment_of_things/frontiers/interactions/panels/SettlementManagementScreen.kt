package assortment_of_things.frontiers.interactions.panels

import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.ui.FacilityDisplayElement
import assortment_of_things.frontiers.ui.SiteDisplayElement
import assortment_of_things.misc.BaseCustomDialogDelegateWithRefresh
import assortment_of_things.misc.BorderedPanelPlugin
import assortment_of_things.misc.addWindow
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class SettlementManagementScreen(var data: SettlementData) : BaseCustomDialogDelegateWithRefresh() {

    override fun onRefresh() {


        var leftPanel = panel.createCustomPanel(width * 0.43f, height, null)
        panel.addComponent(leftPanel)
        addLeftPanel(leftPanel)

        var rightPanel = panel.createCustomPanel(width * 0.54f, height, null)
        panel.addComponent(rightPanel)
        rightPanel.position.rightOfTop(leftPanel, width * 0.02f)
        addRightPanel(rightPanel)


       /* SiteDisplayElement(data, Global.getSettings().getSprite(data.primaryPlanet.spec.texture), element, 96f, 96f).apply {

        }*/
    }

    fun addLeftPanel(leftPanel: CustomPanelAPI) {
        var element = leftPanel.createUIElement(leftPanel.position.width, leftPanel.position.height, false)
        leftPanel.addUIElement(element)
        /*element.addLunaElement(leftPanel.position.width, leftPanel.position.height).apply {
            enableTransparency = true
        }*/
        element.addSectionHeading("Settlement Info", Alignment.MID, 0f)

    }

    fun addRightPanel(rightPanel: CustomPanelAPI) {

        var rWidth = rightPanel.position.width
        var rHeight = rightPanel.position.height

        var iconSize = 96f + 32f

        var centerX = rWidth / 2 - iconSize / 2
        var centerY = (rHeight + 30) / 2 - iconSize / 2

        var element = rightPanel.createUIElement(rWidth, rHeight, false)
        rightPanel.addUIElement(element)
       /* element.addLunaElement(rightPanel.position.width, rightPanel.position.height).apply {
            enableTransparency = true
        }*/
        element.addSectionHeading("Facilities", Alignment.MID, 0f)


        var siteIcon = SiteDisplayElement(data, Global.getSettings().getSprite(data.primaryPlanet.spec.texture), element, iconSize , iconSize)
        siteIcon.position.inTL(centerX, centerY)

        var radius = iconSize + 16f
        var angle = 60f
        for (i in 0 until 6) {

            var iconSize = 96f

            var centerX = rWidth / 2 - iconSize / 2
            var centerY = (rHeight + 30) / 2 - iconSize / 2

            var sprite = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/facilities/landing_pad.png")

            var siteIcon = FacilityDisplayElement(data, sprite, element, iconSize, iconSize)
            var loc = MathUtils.getPointOnCircumference(Vector2f(centerX, centerY), radius, angle)
            siteIcon.position.inTL(loc.x, loc.y)
            siteIcon.onClick {
                element.addWindow(siteIcon.elementPanel, 300f, 300f) {

                    var plugin = BorderedPanelPlugin()
                    plugin.renderBackground = true
                    plugin.backgroundColor = Color(10, 10, 10)
                    plugin.alpha = 0.8f

                    var windowPanel = it.panel.createCustomPanel(300f, 300f, plugin)
                    it.panel.addComponent(windowPanel)

                    var windowHeaderEle = windowPanel.createUIElement(300f, 300f, false)
                    windowPanel.addUIElement(windowHeaderEle)
                    windowHeaderEle.addSectionHeading("Select a Facility", Alignment.MID, 0f)

                    var ele = windowPanel.createUIElement(300f, 280f, true)
                   // ele.addSectionHeading("Select a Facility", Alignment.MID, 0f)
                    for (i in 0 until 50) {
                        ele.addPara("Test$i", 0f)
                    }
                    windowPanel.addUIElement(ele)
                }
            }
            angle += 60
        }
    }



}