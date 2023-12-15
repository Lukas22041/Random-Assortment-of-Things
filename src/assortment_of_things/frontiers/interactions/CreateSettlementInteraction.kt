package assortment_of_things.frontiers.interactions

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.data.SettlementFacilitySlot
import assortment_of_things.frontiers.data.SiteData
import assortment_of_things.frontiers.scripts.SettlementManager
import assortment_of_things.frontiers.submarkets.SettlementStoragePlugin
import assortment_of_things.frontiers.ui.SiteSelectionPickerElement
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

class CreateSettlementInteraction : RATInteractionPlugin() {

    lateinit var previousPlugin: InteractionDialogPlugin
    var selectedSite: SiteData? = null

    override fun init() {
        visualPanel.showLargePlanet(interactionTarget as PlanetAPI)
        textPanel.addPara("A Settlement is a small piece of land located on a planet. " +
                "One can be established in either a fringe world, or a populated one if the faction in power grants permission. " +
                "A settlement can fullfill the needs of a new commander that seeks to expand his influence.")

        textPanel.addPara("To found a new settlement, a site has to be selected. A planet has multiple sites and their contents are correlated to the conditions of the planet. " +
                "It defines which ressource can be harvested and what effects the Settlement will be under.")

        var planet = interactionTarget as PlanetAPI

        if (planet.faction == null || planet.faction.isNeutralFaction) {
            textPanel.addPara("This planet is not under the control of anyone. Establishing a settlement here will require a start-up cost of 500.000 credits to get the foundations up and running.",
            Misc.getTextColor(), Misc.getHighlightColor(), "500.000")
        }
        else {
            textPanel.addPara("This planet is under ${planet.faction.displayNameWithArticle}'s influence. Acquiring the rights to a site and establishing the settlement will require 800.000 credits. All export income will have a 30%% cut taken, and if you turn hostile towards the faction, the settlement will no longer be accessible.",
            Misc.getTextColor(), Misc.getHighlightColor(), "${planet.faction.displayNameWithArticle}'s", "800.000", "30%")
        }

        textPanel.addPara("You can only own a single Settlement at a time.")

        populateOptions()
    }

