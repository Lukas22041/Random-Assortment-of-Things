package assortment_of_things.abyss.interactions

import assortment_of_things.misc.RATInteractionPlugin

class RiftStationInteraction : RATInteractionPlugin() {
    override fun init() {


        textPanel.addPara("The fleet approaches the station at the center of the rift. It appears that the station houses some kind of devide to keep this rift active, its purpose however is yet unknown. There doesnt appear to be any hostile activity around it.")

        var fleet = interactionTarget.memoryWithoutUpdate.get("\$defenderFleet")
        if (fleet != null ) {
            textPanel.addPara("However as we attempt to close in, as if out of nowhere a fleet emerges in the vicinity of the station. " +
                    "It contains signatures that haven't been observed anywhere else within the abyss, it's adviced to proceed with extreme caution.")

            triggerDefenders()
        }
        else {
          /*  closeDialog()
            Misc.fadeAndExpire(interactionTarget)*/

            textPanel.addPara("With nothing in it's way, the fleet docks at the station. Unlike most other entities within the abyss so far, this one seems to be of more recent origin.")

            addOptions()
        }
    }


    fun addOptions() {

        var rewardShip = interactionTarget.memoryWithoutUpdate.get("\$rewardShip")
        if (rewardShip != null) {

            textPanel.addPara("One of the defending ships that now hover disabled around the station appears to be in good enough conditions to be recovered by our repair crew.")
            createOption("Recover the unique hull") {

            }
        }

        var looted = interactionTarget.memoryWithoutUpdate.get("\$rat_looted")
        if (looted == null) {
            textPanel.addPara("The station appears to be full with recoverable items, many appearing to be of the less common kind.")
            createOption("Loot the station") {

            }
        }


        addLeaveOption()

    }

    override fun defeatedDefenders() {
        clearOptions()
        interactionTarget.memoryWithoutUpdate.set("\$defenderFleet", null)

        textPanel.addPara("With the defending fleet disabled, the fleet proceeds to dock at the station. Unlike most other entities within the abyss so far, this one seems to be of more recent origin.")




        addOptions()
    }
}