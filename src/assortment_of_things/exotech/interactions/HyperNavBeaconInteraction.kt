package assortment_of_things.exotech.interactions

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.ExoshipIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc

class HyperNavBeaconInteraction : RATInteractionPlugin() {
    override fun init() {

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        if (!interactionTarget.hasTag("used") && !Global.getSector().intelManager.hasIntelOfClass(ExoshipIntel::class.java)) {
            textPanel.addPara("Due to outdated protocols within this type of beacon, your crew managed to intersect a piece of data that describes the location " +
                    "of their exoship, and their planned warp destinations for the following few months. Afterwards, another protocol detects the forcefull entry and shuts down any further use of the beacon.")

            var intel = ExoshipIntel(ExoUtils.getExoData().getExoship(), true)
            Global.getSector().intelManager.addIntel(intel)
            Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)

            interactionTarget.addTag("used")
        }
        else {
            textPanel.addPara("This beacon no longer serves any use to your fleet.")
        }

        textPanel.addPara("If you so choose, you can destroy the beacon, though one of this age is likely not of much importance to the faction. ")

        createOption("Destroy the beacon") {
            Misc.fadeAndExpire(interactionTarget, 1f)
            closeDialog()
        }

        addLeaveOption()

    }
}