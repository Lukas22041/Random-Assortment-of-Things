package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.relics.items.cores.NeuroCore
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.campaign.Faction
import java.util.Random

class NeuralLaboratoryInteraction : RATInteractionPlugin() {


    override fun init() {
        textPanel.addPara("Your fleet approaches the training facility.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Explore") {
            clearOptions()

            textPanel.addPara("The station looks to be mostly defunct, with odd instruments scattered all around the place. " +
                    "Despite the records detailing this as a structure for biological research, not much infrastructure for human cases can be found.")

            textPanel.addPara("You continue your way through the halls, just as you are about to abandon the search for anything of use, the crew stumbles across a room of intrigue.")

            textPanel.addPara("In it, an ai-core terminal with some unknown kind of core slotted within it. The core is locked rigidly to the socket, and can likely only be removed by temporarily restoring power to the station.")

            createOption("Power up the station") {
                clearOptions()

                CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                textPanel.addPara("Immediately as the lights turn on, sound is building up in your head, a ringing is growing louder, and louder and louder. ")

                createOption("Continue") {
                    clearOptions()

                    CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                    textPanel.addPara("The sound becomes ever increasing, an an immense headache is building and you almost faint and fall to the ground. That is until the sound suddenly stops and you hear a voice")

                    createOption("Continue") {

                        CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                        clearOptions()
                        startConvo()
                    }

                }
            }


            addLeaveOption()
        }



        addLeaveOption()
    }


    fun startConvo() {
        textPanel.addPara("\"Connection established succesfully.\"")

        textPanel.addPara("You look around, and it appears that none of your crew has heard the same voice you did.")

        textPanel.addPara("\"Welcome as the new participant of the \"Neuro\"-branch alpha core research. I hope we will have a good time together.\"")

        createOption("Who is this?") {
            clearOptions()
            var core = NeuroCore().createPerson("rat_neuro_core", Factions.NEUTRAL, Random())



            visualPanel.showPersonInfo(core)

            CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

            textPanel.addPara("Suddenly an image flashes in to your mind and the voice continues to speak.")

            textPanel.addPara("\"Im the Neuro-Core assigned with progressing the field of neurology together with you. I'm capable of communicating directly with my partners brainwaves. " +
                    "While for security reasons my normal functions are limited, this allows for the most efficient exchange of information with my partner.")

            textPanel.addPara("My analysis tells me that you have much combat experience, in that case i can assist by creating a neural link with you during battle, allowing you to pilot both yours and my ship without any delay.\"",
                Misc.getTextColor(), Misc.getHighlightColor(),
            "neural link")

            createOption("Why have you selected me?") {
                clearOptions()
                textPanel.addPara("\"My protocols will link me to the first available target. This is irreversible, atleast until the contact to my partner is lost.\"")

                createOption("Make use of this cores unique features") {
                    clearOptions()
                    textPanel.addPara("You decide to take the core out of the terminal and keep it in your fleet, its unique characteristics may become of use in the future.")

                    textPanel.addPara("\"Im glad to be of your assistance.\"")

                    //Global.getSector().playerFleet.cargo.addCommodity("rat_neuro_core", 1f)
                    Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_ai_core_special", "rat_neuro_core"), 1f)

                    AddRemoveCommodity.addCommodityGainText("rat_neuro_core", 1, textPanel)

                    createOption("Leave") {
                        closeDialog()
                        Misc.fadeAndExpire(interactionTarget)
                    }
                }

                createOption("Leave the station and core behind") {
                    clearOptions()
                    textPanel.addPara("You decide that the risk isn't worth it, and make the decision of leaving the station while you still can. The Neuro-Core stays silent, but as you leave you feel a tingling in your head that grows ever smaller as you gain distance.")

                    createOption("Leave") {
                        closeDialog()
                        Misc.fadeAndExpire(interactionTarget)
                    }
                }
            }
        }
    }
}