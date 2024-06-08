package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.interactions.exoship.ui.ExoCheckbox
import assortment_of_things.exotech.interactions.exoship.ui.ExoResourceSlider
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addTooltip
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCampaignEntityPickerListener
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaUI.elements.LunaSpriteElement
import org.lazywizard.lazylib.MathUtils

class PlayerExoshipInteraction : RATInteractionPlugin() {

    var shownInteractionText = false
    var openedFromAbility = false

    var scrollerY = 0f

    var selectedDestination: SectorEntityToken? = null

    override fun init() {

        if (!shownInteractionText && !openedFromAbility) {
            textPanel.addPara("Your fleet approaches the Exoship.")
            shownInteractionText = true

            textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)
        }



        createOption("Manage Exoship") {
            recreateManagementOptions()
        }

        addLeaveOption()
    }


    fun recreateManagementOptions() {

        clearOptions()

        var exoship = interactionTarget.customPlugin as ExoshipEntity
        var playerData = exoship.playerModule

        var width = 500f
        var height = 450f

        var panel = visualPanel.showCustomPanel(width, height, null)
        var element = panel.createUIElement(width, height, true)


        element.addSpacer(5f)
        element.addLunaSpriteElement("graphics/illustrations/rat_exoship_management.jpg", LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, 500f - 15, 147f).apply {
            borderAlpha = 1f
            renderBorder = true
            enableTransparency = true
        }

        element.addSpacer(10f)
        element.addSectionHeading("Fuel", Alignment.MID, 0f)
        element.addSpacer(10f)


        var remaining = playerData.currentFuelPercent
        if (selectedDestination != null) {
            remaining = calculateFuelRemaining(interactionTarget.starSystem, selectedDestination!!.starSystem)
        }

        element.addPara("The bar shows how much fuel is currently stored within the ship. " +
                "While a destination is selected, a darker bar displays how much fuel will be consumed with the warp.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "a darker bar displays how much fuel will be consumed with the warp")

        element.addSpacer(10f)

        var fuelbar = ExoFuelbar(remaining, playerData.currentFuelPercent, playerData.maxFuelPercent, element, width-15, 40f)

        element.addTooltip(fuelbar.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, width-25) {
            var remainingString = (MathUtils.clamp(remaining, 0f, 1f) * 100).toInt()
            var currentString = (playerData.currentFuelPercent * 100).toInt()

            it.addPara("The percentage of fuel is currently at $currentString%%. The currently selected destination would decrease it to $remainingString%% after a warp.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "$currentString%", "$remainingString%")
        }

        element.addSpacer(10f)
        element.addSectionHeading("Warp Configuration", Alignment.MID, 0f)
        element.addSpacer(10f)

        var bringPlayerCheckbox = ExoCheckbox(playerData.playerJoinsWarp, element, 375f, 24f).apply {
            var para = this.innerElement.addPara("Bring player along for the warp.", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
            para.position.inTL(24f + 10f,0f + para.computeTextHeight("") / 3)

            advance {
                playerData.playerJoinsWarp = this.value
            }
        }


        element.addSpacer(10f)
        element.addSectionHeading("Management", Alignment.MID, 0f)
        element.addSpacer(10f)

        element.addPara("The exoship requires a specific amount of fuel per distance traveled and is able to produce it over time. " +
                "You can configure the budget allocated to generating more fuel, but faster production rates have worse cost-efficiency. This cost only applies if its tanks arent full." +
                "", 0f ,
        Misc.getTextColor(), Misc.getHighlightColor(), "produce it over time", "worse cost-efficiency", "arent full")

        element.addSpacer(10f)

        var imgPath = "graphics/ui/rat_exo_fuel.png"
        Global.getSettings().getAndLoadSprite(imgPath)

        var img = element.beginImageWithText(imgPath, 48f)

        var slider = ExoResourceSlider(img, 375f, 30f, playerData.fuelProductionLevel, 0f, playerData.maxFuelPercent).apply {

        }

        var percentagePara = slider.innerElement.addPara("", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
        percentagePara.position.inTL(slider.width + 10,0f + percentagePara.computeTextHeight("") / 2)



        element.addImageWithText(0f)

        element.addSpacer(10f)

        var fuelInfoPara = element.addPara("It currently produces 750 units of fuel at a cost of 7500 credits per month.",
        0f, Misc.getTextColor(), Misc.getHighlightColor(), "750", "7500")

        slider.advance {
            percentagePara.text = (slider.level * 100).toInt().toString() + "%"


            var fuel = (0 + (playerData.fuelPercentPerMonthMax * slider.level) * 100).toInt()
            var cost = (0 + (playerData.computeMonthlyCost(slider.level))).toInt()

            fuelInfoPara.text = "At the selected rate, the ship recharges $fuel% of its total fuel tank at a cost of $cost credits per month."
            fuelInfoPara.setHighlight("$fuel%", "$cost")

            playerData.fuelProductionLevel = slider.level
        }




        element.addSpacer(20f)

        panel.addUIElement(element)
        element.externalScroller.yOffset = scrollerY


        createOption("Warp") {
            warp()
            closeDialog()
        }

        if (selectedDestination == null) {
            optionPanel.setEnabled("Warp", false)
            optionPanel.setTooltip("Warp", "No destination selected.")
        } else {
            optionPanel.addOptionConfirmation("Warp", "Are you sure you want to warp the Exoship towards ${selectedDestination!!.starSystem.nameWithNoType}?", "Confirm", "Cancel")
        }

        if (remaining < 0) {
            optionPanel.setEnabled("Warp", false)
            optionPanel.setTooltip("Warp", "The ship does not have enough fuel left to innitiate this warp.")
        }

        createOption("Select Destination") {
            var entities = Global.getSector().economy.marketsCopy.filter { !it.isHidden }.map { it.primaryEntity }.toMutableList()
            var centers = entities.addAll(Global.getSector().starSystems.filter {
                !it.hasTag(Tags.THEME_HIDDEN) && !it.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) && !it.hasTag(Tags.SYSTEM_ABYSSAL) }.map { it.center }.filter { it != null })

            dialog.showCampaignEntityPicker("Select a destination", "Destination", "Confirm", Global.getSector().getFaction("rat_exotech"),
                entities, object : BaseCampaignEntityPickerListener() {

                    var arrowList = ArrayList<ArrowData>()

                    override fun getMenuItemNameOverrideFor(entity: SectorEntityToken?): String? {

                        if (entity?.starSystem?.center == entity) {
                            return "Random Orbit"
                        }

                        return super.getMenuItemNameOverrideFor(entity)
                    }

                    override fun pickedEntity(entity: SectorEntityToken?) {
                        super.pickedEntity(entity)

                        if (entity?.containingLocation != interactionTarget.containingLocation) {
                            scrollerY = element.externalScroller.yOffset
                            selectedDestination = entity
                            recreateManagementOptions()
                        }
                    }

                    override fun canConfirmSelection(entity: SectorEntityToken?): Boolean {
                        if (entity == null) return false
                        var remaining = calculateFuelRemaining(interactionTarget.starSystem, entity!!.starSystem)

                        return remaining >= 0f
                    }

                    override fun createInfoText(info: TooltipMakerAPI?, entity: SectorEntityToken?) {
                        var starsystem = entity?.starSystem
                        var remaining = playerData.currentFuelPercent
                        var cost = 0

                        if (starsystem != null) {
                            remaining = calculateFuelRemaining(interactionTarget.starSystem, starsystem)
                            cost = (calculateFuelCost(interactionTarget.starSystem, starsystem) * 100).toInt()

                            arrowList.clear()
                            var data = ArrowData(10f, interactionTarget, starsystem.center, ExoUtils.color1)
                            arrowList.add(data)
                        }

                        var bar = ExoFuelbar(remaining, playerData.currentFuelPercent, playerData.maxFuelPercent, info!!, 400f, 30f)

                        var color = Misc.getHighlightColor()
                        if (remaining < 0f) {
                            color = Misc.getNegativeHighlightColor()
                        }

                        var barPara = bar.innerElement.addPara("This warp will consume $cost%% of \nthe ships fuel.", 0f, Misc.getTextColor(), color, "$cost%")
                        barPara.position.inTL(bar.width + 10,0f)

                    }

                    override fun getArrows(): MutableList<IntelInfoPlugin.ArrowData> {




                        return arrowList
                    }


                })
        }

        createOption("Back") {
            clearOptions()
            init(dialog)
        }
    }

    fun warp() {
        var exoship = interactionTarget.customPlugin as ExoshipEntity
        var playerData = exoship.playerModule


        if (selectedDestination == null) return

        var playerJoins = playerData.playerJoinsWarp
        var remaining = calculateFuelRemaining(interactionTarget.starSystem, selectedDestination!!.starSystem)

        remaining = MathUtils.clamp(remaining, 0f, 1f)

        playerData.currentFuelPercent = remaining

        if (selectedDestination!!.starSystem.center == selectedDestination) {
            exoship.warpModule.warp(selectedDestination!!.starSystem, playerJoins)
        }
        else {
            exoship.warpModule.warp(selectedDestination!!, playerJoins)
        }

        selectedDestination = null
    }

    fun calculateFuelCost(current: StarSystemAPI, destinationSystem: StarSystemAPI) : Float {
        var exoship = interactionTarget.customPlugin as ExoshipEntity
        var playerData = exoship.playerModule

        var distance = Misc.getDistanceLY(current.location, destinationSystem.location)
        var cost = playerData.fuelPerLightyear * distance
        return cost
    }

    fun calculateFuelRemaining(current: StarSystemAPI, destinationSystem: StarSystemAPI) : Float{
        var exoship = interactionTarget.customPlugin as ExoshipEntity
        var playerData = exoship.playerModule

        var cost = playerData.currentFuelPercent - calculateFuelCost(current, destinationSystem)
        return cost
    }
}