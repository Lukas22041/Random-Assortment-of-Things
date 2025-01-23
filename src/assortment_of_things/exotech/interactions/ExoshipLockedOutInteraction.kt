package assortment_of_things.exotech.interactions

import assortment_of_things.misc.RATInteractionPlugin

class ExoshipLockedOutInteraction : RATInteractionPlugin() {
    override fun init() {
        textPanel.addPara("You approach the exoship, but attempts at communication come up with nothing but autonomous warning messages.")

        addLeaveOption()
    }
}