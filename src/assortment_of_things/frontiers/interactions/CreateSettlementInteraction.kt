package assortment_of_things.frontiers.interactions

import assortment_of_things.frontiers.data.SiteData
import assortment_of_things.frontiers.ui.SiteSelectionPickerElement
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
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


        populateOptions()
    }

    fun populateOptions() {
        clearOptions()
        createOption("Select a site") {
            dialog.showCustomDialog(600f, 600f, object: BaseCustomDialogDelegate() {
                override fun createCustomDialog(panel: CustomPanelAPI?, callback: CustomDialogDelegate.CustomDialogCallback?) {
                    var element = panel!!.createUIElement(600f, 600f, false)
                    panel.addUIElement(element)
                    SiteSelectionPickerElement(interactionTarget as PlanetAPI, Global.getSettings().getSprite((interactionTarget as PlanetAPI).spec.texture), element, 600f, 600f)
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

        if (selectedSite != null) {
            textPanel.addPara("Site Description")
        }

        createOption("Establish the settlement") {

        }
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