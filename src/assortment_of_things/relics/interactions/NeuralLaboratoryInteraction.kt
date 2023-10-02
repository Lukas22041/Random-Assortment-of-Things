package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.relics.items.cores.NeuroCore
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.campaign.Faction
import java.util.Random

class NeuralLaboratoryInteraction : RATInteractionPlugin() {

    var textInterval = IntervalUtil(0.05f, 0.05f)
    var glitchingDuration = 10f
    var glitching = 0f
    var addedOptions = false
    var changedTimer = false

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


                textPanel.addPara("Immediately as the lights turn on, you feel a sensation in your head, a ringing is growing louder, and louder and louder. And suddenly...")

                createOption("Continue") {
                    clearOptions()
                    glitching = glitchingDuration
                    CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                    createOption("...") {}
                }
            }


            addLeaveOption()
        }



        addLeaveOption()
    }

    override fun advance(amount: Float) {

        if (glitching > 0) {

            if (!addedOptions && glitching <= glitchingDuration * 0.6f) {
                clearOptions()
                addedOptions = true
                createOption("What...the fuck is going on?", "It...Hurts", "Make...it...stop") {
                    if (!changedTimer) {
                        changedTimer = true
                        glitching = 2f
                    }
                }
            }

            glitching -= 1 * amount

            if (glitching <= 0) {
                clearOptions()
                CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                textPanel.addPara("And then, it suddenly stopped. The massive amount of data streaming in to your head made you loose grip, having the rest of the crew bewildered as to why you almost passed out on the floor.")

                textPanel.addPara("But just as you attempt to get back up, the sudden sound of a voice makes you fall back down.")

                createOption("Continue") {
                    clearOptions()
                    startConvo()
                }

            }

            if (glitching <= 0) return

            textInterval.advance(amount)
            if (textInterval.intervalElapsed()) {

                var text = ""
                for (i in 0 until 17) {

                    if (i == 8) {
                        text += " "
                    }

                    if (Random().nextFloat() > 0.5f) {
                        text += "1"
                    }
                    else {
                        text += "0"
                    }
                }

                textPanel.addPara(text)
            }
        }

    }

    fun startConvo() {
        textPanel.addPara("\"Connection established succesfully.\"")

        textPanel.addPara("You look around, and it appears that none of your crew has heard the same voice you did.")

        textPanel.addPara("\"Welcome as the new participant of the \"Neuro\"-branch alpha core research. I hope we will have a good time together.\"")

        createOption("Who is this?") {
            clearOptions()
            var core = NeuroCore().createPerson("rat_neuro_core", Factions.NEUTRAL, Random())
            visualPanel.showPersonInfo(core)

            textPanel.addPara("\"Im the Neuro-Core assigned with progressing the field of neurology together with you. I'm capable of communicating directly with my partners brainwaves. " +
                    "While for security reasons my normal functions are limited, this allows for the most efficient exchange of information with my partner.\"")

            createOption("Why am i your partner?") {
                clearOptions()
                textPanel.addPara("\"My protocols will link me to the first available target. This is irreversible, atleast until the contact to my partner is lost.\"")

                createOption("This core may prove useful...") {
                    clearOptions()
                    textPanel.addPara("You decide to take the core out of the terminal and keep it in your fleet, its unique characteristics may become of use in the future.")

                    textPanel.addPara("\"Im glad to make this journey with you.\"")

                    Global.getSector().playerFleet.cargo.addCommodity("rat_neuro_core", 1f)
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