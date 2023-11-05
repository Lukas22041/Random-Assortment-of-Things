package assortment_of_things.backgrounds.commander

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.IndustryPickerListener
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaChargeButton
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaTextfield
import lunalib.lunaExtensions.addLunaToggleButton
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.text.DecimalFormat

class BaseCommanderStationInteraction : RATInteractionPlugin() {

    var scrollerPosition = 0f
    lateinit var listener: CommanderStationListener

    override fun init() {

        textPanel.addPara("A small station in ${interactionTarget.starSystem.nameWithNoType} commanded by yourself. Traffic within and near the station is low but steady.")
        listener = interactionTarget.memoryWithoutUpdate.get("\$rat_commander_listener") as CommanderStationListener

        addMainOptions()
    }

    fun addMainOptions() {

        clearOptions()

        visualPanel.showImageVisual(interactionTarget.customInteractionDialogImageVisual)

        createOption("Manage Station") {
            showStationManagementScreen()
        }


        // optionPanel.addOption("Access Cargo Storage", "CARGO")


        //Production
        createOption("Order ship & weapon production") {
            dialog.showCustomProductionPicker(CommanderCustomProduction(interactionTarget.market, listener))
        }

        var hasProduction = listener.hasActiveProject("ship_production") || listener.hasActiveProject("weapon_production")
        if (!hasProduction) {
            optionPanel.setEnabled("Order ship & weapon production", false)
            optionPanel.setTooltip("Order ship & weapon production", "Requires either the ship or weapon & fighter project to be built.")
        }
        else {
            optionPanel.setTooltip("Order ship & weapon production", "Request custom production from the station. Orders will be delivered to this stations cargo at the end of the month.")
        }

        //Fleet & Storage
        createOption("Access Cargo Storage") {
            visualPanel.showCore(CoreUITabId.CARGO, interactionTarget) { }
        }
        optionPanel.setShortcut("Access Cargo Storage", Keyboard.KEY_I, false, false, false, false)

        createOption("Inspect your Fleet") {
            visualPanel.showCore(CoreUITabId.FLEET, interactionTarget) { }
        }
        optionPanel.setShortcut("Inspect your Fleet", Keyboard.KEY_F, false, false, false, false)

        createOption("Use the local facilities to refit your fleet") {
            visualPanel.showCore(CoreUITabId.REFIT, interactionTarget) { }
        }
        optionPanel.setShortcut("Use the local facilities to refit your fleet", Keyboard.KEY_R, false, false, false, false)


        addLeaveOption()
    }

    fun showStationManagementScreen() {
        clearOptions()


        textPanel.addPara("Adjust & Manage a variety of features of this station.")

        textPanel.addPara("Each month based on the state of the station it makes some profit, most of it goes in to its own bank, but the owner gets a 20%% cut of all income. " +
                "While the owner can not directly access the profit in the stations bank, they can make decisions on which projects to invest the credits in to.",
            Misc.getTextColor(), Misc.getHighlightColor(), "20%")

        textPanel.addPara("These projects can increase the stations income or unlock more tools for the owner to make use of.")

       refreshManagementVisual()



        createOption("Back", key = Keyboard.KEY_ESCAPE) {
            addMainOptions()
        }
    }

