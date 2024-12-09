package assortment_of_things.exotech.interactions.questBeginning

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.ExoshipIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine

class BeginningAtExoshipInteraction : RATInteractionPlugin() {
    override fun init() {

        var exoship = ExoUtils.getExoData().getExoshipPlugin()

        textPanel.addPara("Your fleet approaches the Exoship.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        textPanel.addPara("According to most records, the faction tends to not permit interactions with outsiders. ")


        if (!ExoUtils.getExoData().QuestBeginning_StartedFromExoship) {
            createOption("Try to establish contact") {

                clearOptions()

                textPanel.addPara("You order the comms to get a direct line of communication to the mobile station. Just before the your comms officer does the finishing touches, a message appears on your screens.")

                textPanel.addPara("\"This is a pre-recorded message. Do not Approach. Any attempts at closing in will be seen as hostile action and will have an appropriate reaction.\"")

                textPanel.addPara("It appears we have no option other than to leave, or be fired upon from hundreds of fleets sitting just within.")

                createOption("Leave") {
                    clearOptions()

                    var person = Global.getFactory().createPerson()
                    person.portraitSprite = "graphics/portraits/rat_exo1_holo.png"
                    person.name = FullName("", "", FullName.Gender.FEMALE)
                    person.postId = "rat_exo_unknown"
                    person.setFaction("rat_exotech")

                    visualPanel.showPersonInfo(person, true)

                    CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                    textPanel.addPara("Just as the fleet was ready to depart, a sudden response to our comm-request came in. " +
                            "A figure can be seen, but due to the low quality of the data stream, which likely traces back to an antenna not connected to the main array, it can't be further identified.")

                    textPanel.addPara("\"I've got a proposition. Do not send agree nor disagreement back, as to not reveal the comm-line. Just do or do not as told. " +
                            "I'l send over some coordinates, they should have a point of interest to your liking. If it is, come back and i can provide you with even more opportunities \". ")

                    createOption("Continue") {
                        clearOptions()

                        textPanel.addPara("\"The coordinates should now have been transmitted. On top of that, i provided temporary access to some navigational beacons. " +
                                "Those allow tracing the location of the ship for your return trip. I'm looking forward to hearing back. \"")

                        textPanel.addPara("And as abruptly as it started, the connection is immediately disrupted.")

                        visualPanel.showMapMarker(ExoUtils.getExoData().exoshipRemainsEntity, "Destination: Persean Abyss", Misc.getBasePlayerColor(), false,
                            "graphics/icons/intel/discovered_entity.png", null, setOf())

                        //exoship.npcModule.findNewDestination(1.5f)
                        ExoUtils.getExoData().QuestBeginning_StartedFromExoship = true


                        var tooltip = textPanel.beginTooltip()
                        tooltip.addPara("New intel has been added to the tri-pad.", 0f, Misc.getGrayColor(), Misc.getGrayColor())
                        textPanel.addTooltip()

                        var intel1 = ExoshipRemainsIntel()
                        Global.getSector().intelManager.addIntel(intel1)
                        Global.getSector().intelManager.addIntelToTextPanel(intel1, textPanel)

                        var intel2 = ExoshipIntel(ExoUtils.getExoData().getExoship())
                        Global.getSector().intelManager.addIntel(intel2)
                        Global.getSector().intelManager.addIntelToTextPanel(intel2, textPanel)

                        addLeaveOption()
                    }

                }

            }
        }


        addLeaveOption()


    }

}