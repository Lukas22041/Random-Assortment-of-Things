package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.intel.AbyssCrateIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.Misc

class SingularityCrateInteration : RATInteractionPlugin() {
    override fun init() {

        var fleet = interactionTarget.memoryWithoutUpdate.get("\$defenderFleet")
        if (fleet != null ) {
            textPanel.addPara("The fleet moves in to investigate the crate of cargo. However as it closes in multiple signatures appear on the radar systems.")
            textPanel.addPara("It appears that an autonomous fleet is guarding the shipment. It has to be defeated to be able to access its contents.")

            triggerDefenders()
        }
        else {
            closeDialog()
            Misc.fadeAndExpire(interactionTarget)
        }
    }

    override fun defeatedDefenders() {
        clearOptions()

        textPanel.addPara("After defeating the defending fleet, ships are deployed to investigate the contents of the shipment. " +
                "They end up returning with just a single item in their possesion.")

        var tooltip = textPanel.beginTooltip()

        tooltip.addSpacer(10f)

        var img = tooltip.beginImageWithText("graphics/icons/cargo/rat_singularity_device.png", 48f)
        img.addPara("> Acquired Unidentified Device", 0f,
            Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor(), )

        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        textPanel.addTooltip()

        textPanel.addPara("The crew also retrieves a log describing the contents of this shipment.")

        createOption("Read the log") {
            clearOptions()

            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_destabilizer", null), 1f)
            textPanel.addPara("> Read the log", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

            var log = textPanel.beginTooltip()

            log.addSpacer(5f)
            log.addPara("Date: 11.3", 0f)
            log.addPara("Shipment: Gravitational Destabilizer", 0f)
            log.addPara("Destination : Abyss Expedition Fleet", 0f)
            log.addSpacer(10f)
            log.addPara("This shipment is is being send towards the current main expedition force in the abyss. It contains an additional Gravitational Destabiliser. \n\n" +
                    "The additional destabilizer is being send to act as a backup in case of failure in the curent device, as without one there is no method available to enter or exit the abyss with." +
                    "It is adviced to only use the device in hyperspace with a safe distance from any nearby fleet.", 0f)
            log.addSpacer(5f)

            textPanel.addTooltip()

            textPanel.addPara("")

            createOption("Leave") {
                interactionTarget.memoryWithoutUpdate.set("\$defenderFleet", null)
                closeDialog()
                Misc.fadeAndExpire(interactionTarget)

                var intel = Global.getSector().intelManager.getFirstIntel(AbyssCrateIntel::class.java)
                if (intel != null) Global.getSector().intelManager.removeIntel(intel)
            }

        }

    }

}