    fun refreshManagementVisual() {

        var width = 500f
        var height = 500f
        var panel = visualPanel.showCustomPanel(width, height, null)
        var element = panel.createUIElement(width, height, true)
        // element.addPara("", 0f).position.inTL(10f, 0f)

        element.addLunaElement(width - 10, 40f).apply {
            renderBackground = false
            renderBorder = false

            var textfield = innerElement.addLunaTextfield(interactionTarget.name, false, 200f, 30f)
            textfield.enableTransparency = true
            textfield.advance {
                interactionTarget.name = textfield.getText()
            }

            var renamePara = innerElement.addPara("Rename the Station", 0f)
            renamePara.position.rightOfMid(textfield.elementPanel, 10f)
        }

        element.addSectionHeading("Earnings", Alignment.MID, 0f)
        element.addSpacer(10f)

        var stationCredits = listener.bank
        var stationEarnings = (listener.calculateIncome() * 0.8f).toInt()
        var playerEarnings = (stationEarnings * 0.2f).toInt()

        var formatter = DecimalFormat("###,###")

        var stationCreditsString = formatter.format(stationCredits)
        var stationEarningsString = formatter.format(stationEarnings)
        var playerEarningsString = formatter.format(playerEarnings)

        var stationBankIMG = element.beginImageWithText(interactionTarget.faction.crest, 40f)
        stationBankIMG.addPara("Station Bank: $stationCreditsString credits", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$stationCreditsString")
        element.addImageWithText(0f)

        element.addSpacer(10f)

        var stationCreditIMG = element.beginImageWithText(interactionTarget.faction.crest, 40f)
        stationCreditIMG.addPara("Station Earnings per Month: $stationEarningsString credits", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$stationEarningsString")
        element.addImageWithText(0f)

        element.addSpacer(10f)

        var playerCreditsIMG = element.beginImageWithText(Global.getSector().playerFaction.crest, 40f)
        playerCreditsIMG.addPara("Owner Earnings per Month: $playerEarningsString credits", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$playerEarningsString")
        element.addImageWithText(0f)

        element.addSpacer(10f)
        element.addSectionHeading("Station Projects", Alignment.MID, 0f)
        element.addSpacer(10f)

        var buildProjects = listener.allProjects.filter { it.active }.map { it.getID() }
        var projects = listener.allProjects
        for (project in projects.sortedBy { it.getOrder() }) {

            var path = project.getIcon()
            Global.getSettings().loadTexture(path)

            var build = project.active
            var available = false
            var enoughCredits = listener.bank >= project.getCost()
            var hasPrerequisites = project.canBeBuild(buildProjects as ArrayList<String>)
            if (enoughCredits && hasPrerequisites) available = true

            var extraText = ""
            var otherTitleColor = Misc.getTextColor()

            if (build) {
                extraText = " (Built)"
            }
            if (!available && !build) {
                extraText = " (Unavailable)"
                otherTitleColor = Misc.getGrayColor()
            }

            var projectWidget = element.addLunaChargeButton(width - 10f, 60f).apply {
                backgroundColor = Misc.getDarkPlayerColor().darker().darker()
                renderBackground = false
                renderBorder = false
                increaseRate = 0.03f


                if (build) {
                    increaseRate = 0f
                }
                if (!available) {
                    increaseRate = 0f
                }

                innerElement.addSpacer(5f)

                var projectIMG = innerElement.beginImageWithText(path, 50f)
                projectIMG.addPara(project.getName() + extraText, 0f, otherTitleColor, Misc.getHighlightColor(), "${project.getName()}")
                project.addDescription(projectIMG)

                innerElement.addImageWithText(0f)

                onFinish {
                    Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                    project.active = true
                    scrollerPosition = element.externalScroller.yOffset
                    listener.bank -= project.getCost()
                    listener.currentProductionBudget += project.getCustomProductionBudget()
                    refreshManagementVisual()
                }
            }

            element.addTooltipTo(object : TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any?): Float {
                    return 500f
                }

                override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any?) {

                    if (!build && available) {
                        tooltip.addPara("(Hold to Construct)", 0f, Misc.getGrayColor(), Misc.getGrayColor())
                        tooltip.addSpacer(10f)
                    }




                    var projectIMG = tooltip.beginImageWithText(path, 40f)
                    projectIMG.addPara(project.getName() + extraText, 0f, otherTitleColor, Misc.getHighlightColor(), "${project.getName()}")
                    project.addDescription(projectIMG)
                    tooltip.addImageWithText(0f)

                    tooltip.addSpacer(10f)

                    project.addLongDescription(tooltip)

                    if (!build) {
                        tooltip.addPara("Cost: ${formatter.format(project.getCost())}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Cost:")
                    }
                    tooltip.addPara("Income: ${formatter.format(project.getIncome())}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Income:")
                    tooltip.addSpacer(10f)

                    if (!enoughCredits && !build) {
                        tooltip.addNegativePara("Not enough credits in bank.")
                    }

                    if (!hasPrerequisites && !build) {
                        tooltip.addNegativePara("Missing Prerequisites.")
                    }
                }

            }, projectWidget.innerElement, TooltipMakerAPI.TooltipLocation.BELOW)

            element.addSpacer(10f)
        }



        panel.addUIElement(element)
        element.externalScroller.yOffset = scrollerPosition
    }

}