package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.interactions.exoship.ui.ExoCheckbox
import assortment_of_things.exotech.interactions.exoship.ui.ExoResourceSlider
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCampaignEntityPickerListener
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.MapParams
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaUI.elements.LunaSpriteElement

class PlayerExoshipInteraction : RATInteractionPlugin() {

    var shownInteractionText = false
    var openedFromAbility = false

    var scrollerY = 0f

    override fun init() {

        if (!shownInteractionText && !openedFromAbility) {
            textPanel.addPara("Your fleet approaches the Exoship.")
            shownInteractionText = true

            textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)
        }



        createOption("Manage Exoship") {
            clearOptions()
            openManagementPanel()

            createOption("Back") {
                clearOptions()
                init(dialog)
            }
        }

        addLeaveOption()
    }


    fun openManagementPanel() {

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

        //Fuelbar here

        element.addSpacer(10f)
        element.addSectionHeading("Management", Alignment.MID, 0f)
        element.addSpacer(10f)

        element.addPara("The exoship requires a specific amount of fuel per distance traveled and is able to produce it over time. " +
                "You can configure the budget allocated to generating more fuel, but faster production rates have worse cost-efficiency. This cost only applies if its tanks arent full." +
                "", 0f ,
        Misc.getTextColor(), Misc.getHighlightColor(), "produce it over time", "worse cost-efficiency" )

        element.addSpacer(10f)

        var img = element.beginImageWithText("graphics/icons/markets/am_fuel_facility.png", 48f)

        var slider = ExoResourceSlider(img, 375f, 30f, 0.5f, 0f, 3f).apply {

        }

        var percentagePara = slider.innerElement.addPara("Test", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
        percentagePara.position.inTL(slider.width + 10,0f + percentagePara.computeTextHeight("") / 2)



        element.addImageWithText(0f)

        element.addSpacer(10f)

        var fuelInfoPara = element.addPara("It currently produces 750 units of fuel at a cost of 7500 credits per month.",
        0f, Misc.getTextColor(), Misc.getHighlightColor(), "750", "7500")

        slider.advance {
            percentagePara.text = (slider.level * 100).toInt().toString() + "%"


            var fuel = (250 + (1750 * slider.level)).toInt()
            var cost = (1000 + (49000 * (slider.level * slider.level))).toInt()

            fuelInfoPara.text = "It currently produces $fuel units of fuel at a cost of $cost credits per month."
            fuelInfoPara.setHighlight("$fuel", "$cost")
        }

        element.addSpacer(10f)
        element.addSectionHeading("Warp", Alignment.MID, 0f)
        element.addSpacer(10f)

        //Copy the Checkbox i made for Nex over for the "Bring player along" option

        var bringPlayerCheckbox = ExoCheckbox(playerData.playerJoinsWarp, element, 375f, 24f).apply {
            var para = this.innerElement.addPara("Bring playerfleet along for the warp.", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
            para.position.inTL(24f + 10f,0f + percentagePara.computeTextHeight("") / 3)

            advance {
                playerData.playerJoinsWarp = this.value
            }
        }



        element.addSpacer(20f)


        var destinationButton = element.addLunaElement(480f, 40f).apply {
            enableTransparency = true
            addText("Select Destination")
            centerText()

            onClick {
                playClickSound()

                var entities = Global.getSector().economy.marketsCopy.filter { !it.isHidden }.map { it.primaryEntity }.toMutableList()
                var centers = entities.addAll(Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_HIDDEN) && !it.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) }.map { it.center })

                dialog.showCampaignEntityPicker("Select a destination", "Destination", "Confirm", Global.getSector().getFaction("rat_exotech"),
                    entities, object : BaseCampaignEntityPickerListener() {
                        override fun getMenuItemNameOverrideFor(entity: SectorEntityToken?): String? {

                            if (entity?.starSystem?.center == entity) {
                                return "Random Orbit"
                            }

                            return super.getMenuItemNameOverrideFor(entity)
                        }

                        override fun pickedEntity(entity: SectorEntityToken?) {
                            super.pickedEntity(entity)

                            scrollerY = element.externalScroller.yOffset
                            playerData.selectedDestination = entity
                            openManagementPanel()
                        }

                        override fun canConfirmSelection(entity: SectorEntityToken?): Boolean {
                            return entity != null
                        }
                    })
            }

        }

        element.addSpacer(15f)

        var warpButton = element.addLunaElement(480f, 40f).apply {
            enableTransparency = true
            addText("Warp")
            centerText()

            onClick {
                playClickSound()

                warp()
                closeDialog()
            }

        }

        element.addSpacer(10f)


        panel.addUIElement(element)
        element.externalScroller.yOffset = scrollerY
    }

    fun warp() {
        var exoship = interactionTarget.customPlugin as ExoshipEntity
        var playerData = exoship.playerModule

        if (playerData.selectedDestination == null) return

        var playerJoins = playerData.playerJoinsWarp


        if (playerData.selectedDestination!!.starSystem.center == playerData.selectedDestination) {
            exoship.warpModule.warp(playerData.selectedDestination!!.starSystem, playerJoins)
        }
        else {
            exoship.warpModule.warp(playerData.selectedDestination!!, playerJoins)
        }

        playerData.selectedDestination = null
    }
}