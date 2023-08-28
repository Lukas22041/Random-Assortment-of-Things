package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.intel.log.AbyssalLogIntel
import assortment_of_things.abyss.intel.log.AbyssalLogs
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed

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
        }
        else  {
            textPanel.addPara("Its data has already been collected.")
        }

      /*  var pick = AbyssalLogs.getAvailableLog(interactionTarget.getSalvageSeed())

        if (!interactionTarget.hasTag("read_log") && pick != null) {
            textPanel.addPara("It appears that the transmitter has stored the personal log of someone in its database. It may provide worthy reading through it.")
        }*/



        addOptions()
    }

    fun addOptions() {


        if (interactionTarget.hasTag(AbyssTags.TRANSMITTER_UNLOOTED)) {
            createOption("Take accumalated data") {

                interactionTarget.removeTag(AbyssTags.TRANSMITTER_UNLOOTED)

                clearOptions()
               // addOptions()

                textPanel.addPara("You take the data that the transmitter collected for dozens of cycles.", Misc.getTextColor(), Misc.getHighlightColor())

                var tooltip =textPanel.beginTooltip()

                var image = tooltip.beginImageWithText("graphics/icons/cargo/rat_abyss_survey.png", 64f)
                image.addPara("Aquirred Abyssal Survey Data", 0f)
                tooltip.addImageWithText(0f)

                textPanel.addTooltip()

                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_abyss_survey", null), 1f)


                createOption("Leave") {
                    closeDialog()
                    interactionTarget.fadeAndExpire(2f)
                }
            }
        }



       /* var pick = AbyssalLogs.getAvailableLog(interactionTarget.getSalvageSeed())

        if (!interactionTarget.hasTag("read_log") && pick != null) {


            createOption("Read the stored log") {
                interactionTarget.addTag("read_log")

                clearOptions()
                addOptions()

                textPanel.addPara("> Read the stored log",  Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                textPanel.addPara("The fleet downloads the entry stored in the transmitters data structure. A copy of it has been stored in the intel window.")

                var intel = AbyssalLogIntel(pick)

                Global.getSector().intelManager.addIntel(intel)

                var tooltip = textPanel.beginTooltip()

                tooltip.createRect(Misc.getBasePlayerColor(), 2f)

                var text = Global.getSettings().loadText("data/strings/abyss/logs/${pick.id}.txt")

                tooltip.addTitle(pick.name)
                tooltip.addSpacer(2f)

                tooltip.addPara("Author: Undocumented", 0f)
                tooltip.addPara("Date: ${pick.date} | unknown cycle", 0f)

                tooltip.addSpacer(10f)

                tooltip.addPara("$text", 0f)
               // pick.lambda(tooltip)

                textPanel.addTooltip()

            }

        }

*/

        addLeaveOption()
    }




}