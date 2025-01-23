package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.event.QuestlineFinishedFactor
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.DelayedActionScript
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUtil.LunaCommons
import org.magiclib.achievements.MagicAchievementManager

class ExoshipRecoveryContactInteraction : RATInteractionPlugin() {

    var data = ExoUtils.getExoData()

    override fun init() {
        textPanel.addPara("Out of nowhere, the factions other ship warped in.")

        textPanel.addPara("Reconnecting the station to the navigational network must have send alarms going within their control rooms, and an immediate warp been issued as a response.")

        textPanel.addPara("Without any room to take a breath, a comm-link request comes in.")

        createOption("Accept the incoming comm-request") {
            clearOptions()

            textPanel.addPara("\"This is the mobile colony \"Daybreak\". We order you and the rest of your crew to immediately leave the interiors of the Exoship. " +
                    "The station is our property, and not reserved for the hands of some salvagers fleet that stumbled upon it.\"")

            textPanel.addPara("While the voice comes through, our scopes detect the charge up of dozens of weapons across the opposing ship. Without much time to prepare, the only option left for us is to follow their commands.")

            createOption("Order all crew within the ship to disembark from the station.") {
                clearOptions()

                textPanel.addPara("Your fleet makes haste to leave as fast as possible, " +
                        "and a response to your action comes in \"Good. We better not see you attempt anything funny from now on. It would be a good choice for you to disappear as soon as you can from our scopes.\"")

                textPanel.addPara("Following, a small fleet of ships can be seen transferring crew and cargo between both ships. This process alone takes multiple hours, " +
                        "but then the line of ships moving between the stations appears to come to a stop, and both ships appear to be readying up for a warp.")

                createOption("Continue") {
                    clearOptions()
                    visualPanel.showPersonInfo(data.amelie)

                    textPanel.addPara("As the stations warp drives heat up, another comm request comes in, this time however, from the other Exoship.")

                    textPanel.addPara("\"It appears everything worked out quite well for us, hasn't it\" says Amelie right after the request is accepted. " +
                            "\"Don't worry, i know how to keep my part of a promise. However, as my first instruction as the commander of this new vessel, i have been tasked to return with the main fleet for extended inspection and repairs.\"")

                    QuestlineFinishedFactor(100, dialog)

                    createOption("Continue") {
                        clearOptions()

                        textPanel.addPara("\"I'll transfer over the access codes to a private commlink between your fleet and this station. It will enable you to order a warp to a location of your needs. " +
                                "If you need to talk with us, both myself and Xander will be located permanently within this ship now. You can also dock to store resources or travel somewhere together with the ship. \"",
                        Misc.getTextColor(), Misc.getHighlightColor(), "warp to a location of your needs", "Located permanently within this ship", "dock to store resources or travel somewhere together with the ship")

                        var tooltip = textPanel.beginTooltip()

                        Global.getSector().characterData.addAbility("rat_exoship_management")
                        //Global.getSector().playerFleet.addAbility("rat_exoship_management")

                        var img = tooltip.beginImageWithText(Global.getSettings().getAbilitySpec("rat_exoship_management").iconName, 48f)
                        img.addPara("Acquired the \"Manage Exoship\" Ability. \n" +
                                "Equip it on the hotbar to use.", 0f,
                            Misc.getTextColor(), Misc.getHighlightColor(), "Manage Exoship")
                        tooltip.addImageWithText(0f)

                        if (!Global.getSector().playerFleet.hasAbility("fracture_jump")) {
                            textPanel.addPara("\"Oh, it appears that your fleet has no way to leave this location. I will send over some instructions that should help you move into hyperspace.\"")

                            var img = tooltip.beginImageWithText(Global.getSettings().getAbilitySpec("fracture_jump").iconName, 48f)
                            img.addPara("Acquired the \"Transverse Jump\" Ability. \n" +
                                    "Equip it on the hotbar to use.", 0f,
                                Misc.getTextColor(), Misc.getHighlightColor(), "Transverse Jump")

                            Global.getSector().characterData.addAbility("fracture_jump")
                            //Global.getSector().playerFleet.addAbility("fracture_jump")
                        }

                        Global.getSector().playerFleet.getAbility("fracture_jump").cooldownLeft = 0.5f

                        textPanel.addTooltip()

                        textPanel.addPara("\"We will have to interrupt our talk for now, as i've just received the call to initiate the warp. Looking forward to our next opportunity to work together.\"")

                        data.preventNPCWarps = false
                        data.recoveredExoship = true

                        LunaCommons.set("assortment_of_things", "exotechQuestlineDone", true)

                        createOption("Leave") {


                            var systems = Global.getSector().starSystems.filter { !it.hasTag(Tags.THEME_CORE) && !it.hasTag(
                                Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag(
                                Tags.THEME_HIDDEN)}

                            var filtered = systems.filter { system ->
                                system.planets.none { Global.getSector().economy.marketsCopy.contains(it.market) }
                            }

                            var system = systems.random()

                            ExoUtils.getExoData().getPlayerExoshipPlugin().warpModule.warp(system!!, false, false)
                            ExoUtils.getExoData().getExoshipPlugin().warpModule.warp(system!!, false, false)
                            ExoUtils.getExoData().getExoshipPlugin().npcModule.currentWarp = null

                            MagicAchievementManager.getInstance().completeAchievement("rat_beatExotechQuestline")

                            closeDialog()
                        }
                    }
                }

            }
        }
    }
}