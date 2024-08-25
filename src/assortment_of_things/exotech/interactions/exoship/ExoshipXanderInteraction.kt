package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.event.MissionCompletedFactor
import assortment_of_things.exotech.intel.missions.ProjectGilgameshIntel
import assortment_of_things.exotech.intel.missions.RapidResponseIntel
import assortment_of_things.exotech.intel.missions.WarpCatalystMissionIntel
import assortment_of_things.exotech.interactions.questBeginning.ExoshipRemainsIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

class ExoshipXanderInteraction(var original: ExoshipInteractionPlugin) : RATInteractionPlugin() {

    var data = ExoUtils.getExoData()

    override fun init() {
        if (!data.talkedWithXanderOnce) {
            firstTalkToXander()
        }
        else {
            talkToXander()
        }
    }

    //Xander
    fun firstTalkToXander() {
        data.talkedWithXanderOnce = true
        visualPanel.showPersonInfo(data.xander)

        textPanel.addPara("\"Hey - Amelie already informed me. I'm the head of her fleet's intelligence sector. I will relay anything relevant to her goals to you.")

        textPanel.addPara("We've got some jobs that our fleet doesn't have the time to handle, or some that Amelie herself can not be risked to be associated with, and some information the higher ups are not aware of yet. All to say is, we have lots of work left to do.")

        textPanel.addPara("In the meanwhile, I will keep on the look out for the opportunity to acquire a \"Warp Catalyst\". They aren't easy to get a hold of, so it may take a while.\"", Misc.getTextColor(), Misc.getHighlightColor(), "Warp Catalyst")

        populateXanderDialog()

    }

    fun talkToXander() {
        visualPanel.showPersonInfo(data.xander)

        textPanel.addPara("\"What do you need?\"")

        populateXanderDialog()
    }