    fun populateOptions() {
        clearOptions()
        createOption("Select a site") {

            var selectedHexagon: SiteSelectionPickerElement.SiteHexagon? = null

            dialog.showCustomDialog(1000f, 500f, object: BaseCustomDialogDelegate() {

                var basePanel: CustomPanelAPI? = null
                var panel: CustomPanelAPI? = null

                var leftPanel: CustomPanelAPI? = null
                var leftElement: TooltipMakerAPI? = null

                override fun createCustomDialog(panel: CustomPanelAPI?, callback: CustomDialogDelegate.CustomDialogCallback?) {
                    basePanel = panel
                    recreatePanel()
                }

                fun recreatePanel() {

                    if (panel != null) {
                        basePanel!!.removeComponent(panel)
                    }
                    var width = basePanel!!.position.width
                    var height = basePanel!!.position.height
                    panel = basePanel!!.createCustomPanel(width, height, null)
                    basePanel!!.addComponent(panel!!)

                    var element = panel!!.createUIElement(1000f, height, false)
                    panel!!.addUIElement(element)
                    //element.addSectionHeading("Select a Site", Alignment.MID, 0f)

                    leftPanel = Global.getSettings().createCustom(440f, height, null)
                    element.addCustom(leftPanel, 0f)
                    addLeftPanel(leftPanel!!)

                    var rightPanel = Global.getSettings().createCustom(550f, height, null)
                    element.addCustom(rightPanel, 0f)
                    addRightPanel(rightPanel)

                    rightPanel.position.rightOfMid(leftPanel, 10f)

                }

                fun addLeftPanel(leftPanel: CustomPanelAPI) {
                    leftElement = leftPanel!!.createUIElement(440f, 500f, false)
                    leftPanel!!.addUIElement(leftElement)
                    leftElement!!.addSectionHeading("Info", Alignment.MID, 0f)
                    leftElement!!.addSpacer(10f)
                    leftElement!!.addPara("The selected site influences multiple characteristics about your settlement. " +
                            "Most importantly it allows choosing a resources hotspot that can be extracted from for further income and effects for the settlement.", 0f)

                    if (selectedHexagon == null) {
                        leftElement!!.addSpacer(10f)
                        leftElement!!.addPara("Select a site on the right for more information.", 0f)
                        return
                    }

                    var site = selectedHexagon!!.site

                    leftElement!!.addSpacer(10f)
                    leftElement!!.addSectionHeading("Resource", Alignment.MID, 0f)
                    leftElement!!.addSpacer(10f)
                    Global.getSettings().loadTexture("graphics/icons/mission_marker.png")
                    var ressource = FrontiersUtils.getRessource(site)
                    if (ressource != null) {
                        var img = leftElement!!.beginImageWithText(ressource.getIcon(), 48f)
                        img.addPara("${ressource.getName()}:\n${ressource.getDescription()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${ressource.getName()}")
                        leftElement!!.addImageWithText(0f)

                        if (ressource.canBeRefined()) {
                            leftElement!!.addSpacer(10f)
                            var img = leftElement!!.beginImageWithText(ressource.getRefinedIcon(), 48f)
                            img.addPara("Can be refined to increase the export value by 50%%. Requires the \"Refinery\" facility.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "50%", "Refinery")
                            leftElement!!.addImageWithText(0f)
                        }

                        leftElement!!.addSpacer(10f)
                        var creditIcon = Global.getSettings().getAndLoadSprite("graphics/icons/frontiers/credit_icon.png")
                        var img2 = leftElement!!.beginImageWithText("graphics/icons/frontiers/credit_icon.png", 48f)
                        var income = ressource.spec.conditions.get(interactionTarget.market.conditions.find { ressource.spec.conditions.contains(it.id) }!!.id)
                        var incomeString = Misc.getDGSCredits(income!!.toFloat())
                        img2.addPara("The richness of this sites resource hotspot promises an estimated income of ${incomeString} credits per month from exports (without refinement).", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${incomeString}")
                        leftElement!!.addImageWithText(0f)
                    }
                    else {
                        var img = leftElement!!.beginImageWithText("graphics/icons/mission_marker.png", 48f)
                        img.addPara("This site has no resource.", 0f)
                        leftElement!!.addImageWithText(0f)
                    }

                   /* leftElement!!.addSpacer(10f)
                    leftElement!!.addSectionHeading("Modifiers", Alignment.MID, 0f)
                    leftElement!!.addSpacer(10f)

                    var img = leftElement!!.beginImageWithText("graphics/icons/mission_marker.png", 48f)
                    img.addPara("Placeholder Modifier 1", 0f)
                    leftElement!!.addImageWithText(0f)
                    leftElement!!.addSpacer(10f)
                    img = leftElement!!.beginImageWithText("graphics/icons/mission_marker.png", 48f)
                    img.addPara("Placeholder Modifier 2", 0f)
                    leftElement!!.addImageWithText(0f)
                    leftElement!!.addSpacer(10f)
                    img = leftElement!!.beginImageWithText("graphics/icons/mission_marker.png", 48f)
                    img.addPara("Placeholder Modifier 3s", 0f)
                    leftElement!!.addImageWithText(0f)
                    leftElement!!.addSpacer(10f)*/
                }

                fun addRightPanel(rightPanel: CustomPanelAPI) {
                    var element = rightPanel!!.createUIElement(550f, 500f, false)
                    rightPanel!!.addUIElement(element)
                    element.addSectionHeading("Site Selection", Alignment.MID, 0f)

                    element.addSpacer(10f)

                    var picker = SiteSelectionPickerElement(interactionTarget as PlanetAPI, Global.getSettings().getSprite((interactionTarget as PlanetAPI).spec.texture),
                        element, 550f, 450f)

                    picker.advance {


                        var selected = picker.selectedHexagon
                        //if (picker.hoveredHexagon != null) selected = picker.hoveredHexagon

                        if (selected != selectedHexagon && selected != null) {
                            selectedHexagon = selected
                            leftPanel!!.removeComponent(leftElement)
                            addLeftPanel(leftPanel!!)
                        }
                    }
                }

                override fun customDialogConfirm() {
                    if (selectedHexagon != null) {
                        selectedSite = selectedHexagon!!.site
                        populateOptions()
                    }
                }

                override fun getConfirmText(): String {
                    return "Select Site"
                }

                override fun hasCancelButton(): Boolean {
                    return true
                }
                override fun getCancelText(): String {
                    return "Cancel"
                }
            })
        }

       /* if (selectedSite != null) {
            textPanel.addPara("Site Description")
        }*/

        createOption("Establish the settlement") {
            clearOptions()

            var settlementDelegate = interactionTarget.starSystem.addCustomEntity("rat_frontiers_settlement_${Misc.genUID()}", "${interactionTarget.name} Settlement", "rat_frontiers_settlement", Factions.PLAYER)
            //station!!.setCircularOrbit(system.center, MathUtils.getRandomNumberInRange(0f, 360f), 3000f, 180f)
            settlementDelegate.setCircularOrbitPointingDown(interactionTarget, 1f, 1f, 360f)
            settlementDelegate.memoryWithoutUpdate["\$abandonedStation"] = true
            val market = Global.getFactory().createMarket("rat_station_commander_market", settlementDelegate.name, 3)
            market.surveyLevel = MarketAPI.SurveyLevel.FULL
            market.primaryEntity = settlementDelegate
            market.factionId = Factions.PLAYER
            market.addIndustry(Industries.SPACEPORT)
            //market.addSubmarket(Submarkets.SUBMARKET_STORAGE)
            market.addSubmarket("rat_settlement_storage")
            (market.getSubmarket("rat_settlement_storage").plugin as SettlementStoragePlugin).setPlayerPaidToUnlock(true)
            market.isPlanetConditionMarketOnly = false
           // (market.getSubmarket(Submarkets.SUBMARKET_STORAGE).plugin as StoragePlugin).setPlayerPaidToUnlock(false)
            settlementDelegate.market = market
            settlementDelegate.memoryWithoutUpdate.unset("\$tradeMode")

            for (condition in interactionTarget.market.conditions) {
                market.addCondition(condition.id)
                var cond = market.getCondition(condition.id)
                cond.isSurveyed = true
            }

            market.stability.modifyFlat("rat_base", 10f)
            market.accessibilityMod.modifyFlat("rat_base", 0.40f)
            market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat("rat_base", 200f)

            var settlementData = SettlementData(interactionTarget as PlanetAPI, settlementDelegate)
            settlementData.location = selectedSite!!.location
            settlementData.angleFromCenter = selectedSite!!.angleFromCenter
            settlementData.distanceFromCenteer = selectedSite!!.distanceFromCenter
            settlementData.modifiers = selectedSite!!.modifierIDs
            FrontiersUtils.getFrontiersData().activeSettlement = settlementData

            var firstSlot = true
            for (i in 0 until 6) {
                var slot = SettlementFacilitySlot(settlementData)
                if (firstSlot) {
                    firstSlot = false
                    slot.installNewFacility("landing_pad")
                }
                settlementData.facilitySlots.add(slot)
            }

            if (!Global.getSector().hasScript(SettlementManager::class.java)) {
                Global.getSector().removeScriptsOfClass(SettlementManager::class.java)
                Global.getSector().listenerManager.removeListenerOfClass(SettlementManager::class.java)
            }

            var listener = SettlementManager(settlementData)
            Global.getSector().addScript(listener)
            Global.getSector().listenerManager.addListener(listener)
            settlementData.mananger = listener


            textPanel.addPara("You lay claim to the site and order the construction of a new settlement on ${interactionTarget.name}. ")

            textPanel.addPara("A fleet of dropships descends from your fleet towards the surface and establishes a provisional landing pad. " +
                    "Over the following days a small but busy hub sets up around it, forming the fundementals for the new settlement.")

            textPanel.addPara("Due to a settlements limited scope, it is capable of functioning mostly autonomously from locally acquired materials and small-scale trades with other planetary facilities or with traders within nearby space." +
                    "")

            createOption("Descend towards the settlement") {
                dialog.optionPanel.clearOptions()
                //dialog.hideVisualPanel()

                var data = FrontiersUtils.getSettlementData()
                var newPlugin = SettlementInteraction(data)
                newPlugin.dontReAddLargePlanet = true
                newPlugin.previousPlugin = previousPlugin
                dialog.plugin = newPlugin
                dialog.interactionTarget = settlementDelegate
                newPlugin.init(dialog)
            }
        }

        optionPanel.addOptionConfirmation("Establish the settlement",
            "Are you sure you want to establish your settlement on ${interactionTarget.name}?", "Confirm", "Cancel")

        if (selectedSite == null) {
            optionPanel.setEnabled("Establish the settlement", false)
            optionPanel.setTooltip("Establish the settlement", "You need to select a site to establish the settlement.")
        }

        createOption("Back") {
            clearOptions()
            dialog.plugin = previousPlugin
            dialog.plugin.init(dialog)
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }
}