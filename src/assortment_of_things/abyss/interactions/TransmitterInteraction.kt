package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.Misc

class TransmitterInteraction : RATInteractionPlugin() {

    override fun init() {

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the transmitter. Its purpose seems to be to autonomously collect data and send it towards local research stations.")


        if (interactionTarget.hasTag(AbyssTags.TRANSMITTER_UNLOOTED)) {
            textPanel.addPara("The transmitter however seems to have lost capability to send its data back, slowly accumalating the data within it.")
            createOption("Take accumalated data") {
                clearOptions()
                addLeaveOption()
                interactionTarget.removeTag(AbyssTags.TRANSMITTER_UNLOOTED)

                textPanel.addPara("> Take accumalated data", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                textPanel.addPara("You take the data that the transmitter collected for dozens of cycles.", Misc.getTextColor(), Misc.getHighlightColor())

                var tooltip =textPanel.beginTooltip()

                var image = tooltip.beginImageWithText("graphics/icons/cargo/rat_abyss_survey.png", 64f)
                image.addPara("Aquirred Abyssal Survey Data", 0f)
                tooltip.addImageWithText(0f)

                textPanel.addTooltip()

                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_abyss_survey", null), 1f)

            }
        }
        else
        {
            textPanel.addPara("Its data has already been collected.")
        }



        addLeaveOption()

    }




}