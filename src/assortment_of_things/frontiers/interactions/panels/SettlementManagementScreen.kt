package assortment_of_things.frontiers.interactions.panels

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.frontiers.SettlementManager
import assortment_of_things.frontiers.data.SettlementFacilitySlot
import assortment_of_things.frontiers.interactions.SettlementInteraction
import assortment_of_things.frontiers.ui.FacilityDisplayElement
import assortment_of_things.frontiers.ui.SiteDisplayElement
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.*
import lunalib.lunaUI.elements.LunaSpriteElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.fadeAndExpire
import java.awt.Color

class SettlementManagementScreen(var data: SettlementData, var dialogPlugin: SettlementInteraction) : BaseCustomVisualDialogDelegateWithRefresh("graphics/icons/frontiers/ManagementBackground.png") {

    var noFacilityIcon = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/facilities/empty_facility.png")

    override fun onRefresh() {
        var mainPanel = panel.createCustomPanel(width, height, null)
        panel.addComponent(mainPanel)

        var element = mainPanel.createUIElement(width, height, false)
        mainPanel.addUIElement(element)

        var centerSize = 96f + 32f

        displaySettlementIcon(element, centerSize)
        displayFacilityIcons(element, centerSize)

        var help = element.addLunaSpriteElement("graphics/ui/icons/fleettab/more_info.png", LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, 32f, 32f).apply {
            getSprite().alphaMult = 1f
            getSprite().color = Misc.getDarkPlayerColor().brighter()
            position.inTL(mainPanel.position.width - 32 - 1, 35f)

            render {

                if (isHovering) {
                    var sprite = getSprite()
                    sprite.setAdditiveBlend()
                    sprite.alphaMult = 1f
                    sprite.render(x, y)
                    sprite.alphaMult = 1f
                    sprite.setNormalBlend()
                }

            }

            onHoverEnter {
                playScrollSound()
            }
            onClick {
                playClickSound()
            }
        }

        element.addTooltip(help.elementPanel, TooltipMakerAPI.TooltipLocation.RIGHT, 350f) { tooltip ->
            tooltip.addSectionHeading("Settlements", Alignment.MID, 0f)
            tooltip.addSpacer(3f)
            tooltip.addPara("The settlement lets you establishing a small-scale home-base within the sector. " +
                    "It provides some passive income, but noteably comes with a variety of utility related facilities that can be constructed." +
                    "\n\n" +
                    "A Settlement only has 6 plots that can hold a facility, with one always being used by its spaceport, meaning that there is a choice between 5 different facilities in your settlement. " +
                    "Constructing a new facility costs credits and time, but multiple facilities can be constructed at the same time." +
                    "\n\n" +
                    "Only one settlement can be constructed by the player.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "income", "facilities", "6", "5", "at the same time")

        }

        var config = element.addLunaSpriteElement("graphics/ui/icons/fleettab/suspend_repairs.png", LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, 32f, 32f).apply {
            getSprite().alphaMult = 1f
            getSprite().color = Misc.getDarkPlayerColor().brighter()
            position.belowLeft(help.elementPanel, 2f)

            render {

                if (isHovering) {
                    var sprite = getSprite()
                    sprite.setAdditiveBlend()
                    sprite.alphaMult = 1f
                    sprite.render(x, y)
                    sprite.alphaMult = 1f
                    sprite.setNormalBlend()
                }

            }


            var popupWidth = 300f
            var popupHeight = 350f

            onClick {
                playClickSound()
                element.addWindow(this.elementPanel, popupWidth, popupHeight) { window ->

                    var panelPlugin = BorderedPanelPlugin()
                    panelPlugin.renderBackground = true
                    panelPlugin.backgroundColor = Color(10, 10, 10)
                    panelPlugin.alpha = 0.8f

                    var windowPanel = window.panel.createCustomPanel(popupWidth, popupHeight, panelPlugin)
                    window.panel.addComponent(windowPanel)

                    var windowElement = windowPanel.createUIElement(popupWidth, popupHeight, false)
                    windowPanel.addUIElement(windowElement)

                    windowElement.addPara("").position.inTL(10f, 0f)

                    var auto = windowElement.addLunaToggleButton(data.autoDescend, popupWidth - 20, 30f).apply {
                        enableTransparency = true
                        this.borderAlpha = 0.5f
                        this.backgroundAlpha = 0.8f
                        changeStateText("Auto-Descend: On", "Auto-Descend: Off")
                        centerText()

                        advance {
                            data.autoDescend = value
                        }
                    }

                    windowElement.addTooltip(auto.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, 300f) { tooltip ->
                        tooltip.addPara("Automaticly moves you to the settlement screen instead of the planet screen. Only works on planets that dont have a colony.")
                    }

                    windowElement.addSpacer(10f)


                    var header = windowElement.addSectionHeading("Descend Description", Alignment.MID, 0f)
                    header.position.setSize(header.position.width - 10, header.position.height)

                    windowElement.addLunaTextfield("${data.description}", true, popupWidth - 20, popupHeight - 95).apply {
                        this.enableTransparency = true
                        this.borderAlpha = 0.5f
                        this.backgroundAlpha = 0.8f
                        this.elementPanel.position.belowLeft(auto.elementPanel, 40f)

                        this.advance {
                            data.description = this.getText()
                        }
                    }
                }
            }

            onHoverEnter {
                playScrollSound()
            }
        }

        element.addTooltip(config.elementPanel, TooltipMakerAPI.TooltipLocation.RIGHT, 300f) { tooltip ->
            tooltip.addPara("Click to configure aspects of the settlement.")
        }

        var abandon = element.addLunaSpriteElement("graphics/ui/icons/fleettab/scuttle.png", LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, 32f, 32f).apply {
            getSprite().alphaMult = 1f
            getSprite().color = Misc.getDarkPlayerColor().brighter()
            position.belowLeft(config.elementPanel, 2f)

            render {

                if (isHovering) {
                    var sprite = getSprite()
                    sprite.setAdditiveBlend()
                    sprite.alphaMult = 1f
                    sprite.render(x, y)
                    sprite.alphaMult = 1f
                    sprite.setNormalBlend()
                }

            }


            var popupWidth = 300f
            var popupHeight = 60f

            onClick {
                playClickSound()
                element.addWindow(this.elementPanel, popupWidth, popupHeight) { window ->

                    var panelPlugin = BorderedPanelPlugin()
                    panelPlugin.renderBackground = true
                    panelPlugin.backgroundColor = Color(10, 10, 10)
                    panelPlugin.alpha = 0.8f

                    var windowPanel = window.panel.createCustomPanel(popupWidth, popupHeight, panelPlugin)
                    window.panel.addComponent(windowPanel)

                    var windowElement = windowPanel.createUIElement(popupWidth, popupHeight, false)
                    windowPanel.addUIElement(windowElement)

                    windowElement.addPara("").position.inTL(10f, 0f)

                    var button = windowElement.addLunaChargeButton(popupWidth - 20, 30f).apply {
                        enableTransparency = true
                        this.borderAlpha = 0.5f
                        this.backgroundAlpha = 0.8f
                        addText("Abandon Settlement", Misc.getBasePlayerColor())
                        centerText()

                        onFinish {
                            playSound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                            abandonSettlement()
                        }
                    }

                    windowElement.addTooltip(button.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, 300f) { tooltip ->
                        tooltip.addPara("Hold to abandon the settlement. " +
                                "Abandoning the settlement allows you to create a new one, but causes you to loose all progress and benefits of this settlement.")
                    }
                }
            }

            onHoverEnter {
                playScrollSound()
            }
        }

        element.addTooltip(abandon.elementPanel, TooltipMakerAPI.TooltipLocation.RIGHT, 300f) { tooltip ->
            tooltip.addPara("Click to open the options for abandoning the settlement.")
        }

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

            var income = data.stats.income.modifiedValue * RATSettings.frontiersIncomeMult!!
            var incomeString = Misc.getDGSCredits(income)

            tooltip.addPara("A small settlement established on ${data.primaryPlanet.name}.", 0f)
            tooltip.addSpacer(10f)
            tooltip.addPara("The settlement acts autonomously in most of its duties. " +
                    "The settlement currently makes $incomeString credits per month from its ground operations.",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "$incomeString")

            var incomeStat = data.stats.income

            tooltip.addSpacer(10f)
            tooltip.addSectionHeading("Income", Alignment.MID, 0f)
            tooltip.addSpacer(10f)

            tooltip.addPara("Resource: ${Misc.getDGSCredits(incomeStat.baseValue)}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${Misc.getDGSCredits(incomeStat.baseValue)}")

            for (stat in incomeStat.flatMods) {
                var value = "+${stat.value.value}"
                tooltip.addPara("${stat.value.desc}: $value", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$value")
            }
            for (stat in incomeStat.percentMods) {
                var value = "+${stat.value.value}%"
                tooltip.addPara("${stat.value.desc}: $value%", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$value")
            }
            for (stat in incomeStat.multMods) {
                var value = "+${stat.value.value}x"
                tooltip.addPara("${stat.value.desc}: $value", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$value")
            }

           /* if (resource != null) {
                tooltip.addPara("The settlement harvests from a local hotspot of ${resource.getName()} for more substantual gains.",
                    0f, Misc.getTextColor(), Misc.getHighlightColor(), "${resource.getName()}")
            } else {
                tooltip.addPara("However, the settlement is not nearby any resource hotspot of significance",
                    0f, Misc.getTextColor(), Misc.getHighlightColor())
            }*/

            if (resource != null) {
                tooltip.addSpacer(10f)
                tooltip.addSectionHeading("Resource", Alignment.MID, 0f)
                tooltip.addSpacer(10f)

                var img = tooltip!!.beginImageWithText(resource.getIcon(), 48f)
                resource.getDescription(img)
                tooltip!!.addImageWithText(0f)

                if (resource.getSpec().canBeRefined) {
                    tooltip!!.addSpacer(10f)
                    var img = tooltip!!.beginImageWithText(resource.getRefinedIcon(), 48f)
                    img.addPara("Can be refined to increase the export value by 75%%. Requires the \"Refinery\" facility.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "75%", "Refinery")
                    tooltip!!.addImageWithText(0f)
                }

                tooltip!!.addSpacer(10f)
                var creditIcon = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/credit_icon.png")
                var img2 = tooltip!!.beginImageWithText("graphics/icons/frontiers/credit_icon.png", 48f)
                var income = resource.getSpec().conditions.get(data.primaryPlanet.market.conditions.find { resource.getSpec().conditions.contains(it.id) }!!.id)
                var incomeString = Misc.getDGSCredits(income!!.toFloat())
                img2.addPara("The richness of this sites resource hotspot promises an estimated income of ${incomeString} credits per month from exports (without refinement).", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${incomeString}")
                tooltip!!.addImageWithText(0f)
            }




        }
    }

    fun displayFacilityIcons(element: TooltipMakerAPI, centerSize: Float) {
        var radius = centerSize + 16f
        var angle = -60f
        var slots = data.facilitySlots
        for (slot in slots) {

            var facilitySize = 96f

            var centerX = width / 2 - facilitySize / 2
            var centerY = (height) / 2 - facilitySize / 2


            var plugin = slot.getPlugin()


            var facilityIcon = FacilityDisplayElement(data, slot, plugin, element, facilitySize, facilitySize)
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
                    var label = img.addPara("This plot is reserved for the \"${plugin.getName()}\". As it is vital to the settlements infrastructure, it can not be removed or replaced.", 0f)

                    label.setHighlightColors(Misc.getHighlightColor(), Misc.getNegativeHighlightColor())
                    label.setHighlight("${plugin.getName()}", "it can not be removed or replaced.")

                    tooltip.addImageWithText(0f)

                    tooltip.addSpacer(10f)
                    tooltip.addSectionHeading("Facility", Alignment.MID, 0f)
                    tooltip.addSpacer(10f)
                    plugin.addDescriptionToTooltip(tooltip)
                }
                else {
                    var img = tooltip.beginImageWithText(plugin.getIcon(), 48f)
                    img.addPara("This plot is reserved by the \"${plugin.getName()}\" facility. Left-Click to remove or replace it with another facility.", 0f,
                            Misc.getTextColor(), Misc.getHighlightColor(), "${plugin.getName()}", "Left-Click")
                    tooltip.addImageWithText(0f)

                    tooltip.addSpacer(10f)
                    tooltip.addSectionHeading("Facility", Alignment.MID, 0f)
                    tooltip.addSpacer(10f)

                    plugin.addDescriptionToTooltip(tooltip)

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
            .filter { plugin -> data.facilitySlots.none { facilities -> facilities.facilityID == plugin.getID() } && plugin.getID() != "landing_pad" }

        plugins = plugins.sortedByDescending { it.canBeBuild() && (it.getCost() * RATSettings.frontiersCostMult!!) <= Global.getSector().playerFleet.cargo.credits.get() }

        for (plugin in plugins) {
            plugin.settlement = data
        }

        element.addSpacer(5f)



        if (slot.facilityID != "") {
            var removalElement = element.addLunaElement(popupWidth - 10, 30f).apply {
                enableTransparency = true
                borderAlpha = 0.2f
                backgroundAlpha = 0.4f
                var text = "Click to remove the current facility"
                if (slot.isBuilding) text = "Cancel construction & refund credits"
                addText(text, Misc.getBrightPlayerColor())
                centerText()

                onHoverEnter {
                    playScrollSound()
                    backgroundAlpha = 0.6f
                }
                onHoverExit {
                    backgroundAlpha = 0.4f
                }

                onClick {
                    if (!it.isLMBEvent) return@onClick
                    playClickSound()
                    playSound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                    slot.removeCurrentFacility()
                    window.close()
                    dialogPlugin.populateOptions()
                    refresh()
                }
            }
            element.addSpacer(10f)
        }



        //Picker List
        for (plugin in plugins.filter { it.shouldShowInPicker() }) {

            var facilityElement = element.addLunaElement(popupWidth - 10, 60f).apply {
                enableTransparency = true
                backgroundAlpha = 0.3f
                borderAlpha = 0.1f

                var inner = innerElement

                var cost = plugin.getCost() * RATSettings.frontiersCostMult!!
                var costString = Misc.getDGSCredits(cost)

                inner.addSpacer(6f)

                var cantBeConstructed = cost >= Global.getSector().playerFleet.cargo.credits.get() || !plugin.canBeBuild()

                var color = Misc.getHighlightColor()
                if (cantBeConstructed) {
                    color = Misc.getNegativeHighlightColor()
                    backgroundColor = Misc.getNegativeHighlightColor().darker().darker().darker()
                    borderColor = Misc.getNegativeHighlightColor().darker().darker()
                }

                var text = inner.beginImageWithText(plugin.getIcon(), 48f)
                text.addPara("${plugin.getName()} - $costString \n${plugin.getShortDesc()}", 0f, Misc.getTextColor(), color, "${plugin.getName()}", "$costString")
                inner.addImageWithText(0f)

                onClick {
                    if (!it.isLMBEvent || cantBeConstructed) return@onClick
                    playClickSound()
                    playSound(Sounds.STORY_POINT_SPEND, 1f, 1f)

                    Global.getSector().playerFleet.cargo.credits.subtract(cost)
                    var formated = Misc.getDGSCredits(cost)
                    Global.getSector().campaignUI.messageDisplay.addMessage("Spend $formated credits", Misc.getBasePlayerColor(), "$formated", Misc.getHighlightColor())
                    slot.installNewFacility(plugin.getID())
                    window.close()
                    dialogPlugin.populateOptions()
                    refresh()
                }

                onHoverEnter {
                    playScrollSound()
                    backgroundAlpha = 0.6f
                }
                onHoverExit {
                    backgroundAlpha = 0.3f
                }

                element.addTooltip(elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, 400f) {tooltip ->
                    tooltip.addTitle("${plugin.getName()}")
                    tooltip.addSpacer(10f)
                    plugin.addDescriptionToTooltip(tooltip)
                    tooltip.addSpacer(10f)
                    var days = plugin.getBuildTime()
                    var playerCash =  Misc.getDGSCredits( Global.getSector().playerFleet.cargo.credits.get())
                    tooltip.addPara("$costString credits and $days days to build. You have $playerCash.",
                        0f, Misc.getTextColor(), Misc.getHighlightColor(), "$costString", "$days", "$playerCash")

                    if (!plugin.canBeBuild()) {
                        tooltip.addSpacer(10f)
                        plugin.canNotBeBuildReason(tooltip, data)
                    }
                }
            }
            element.addSpacer(10f)
        }




        windowPanel.addUIElement(element)
    }

    fun abandonSettlement() {

        callbacks.dismissDialog()
        dialogPlugin.dialog.dismiss()

        for (mod in data.modifiers) {
            mod.unapply()
        }

        for (slot in data.facilitySlots) {
            if (slot.isFunctional()) {
                slot.getPlugin()?.unapply()
            }
        }

        Global.getSector().removeScript(data.mananger)
        Global.getSector().listenerManager.removeListener(data.mananger)

        Global.getSector().intelManager.removeIntel(data.intel)

        data.settlementEntity.fadeAndExpire(1f)

        FrontiersUtils.getFrontiersData().activeSettlement = null

    }
}