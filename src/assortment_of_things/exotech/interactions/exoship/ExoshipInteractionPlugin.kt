package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global

abstract class ExoshipInteractionPlugin : RATInteractionPlugin() {

    abstract fun populateOptions()


    /*fun populateExecutiveOfficerInteraction() {
        if (Global.getSettings().modManager.isModEnabled("second_in_command") && !ExoUtils.getExoData().claimedExotechOfficer) {

            createOption("Check with a potential hire for your fleet") {
                textPanel.addPara()
            }
        }
    }*/

}