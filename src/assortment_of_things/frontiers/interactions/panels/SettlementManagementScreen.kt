package assortment_of_things.frontiers.interactions.panels

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.data.SettlementFacilitySlot
import assortment_of_things.frontiers.ui.FacilityDisplayElement
import assortment_of_things.frontiers.ui.SiteDisplayElement
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class SettlementManagementScreen(var data: SettlementData) : BaseCustomVisualDialogDelegateWithRefresh("graphics/icons/frontiers/ManagementBackground.png") {

    var noFacilityIcon = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/facilities/empty_facility.png")

    override fun onRefresh() {

        var mainPanel = panel.createCustomPanel(width, height, null)
        panel.addComponent(mainPanel)

        var element = mainPanel.createUIElement(width, height, false)
        mainPanel.addUIElement(element)

        var centerSize = 96f + 32f

        displaySettlementIcon(element, centerSize)
        displayFacilityIcons(element, centerSize)

       /* var plugin = PanelWithCloseButton()
        plugin.onClosePress = {
            callbacks.dismissDialog()
        }
        var closeButtonPanel = mainPanel.createCustomPanel(width, height, plugin)
        mainPanel.addComponent(closeButtonPanel)
        closeButtonPanel.position.inTL(0f, 0f)*/
    }


    fun displaySettlementIcon(element: TooltipMakerAPI, centerSize: Float) {
        var centerX = width / 2 - centerSize / 2
        var centerY = (height) / 2 - centerSize / 2

        var siteIcon = SiteDisplayElement(data, Global.getSettings().getSprite(data.primaryPlanet.spec.texture), element, centerSize , centerSize)
        siteIcon.position.inTL(centerX, centerY)


        /*siteIcon.renderBelow {
            background.setSize(width, height)
            background.alphaMult = it
            background.renderAtCenter(siteIcon.position.centerX, siteIcon.position.centerY)
        }*/

        element.addTooltip(siteIcon.elementPanel, TooltipMakerAPI.TooltipLocation.RIGHT, 400f) { tooltip ->
            tooltip.addSectionHeading("Settlement", Alignment.MID, 0f)
            tooltip.addSpacer(10f)

            var resource = FrontiersUtils.getRessource(data)
            var baseIncome = Misc.getDGSCredits(5000f)

            tooltip.addPara("A small settlement established on ${data.primaryPlanet.name}.", 0f)
            tooltip.addSpacer(10f)
            tooltip.addPara("The settlement acts autonomously in most of its duties. " +
                    "It harvests minor sites of local resources and performs trades to make it from day to day, resuling in an average net income of $baseIncome credits.",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "$baseIncome")

            tooltip.addSpacer(10f)

            if (resource != null) {
                tooltip.addPara("The settlement harvests from a local hotspot of ${resource.getName()} for more substantual profits.",
                    0f, Misc.getTextColor(), Misc.getHighlightColor(), "${resource.getName()}")
            } else {
                tooltip.addPara("However, the settlement is not nearby any resource hotspot of significance",
                    0f, Misc.getTextColor(), Misc.getHighlightColor())
            }

            if (resource != null) {
                tooltip.addSpacer(10f)
                tooltip.addSectionHeading("Resource", Alignment.MID, 0f)
                tooltip.addSpacer(10f)

                var img = tooltip!!.beginImageWithText(resource.getIcon(), 48f)
                img.addPara("${resource.getName()}:\n${resource.getDescription()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${resource.getName()}")
                tooltip!!.addImageWithText(0f)

                if (resource.canBeRefined()) {
                    tooltip!!.addSpacer(10f)
                    var img = tooltip!!.beginImageWithText(resource.getRefinedIcon(), 48f)
                    img.addPara("Can be refined to increase the export value by 50%%. Requires the \"Refinery\" facility.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "50%", "Refinery")
                    tooltip!!.addImageWithText(0f)
                }

                tooltip!!.addSpacer(10f)
                var creditIcon = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/credit_icon.png")
                var img2 = tooltip!!.beginImageWithText("graphics/icons/frontiers/credit_icon.png", 48f)
                var income = resource.spec.conditions.get(data.primaryPlanet.market.conditions.find { resource.spec.conditions.contains(it.id) }!!.id)
                var incomeString = Misc.getDGSCredits(income!!.toFloat())
                img2.addPara("The richness of this sites resource hotspot promises an estimated income of ${incomeString} credits per month from exports (without refinement).", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${incomeString}")
                tooltip!!.addImageWithText(0f)
            }

            tooltip.addSpacer(10f)
            tooltip.addSectionHeading("Info", Alignment.MID, 0f)
            tooltip.addSpacer(10f)




        }
    }

    fun displayFacilityIcons(element: TooltipMakerAPI, centerSize: Float) {
        var radius = centerSize + 16f
        var angle = -60f
        var slots = data.facilities
        for (slot in slots) {

            var facilitySize = 96f

            var centerX = width / 2 - facilitySize / 2
            var centerY = (height) / 2 - facilitySize / 2


            var plugin = slot.getFacilityPlugin()


            var facilityIcon = FacilityDisplayElement(data, plugin, element, facilitySize, facilitySize)
            var loc = MathUtils.getPointOnCircumference(Vector2f(centerX, centerY), radius, angle)

            element.addTooltip(facilityIcon.elementPanel, TooltipMakerAPI.TooltipLocation.RIGHT, 400f) { tooltip ->
                tooltip.addSectionHeading("Plot", Alignment.MID, 0f)
                tooltip.addSpacer(10f)

                if (plugin == null) {
                    var img = tooltip.beginImageWithText("graphics/icons/frontiers/facilities/empty_facility.png", 48f)
                    img.addPara("An empty plot with no facility located within it. Left-Click to construct a facility in this plot.", 0f,
                        Misc.getTextColor(), Misc.getHighlightColor(), "Left-Click")
                    tooltip.addImageWithText(0f)
                }
                else if (plugin.getID() == "landing_pad") {
                    var img = tooltip.beginImageWithText(plugin.getIcon(), 48f)
                    var label = img.addPara("This plot of land is home to the ${plugin.getName()}. As it is vital to the settlements infrastructure, it can not be removed or replaced.", 0f)

                    label.setHighlightColors(Misc.getHighlightColor(), Misc.getNegativeHighlightColor())
                    label.setHighlight("${plugin.getName()}", "it can not be removed or replaced.")

                    tooltip.addImageWithText(0f)

                    tooltip.addSpacer(10f)
                    tooltip.addSectionHeading("Facility", Alignment.MID, 0f)
                    tooltip.addSpacer(10f)
                    plugin.addDescriptionToTooltip(tooltip, data)
                }
                else {
                    var img = tooltip.beginImageWithText(plugin.getIcon(), 48f)
                    img.addPara("This plot of land is home to a ${plugin.getName()}. Left-Click to remove or replace it with another facility.", 0f,
                            Misc.getTextColor(), Misc.getHighlightColor(), "${plugin.getName()}", "Left-Click")
                    tooltip.addImageWithText(0f)

                    tooltip.addSpacer(10f)
                    tooltip.addSectionHeading("Facility", Alignment.MID, 0f)
                    tooltip.addSpacer(10f)

                    plugin.addDescriptionToTooltip(tooltip, data)

                }
                tooltip.addSpacer(3f)
            }


            facilityIcon.position.inTL(loc.x, loc.y)
            facilityIcon.onClick {
                if (plugin?.getID() != "landing_pad") {
                    element.addWindow(facilityIcon.elementPanel, 300f, 300f) {
                        addFacilityPicker(it, slot)
                    }
                }
            }
            angle += 60
        }
    }

    fun addFacilityPicker(window: RATWindowPlugin, slot: SettlementFacilitySlot) {
        var panelPlugin = BorderedPanelPlugin()
        panelPlugin.renderBackground = true
        panelPlugin.backgroundColor = Color(10, 10, 10)
        panelPlugin.alpha = 0.8f

        var popupWidth = 350f
        var popupHeight = 300f

        var windowPanel = window.panel.createCustomPanel(popupWidth, popupHeight, panelPlugin)
        window.panel.addComponent(windowPanel)

        var windowHeaderEle = windowPanel.createUIElement(popupWidth, popupHeight, false)
        windowPanel.addUIElement(windowHeaderEle)
        var credits = Misc.getDGSCredits(Global.getSector().playerFleet.cargo.credits.get())

        windowHeaderEle.addSectionHeading("Select a Facility - $credits credits", Alignment.MID, 0f)

        windowHeaderEle.addSpacer(5f)

        var element = windowPanel.createUIElement(popupWidth, popupHeight - 20, true)

        var plugins = FrontiersUtils.getAllFacilityPlugins()
            .filter { plugin -> data.facilities.none { facilities -> facilities.facilityID == plugin.getID() } && plugin.getID() != "landing_pad" }

        plugins = plugins.sortedBy { it.canBeBuild(data) }

        element.addSpacer(5f)



        if (slot.facilityID != "") {
            var removalElement = element.addLunaElement(popupWidth - 10, 30f).apply {
                enableTransparency = true
                borderAlpha = 0.2f
                backgroundAlpha = 0.4f
                addText("Click to remove current facility", Misc.getBrightPlayerColor())
                centerText()

                onHoverEnter {
                    playScrollSound()
                    backgroundAlpha = 0.6f
                }
                onHoverExit {
                    backgroundAlpha = 0.4f
                }

                onClick {
                    playClickSound()
                    playSound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                    slot.removeCurrentFacility()
                    window.close()
                    refresh()
                }
            }
            element.addSpacer(10f)
        }



        //Picker List
        for (plugin in plugins) {

            var facilityElement = element.addLunaElement(popupWidth - 10, 60f).apply {
                enableTransparency = true
                backgroundAlpha = 0.3f
                borderAlpha = 0.1f

                var inner = innerElement

                var cost = Misc.getDGSCredits(plugin.spec.cost)

                inner.addSpacer(5f)
                var text = inner.beginImageWithText(plugin.getIcon(), 48f)
                text.addPara("${plugin.getName()} - $cost \n${plugin.getShortDesc()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${plugin.getName()}", "$cost")
                inner.addImageWithText(0f)

                onClick {
                    playClickSound()
                    playSound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                    slot.installNewFacility(plugin.getID())
                    window.close()
                    refresh()
                }

                onHoverEnter {
                    playScrollSound()
                    backgroundAlpha = 0.6f
                }
                onHoverExit {
                    backgroundAlpha = 0.3f
                }
            }
            element.addSpacer(10f)
        }




        windowPanel.addUIElement(element)
    }
}