    fun populateXanderDialog() {
        visualPanel.showPersonInfo(data.xander)
        visualPanel.hideSecondPerson()

        if (data.hasActiveMission && data.finishedCurrentMission) {
            createOption("Talk to Xander about the completed mission") {
                clearOptions()
                data.hasActiveMission = false
                data.finishedCurrentMission = false

                if (data.finishedGilgameshMission) {
                    data.finishedGilgameshMission = false
                    data.finishedGilgameshMissionEntirely = true
                    gilgameshMissionEnd()
                }

                else if (data.finishedRapidMission) {
                    data.finishedRapidMission = false
                    data.finishedRapidMissionEntirely = true
                    rapidMissionEnd()
                }

                else if (data.finishedWarpCatalystMission) {
                    data.finishedWarpCatalystMission = false
                    data.finishedWarpCatalystMissionEntirely = true
                    warpCatalystMissionEnd()
                }
            }
        }


        createOption("Inquire about available missions.") {
            clearOptions()

            populateXanderMissions()
        }

        if (data.hasActiveMission) {
            optionPanel.setEnabled("Inquire about available missions.", false)
            optionPanel.setTooltip("Inquire about available missions.", "You already have an active mission.")
        }

        createOption("Ask him some questions.") {
            clearOptions()
            populateXanderTalk()
        }

        createOption("Back") {
            clearOptions()

            dialog.plugin = original
            original.populateOptions()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }

    // Missions
    fun populateXanderMissions() {



        var anyMissionAvailable = false

        if (!data.finishedGilgameshMissionEntirely) {
            anyMissionAvailable = true
            gilgameshMissionStart()
        }

        if (!data.finishedRapidMission) {
            anyMissionAvailable = true
            rapidMissionStart()
        }

        if (data.reachedLeadershipGoal) {
            if (!data.finishedWarpCatalystMissionEntirely) {
                anyMissionAvailable = true
                warpCatalystMissionStart()
            }
        }

        if (!anyMissionAvailable) {
            textPanel.addPara("Xander has no new missions for you at the moment.")
        }
        else {
            textPanel.addPara("\"I've got some missions available for you. Keep in mind that you will only be able to pursue one of them at a time.\"",
                Misc.getTextColor(), Misc.getHighlightColor(), " one of them at a time.")
        }

        createOption("Back") {
            clearOptions()
            populateXanderDialog()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }

    //Xander Talk
    fun populateXanderTalk() {

        createOption("About Amelie") {
            clearOptions()

            textPanel.addPara("\"She respects my work, and i get paid.\"")

            textPanel.addPara("\"Doesn't need much more for me. She has the energy that expresses her competence, and i can see a clear future in working with her. " +
                    "She got her goals, and i think i can work with those.\"")

            addBackOptionForXanderTalk()
        }

        createOption("Back") {
            clearOptions()
            populateXanderDialog()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }



    //Warp Catalyst
    fun warpCatalystMissionStart() {
        createOption("About a potential lead for a Warp Catalyst") {
            clearOptions()

            textPanel.addPara("\"We've got assigned an important mission directly to our fleet. " +
                    "But we got our hands full in other places already, so we require your assistance. However, there is good reason for you to assist.")

            textPanel.addPara("A defector has withdrawn from the faction, but hasn't done so empty handed. " +
                    "His entire Armada followed suit, escaping with a fleet's worth of cargo. Here comes the important aspect. " +
                    "Part of the stolen cargo was a \"Warp Catalyst\". It may be the best opportunity we will ever get to acquire one.\"",
                Misc.getTextColor(), Misc.getHighlightColor(), "Warp Catalyst")

            createOption("Ask for the specifics of the assignment") {
                clearOptions()

                textPanel.addPara("\"The order is to completely destroy the defectors fleet. To our dismay, the higher ups also require the recovery and return of the Warp Catalyst. " +
                        "While you are performing the job, i will discuss with Amelie on how to proceed with that issue.",
                Misc.getTextColor(), Misc.getHighlightColor(), "Warp Catalyst")

                textPanel.addPara("Are you prepared to do this job?\"")

                createOption("Accept the mission") {
                    clearOptions()


                    data.hasActiveMission = true

                    var intel = WarpCatalystMissionIntel()
                    Global.getSector().intelManager.addIntel(intel)

                    textPanel.addPara("\"Good to hear. We asume the fleet to be in the ${intel.hideout.starSystem.nameWithNoType} system. " +
                            "However we have no information on where, so you you will have to search for them yourself. Good Luck \"",
                    Misc.getTextColor(), Misc.getHighlightColor(), "${intel.hideout.starSystem.nameWithNoType}")

                    visualPanel.showMapMarker(intel.hideout.starSystem.center, "Destination: ${intel.hideout.starSystem.name}", Misc.getBasePlayerColor(), false,
                        "graphics/icons/intel/discovered_entity.png", null, setOf())

                    Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)

                    intel.hideout.customDescriptionId = "rat_exo_hideout_active"

                    addBackOptionForXanderDialog()
                }

                addBackOptionForXanderMissions()
            }

        }
        dialog.setOptionColor("About a potential lead for a Warp Catalyst", Misc.getHighlightColor())
    }

    fun warpCatalystMissionEnd() {
        clearOptions()

        visualPanel.showSecondPerson(data.amelie)

        var intel = Global.getSector().intelManager.getFirstIntel(WarpCatalystMissionIntel::class.java) as WarpCatalystMissionIntel
        intel.endImmediately()

        textPanel.addPara("Before you start talking, Amelie enters the room, anticipating your report on the situation. " +
                "Both of them look at you with a light but smirky smile when you reveal what you have returned with.")

        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

        Misc.adjustRep(data.xander, 0.1f, textPanel)
        Misc.adjustRep(data.amelie, 0.1f, textPanel)

        textPanel.addPara("Amelie says \"This is good news. With this we should be able to get our plan in motion\"")

        createOption("Continue") {
            clearOptions()

            textPanel.addPara("She continues \"Xander and I already discussed on how we should continue; the faction would like the catalyst returned, but we would rather keep it for our own plan.\" " +
                    "Xander intervenes \"We have decided to report the catalyst as unrecoverable, found scattered into thousand pieces amidst the remains of the defector's fleet. The higher-ups won't like this result, but it's the best we've got.\"")

            textPanel.addPara("Amelie continues \"So, make your way towards the damaged Exoship, and use the catalyst to boot up the repair protocols. " +
                    "We analysed your data of its wreck, and the autonomous drones within the ship should do the work. Meanwhile both of us will stay here to prepare for what comes after\".")

            data.readyToRepairExoship = true

            MissionCompletedFactor(50, dialog, "Exo-Defector")

            Global.getSector().intelManager.addIntelToTextPanel(Global.getSector().intelManager.getFirstIntel(ExoshipRemainsIntel::class.java), textPanel)

            addBackOptionForXanderDialog()
        }



    }




    //Gilgamesh
    fun gilgameshMissionStart() {

        createOption("About a stolen prototype ship") {
            clearOptions()

            textPanel.addPara("\"One of the faction's fleets got ambushed while performing trial deployments of a new destroyer we've got in the works. We identified the perpetrators as pirates, and discovered someone providing them with information from within, but I will spare you the details.")

            textPanel.addPara("Point is, there is a bounty on taking out the fleet and preventing the destroyer from being transferred over to a buyer. We have been instructed that the destruction of the target is acceptable if the situation calls for it. " +
                    "Perhaps the destroyer will simply disappear after you have done your work, if you catch my drift. \"")

            createOption("Accept this mission") {
                clearOptions()

                data.hasActiveMission = true

                var intel = ProjectGilgameshIntel()
                Global.getSector().intelManager.addIntel(intel)

                visualPanel.showMapMarker(intel.fleet.starSystem.center, "Destination: ${intel.fleet.starSystem.name}", Misc.getBasePlayerColor(), false,
                    "graphics/icons/intel/discovered_entity.png", null, setOf())
                //visualPanel.showFleetInfo("Target Fleet", intel.fleet, null, null)

                var tooltip = textPanel.beginTooltip()

                var shipsInList = 7
                var shipListCount = 0
                var previewList = ArrayList<FleetMemberAPI>()
                for (member in intel.fleet.fleetData.membersListCopy) {
                    if (shipListCount >= shipsInList) break
                    shipListCount += 1

                    previewList.add(member)
                }

                textPanel.addPara("\"Good. Remember, everything goes as long as the pirates lose their access to the Gilgamesh-class. " +
                        "We have some information about their fleet, but they likely have many more ships than those listed.\"",
                    Misc.getTextColor(), Misc.getHighlightColor(), "Gilgamesh-class")

                tooltip.addShipList(7, 1, 64f, Misc.getBasePlayerColor(), previewList, 0f)

                textPanel.addTooltip()

                var locDescription = intel.getOrbitLocationDescription()
                textPanel.addPara("\"The target appears to be in the ${intel.fleet.starSystem.nameWithNoType} system. $locDescription. We've transferred the necessary intel to complete the mission.\"",
                    Misc.getTextColor(), Misc.getHighlightColor(), "${intel.fleet.starSystem.nameWithNoType}")

                Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)


                addBackOptionForXanderDialog()
            }

            addBackOptionForXanderMissions()
        }


    }

    fun gilgameshMissionEnd() {
        textPanel.addPara("\"Good work. Don't worry about bringing back the prototypes remains. If you have salvaged it for yourself, keep it. " +
                "Take it as a reward for helping us. We can fake the records of it's guaranteed destruction. ")

        textPanel.addPara("I have also reported the traitor that leaked the original fleet's flight plans, im sure the higher ups will be quite pleased.\"")

        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

        Misc.adjustRep(data.xander, 0.1f, textPanel)
        Misc.adjustRep(data.amelie, 0.07f, textPanel)

        MissionCompletedFactor(200, dialog, "Project Gilgamesh")

        var intel = Global.getSector().intelManager.getFirstIntel(ProjectGilgameshIntel::class.java) as ProjectGilgameshIntel
        intel.endImmediately()

        addBackOptionForXanderDialog()
    }







    //Rapid Response
    fun rapidMissionStart() {
        createOption("About a rogue autonomous fleet") {
            clearOptions()

            textPanel.addPara("\"An autonomous fleet that the faction deployed to experiment with the concept of an in-system rapid response unit went rogue, and we've been tasked with eliminating it.")

            textPanel.addPara("The fleet uses a different doctrine than usual within the faction, it has no phase ships within it, and instead fields a bunch of Hypatia-class destroyers. " +
                    "Most importantly, a new prototype of an autonomous fighter has been integrated in this fleet, and its capabilities should not be underestimated.\"")

            createOption("Accept this mission") {
                clearOptions()

                data.hasActiveMission = true

                var intel = RapidResponseIntel()
                Global.getSector().intelManager.addIntel(intel)

                visualPanel.showMapMarker(intel.fleet.starSystem.center, "Destination: ${intel.fleet.starSystem.name}", Misc.getBasePlayerColor(), false,
                    "graphics/icons/intel/discovered_entity.png", null, setOf())
                //visualPanel.showFleetInfo("Target Fleet", intel.fleet, null, null)


                var shipsInList = 999
                var shipListCount = 0
                var previewList = ArrayList<FleetMemberAPI>()
                for (member in intel.fleet.fleetData.membersListCopy) {
                    if (shipListCount >= shipsInList) break
                    shipListCount += 1

                    previewList.add(member)
                }

                textPanel.addPara("\"Alright. Make sure to get rid of the entire fleet. Having any ships remain may pose issues to others, or worse fall in to the hands of those they should not. " +
                        "I went and listed the composition of the fleet for you below. \"",
                    Misc.getTextColor(), Misc.getHighlightColor(), "")

                var tooltip = textPanel.beginTooltip()

                tooltip.addShipList(7, 2, 64f, Misc.getBasePlayerColor(), previewList, 0f)

                textPanel.addTooltip()

                var locDescription = intel.getOrbitLocationDescription()
                textPanel.addPara("\"The target appears to be in the ${intel.fleet.starSystem.nameWithNoType} system. $locDescription. We've transferred the necessary intel to complete the mission.\"",
                    Misc.getTextColor(), Misc.getHighlightColor(), "${intel.fleet.starSystem.nameWithNoType}")

                Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)

                addBackOptionForXanderDialog()
            }
        }
    }

    fun rapidMissionEnd() {
        textPanel.addPara("\"Good work. Now theres no longer any reason to worry about that mess. I'l spare you the details of how this situation came to be.\n\n" +
                "In case you recovered anything of note, keep it for your own fleet, it should be of more use to yourself than those that lost the equipment in the first place.\"")

        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

        Misc.adjustRep(data.xander, 0.1f, textPanel)
        Misc.adjustRep(data.amelie, 0.07f, textPanel)

        MissionCompletedFactor(200, dialog, "Rapid Response")

        var intel = Global.getSector().intelManager.getFirstIntel(RapidResponseIntel::class.java) as RapidResponseIntel
        intel.endImmediately()

        addBackOptionForXanderDialog()
    }



    fun addBackOptionForXanderMissions() {
        createOption("Back") {
            clearOptions()
            populateXanderMissions()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }

    fun addBackOptionForXanderTalk() {
        createOption("Back") {
            clearOptions()
            populateXanderTalk()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }

    fun addBackOptionForXanderDialog() {
        createOption("Back") {
            clearOptions()
            populateXanderDialog()
        }
        optionPanel.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)
    }



}
