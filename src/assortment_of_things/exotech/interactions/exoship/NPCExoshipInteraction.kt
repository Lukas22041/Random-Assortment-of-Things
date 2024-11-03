package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.event.DonatedItemFactor
import assortment_of_things.exotech.intel.event.MissionCompletedFactor
import assortment_of_things.exotech.intel.missions.ProjectGilgameshIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoPickerListener
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.impl.items.*
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

class NPCExoshipInteraction : ExoshipInteractionPlugin() {

    var data = ExoUtils.getExoData()

    override fun init() {

        textPanel.addPara("You approach the exoship.")

        if (data.recoveredExoship) {
            textPanel.addPara("With Amelie and Xander no longer on this ship, you have nothing left to do here.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet sends in your shuttle together with some inconspicuous cargo ships, to create a pretense of trade. You once again land at this shabby hangar, and make your way towards the quarters of Amelies fleet.")

        populateOptions()

    }

    override fun populateOptions() {
        clearOptions()
        visualPanel.showImageVisual(interactionTarget.customInteractionDialogImageVisual)


        var amelieDelegate = ExoshipAmelieInteraction(this)
        var xanderDelegate = ExoshipXanderInteraction(this)


        createOption("Talk to Amelie") {
            clearOptions()

            dialog.plugin = amelieDelegate
            amelieDelegate.init(dialog)
        }

        createOption("Talk to Xander") {
            clearOptions()

            clearOptions()
            dialog.plugin = xanderDelegate
            xanderDelegate.init(dialog)
        }

        //populateExecutiveOfficerInteraction()

        createOption("Trade") {
            visualPanel.showCore(CoreUITabId.CARGO, interactionTarget) { }
        }
        optionPanel.setShortcut("Trade", Keyboard.KEY_I, false, false, false, false)

        createOption("Manage Fleet") {
            visualPanel.showCore(CoreUITabId.FLEET, interactionTarget) { }
        }
        optionPanel.setShortcut("Manage Fleet", Keyboard.KEY_F, false, false, false, false)

        createOption("Refit Ships") {
            visualPanel.showCore(CoreUITabId.REFIT, interactionTarget) { }
        }
        optionPanel.setShortcut("Refit Ships", Keyboard.KEY_R, false, false, false, false)

        addLeaveOption()
    }













}