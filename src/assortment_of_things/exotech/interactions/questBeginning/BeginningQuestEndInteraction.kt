package assortment_of_things.exotech.interactions.questBeginning

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.event.ExotechEventIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc

class BeginningQuestEndInteraction : RATInteractionPlugin() {

    var data = ExoUtils.getExoData()

    override fun init() {
        if (data.QuestBeginning_StartedFromExoship) {
            startedFromExoship()
        }
        if (data.QuestBeginning_StartedFromRemains) {
            startedFromRemains()
        }
    }

    fun startedFromExoship() {
        textPanel.addPara("You fleet closes in on the Exoship.")

        textPanel.addPara("The fleet attempts to hail the Exoship, but the same pre-recorded message as before plays on repeat.")

        textPanel.addPara("\"This is a pre-recorded message. Do not approach. " +
                "Any attempts at closing in will be seen...\" and on the 3rd repeat, it suddenly cuts out, with another message following in \"Permission granted to dock with a shuttle in Hangar 7-B\"")

        textPanel.addPara("The fleet prepares a shuttle for approach.")

        createOption("Take a shuttle towards the Exoship") {
            clearOptions()

            textPanel.addPara("On your approach, you take notice of the hundreds of exterior hangars, with ships ferrying in resources from all across the local system.")

            textPanel.addPara("As the ship looms larger in front of you, your destination becomes visible. When flying in, it is immediately clear that this dock is hardly ever used, as it appears much shabbier than the others you have spotted.")

            textPanel.addPara("Disembarking from your ship leaves you in a hall with an eerie silence, which is quickly broken by your escort, who takes you to their contact without time to spare.")

            createOption("Continue") {
                clearOptions()

                visualPanel.showPersonInfo(data.amelie)

                textPanel.addPara("You enter the room and your guide leaves you alone with the person of contact. Despite the foggy resolution of the previous meeting's datastream, you can immediately recall the person in front of you.")

                textPanel.addPara("\"Let's keep introductions short. My name's Amelie - seeing that you returned, I think we could say that both of us may have some overlapping interests.\" \n\n" +
                        "She continues \"So here is my proposition, I can provide access to some of my faction's proprietary tech, while you do what I require you to do. \"")

                mainPortion()
            }
        }
    }

    fun startedFromRemains() {
        textPanel.addPara("You fleet closes in on the Exoship.")

        textPanel.addPara("When attempting to hail the Exoship, but only receive an automated message: \"This is a pre-recorded message. Do not approach. Any attempts at closing in will be seen as hostile action and will have an appropriate reaction.\"")

        textPanel.addPara("But suddenly, on the dozenth repeat, it cuts out and says \"Permission granted to dock with a shuttle in Hangar 7-B\". The sudden shift comes at a surprise; docking rights aren't given freely in most cases.")

        textPanel.addPara("Nonetheless, your fleet prepares a shuttle for approach.")

        createOption("Continue") {
            clearOptions()
            textPanel.addPara("On your approach you take notice of the hundreds of exterior hangars, with ships ferrying in resources from all across the local system.")

            textPanel.addPara("As the ship looms larger in front of you, your destination becomes visible. When flying in, it is immediately clear that this dock is hardly ever used, as it appears much shabbier than the others you have spotted.")

            textPanel.addPara("Disembarking from your ship leaves you in a hall with an eerie silence, which is quickly broken by your escort, who takes you to some location without any time to spare.")

            createOption("Continue") {
                clearOptions()

                visualPanel.showPersonInfo(data.amelie)

                textPanel.addPara("You enter the room and your guide leaves. A person sits at the table across you.")

                textPanel.addPara("\"Let's keep introductions short. My name's Amelie; me and my crew, but not others in my faction, know what you have encountered out there. We've been keeping eyes on that specific location for good reasons.\" \n\n" +
                        "She continues \"So here is my proposition - I can provide access to some of my faction's proprietary tech, while you do what I require you to do. \"")

                mainPortion()
            }
        }
    }

    fun mainPortion() {
        createOption("Inquire about what you're needed for") {
            clearOptions()

            textPanel.addPara("She responds with a serious tone \"The faction isn't quite what it used to be - aspects of it, as you could possibly tell from your landing here, are not quite at a standard where it should be. " +
                    "It tries its best to keep appearances up, but internally it is slowly falling apart. A fear to make any attempts at anything keeps us stagnant, or worse at a decline.\"")

            textPanel.addPara("She continues \"At some point in the future, our hoarded resources will run out, and I would rather not be there when that happens, or moreso not without having prepared any measures for the moment.")

            createOption("Continue") {
                clearOptions()

                textPanel.addPara("\"So imagine my surprise when my own fleet managed to receive a signal from another Exoship within the sector. There is no doubt that it wouldn't be in a good state, yet it still presented opportunity.")

                textPanel.addPara("So here is the issue - were we to report this finding, what do you think will happen with it? Would the same people in charge decide to get it running, and then make some real use of it?\"")

                createOption("Continue") {
                    clearOptions()

                    textPanel.addPara("\"So that is where you come into play. My will is to be in charge of the sleeping construct once we manage to reactivate it, but I require an independent contractor to do the work, lest they discover the secret we are hiding.")

                    textPanel.addPara("So, are you in? Is the promise of rare goods, and the potential to co-own a colossal, star-treading station enough to peak your interest?\"")

                    createOption("Accept the offer") {
                        clearOptions()
                        accept()
                    }

                    createOption("Decline the offer") {
                        clearOptions()
                        decline()
                    }


                }
            }
        }
    }

    fun accept() {
        textPanel.addPara("\"Good, that is promising to say the least. Let's not waste any time then. We have two major objectives to follow. " +
                "First, we need to acquire the necessary materials to repair the structure. Secondly, my standing within the faction needs to improve. " +
                "As the commmander of a fleet, I am not in a low place to begin with, but am nonetheless miles away from being a suitable candidate to lead a station of my own.\"")

        textPanel.addPara("To tackle both of these objectives, we need to work with my intelligence officer, Xander. He should be able to locate what we are missing for the repairs, and he will be able to find opportunities to further improve my standing. " +
                "Alternatively, the higher-ups take special consideration of those who are capable of recovering Domain-era relics. If you have any of those that you aren't too attached to, make sure to deliver them to me.\"", Misc.getTextColor(), Misc.getHighlightColor(), "Xander")


        var tooltip = textPanel.beginTooltip()

        tooltip.addPara("New intel available", 0f, Misc.getGrayColor(), Misc.getGrayColor())

        textPanel.addTooltip()

        var intel = ExotechEventIntel()
        Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)

        data.QuestBeginning_Done = true

        addLeaveOption()
    }

    fun decline() {
        textPanel.addPara("\"...that is truly unfortunate; a person like yourself should be able to see the value, but the choice is yours.\"")

        textPanel.addPara("Amelie orders the escort back in, and asks him to guide you back to your shuttle. With your one available contact burned, it is unlikely you will manage to enter ever again.")

        data.lockedOutOfQuest = true

        addLeaveOption()
    }
}