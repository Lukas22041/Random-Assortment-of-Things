package assortment_of_things.frontiers.interactions.panels

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.ui.FacilityDisplayElement
import assortment_of_things.frontiers.ui.SiteDisplayElement
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class SettlementManagementScreen(var data: SettlementData) : BaseCustomVisualDialogDelegateWithRefresh() {

    var background = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/ManagementBackground.png")

    override fun onRefresh() {

        var mainPanel = panel.createCustomPanel(width, height, null)
        panel.addComponent(mainPanel)
        addRightPanel(mainPanel)

        var iconSize = 96f + 32f

        var centerX = width / 2 - iconSize / 2
        var centerY = (height) / 2 - iconSize / 2

        var element = mainPanel.createUIElement(width, height, false)
        mainPanel.addUIElement(element)
       // element.addSectionHeading("Manage Settlement", Alignment.MID, 0f)

        var siteIcon = SiteDisplayElement(data, Global.getSettings().getSprite(data.primaryPlanet.spec.texture), element, iconSize , iconSize)
        siteIcon.position.inTL(centerX, centerY)


        siteIcon.renderBelow {
            background.setSize(width, height)
            background.alphaMult = it
            background.renderAtCenter(siteIcon.position.centerX, siteIcon.position.centerY)
        }

        element.addTooltip(siteIcon.elementPanel, TooltipMakerAPI.TooltipLocation.RIGHT, 300f) { tooltip ->
            tooltip.addSectionHeading("Settlement", Alignment.MID, 0f)
            tooltip.addSpacer(3f)

            tooltip.addPara("A small settlement established on ${data.primaryPlanet.name}.", 0f)
        }

        var radius = iconSize + 16f
        var angle = 60f
        for (i in 0 until 6) {

            var iconSize = 96f

            var centerX = width / 2 - iconSize / 2
            var centerY = (height) / 2 - iconSize / 2

            var sprite = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/facilities/landing_pad.png")

            var plugin = FrontiersUtils.facilitySpecs.map { FrontiersUtils.getFacilityPlugin(it) }.random()

            var facilityIcon = FacilityDisplayElement(data, plugin, element, iconSize, iconSize)
            var loc = MathUtils.getPointOnCircumference(Vector2f(centerX, centerY), radius, angle)

            element.addTooltip(facilityIcon.elementPanel, TooltipMakerAPI.TooltipLocation.RIGHT, 300f) { tooltip ->
                tooltip.addSectionHeading("Facility", Alignment.MID, 0f)
                tooltip.addSpacer(3f)
            }


            facilityIcon.position.inTL(loc.x, loc.y)
            facilityIcon.onClick {
                element.addWindow(facilityIcon.elementPanel, 300f, 300f) {

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


    fun addRightPanel(rightPanel: CustomPanelAPI) {


    }


}