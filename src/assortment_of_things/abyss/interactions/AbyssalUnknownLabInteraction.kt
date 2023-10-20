package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.VignettePanelPlugin
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.CombatEngine
import lunalib.lunaExtensions.addLunaElement
import org.lwjgl.input.Keyboard

class AbyssalUnknownLabInteraction : RATInteractionPlugin() {



    override fun init() {

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        var spec = interactionTarget.customEntitySpec
        var skillSpec = Global.getSettings().getSkillSpec("rat_abyssal_bloodstream")

        textPanel.addPara("Your fleet approaches a the unknown lab")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Explore") {
            clearOptions()
            textPanel.addPara("You make your way through the silent halls of the station. With no records to go of the fleet tries to decipher the role this place had. ")

            textPanel.addPara("As this expedition progresses, many medical facilities and laboratories are discovered. " +
                    "Overall even for the state that the station is in after cycles of neglect, all of them have been in complete disarray.")

            createOption("Continue") {
                clearOptions()
                textPanel.addPara("You and your team arrive at the core of the station, a reserve power system seems to be able to provide energy to this part of the station. " +
                        "As the team prepares to download and analyse the stations logs at its terminal, you take a look through the surrounding rooms.")

                textPanel.addPara("A room you stumble upon appears to be another operating room like all the others, but then, the doors close, and the autonomous surgery robot boots up.")

                createOption("Continue") {
                    clearOptions()
                    textPanel.addPara("Its robotic arms grab your hands, making you unable to resist. " +
                            "You scream for assistance, and your team hears the call, but their frantic attempts at opening the doors and shutting down the system seem futile.")

                    textPanel.addPara("The machine proceeds, meanwhile crumbling away from decay, but suceeds in retrieving a small syringe of an unknown substance. Without hesitation, it injects it in to your skin. " +
                            "But just before it finishes, its age does its part and the machine falls appart, the syringe falling to the ground and spilling its leftovers.")

                    textPanel.addPara("The team finaly cracks the doors and shuts off the emergency power system. They immediately run to your location with medpacks in hand.")

                    createOption("Continue") {
                        clearOptions()

                        var plugin = VignettePanelPlugin()
                        var panel = visualPanel.showCustomPanel(Global.getSettings().screenWidth, Global.getSettings().screenHeight, plugin)

                        var element = panel.createUIElement(Global.getSettings().screenWidth, Global.getSettings().screenHeight, false)
                        panel.addUIElement(element)
                        panel.position.inTL(0f, 0f)

                        textPanel.addPara("Just as you are about to tell your crew that you are fine, your vision darkens, it becomes hard to breath, and time appears to come to a still.")

                        textPanel.addPara("As your normal senses weaken, something new comes in to your perception, not something you have ever felt before, a resonance only observed by you.")

                        createOption("Continue") {
                            clearOptions()

                            var text = "This resonance is felt all around, in the station, in the abyssal sea surrounding it, and now in yourself. You try calling it out to it, you feel as if its trying to respond, but you cant understand what it means. "


                            var cargo = Global.getSector().playerFleet.cargo
                            if (cargo.getCommodityQuantity(RATItems.CHRONOS_CORE) != 0f || cargo.getCommodityQuantity(RATItems.COSMOS_CORE) != 0f || cargo.getCommodityQuantity(RATItems.SERAPH_CORE) != 0f) {
                                text += "You also feel a stronger response from the the direction of your fleets. What feels like a soft whisper closing in."
                            }

                            textPanel.addPara(text)

                            createOption("Continue") {
                                clearOptions()

                                var skillSpec = Global.getSettings().getSkillSpec("rat_abyssal_bloodstream")

                                plugin.decreaseVignette = true
                                textPanel.addPara("The response weakens, and suddenly, your senses start flooding back in, time appears to move, and you lose grasp of this new sensation. " +
                                        "Your team still surround you, with your heart having stopped beating for seconds, preparing to relocate you towards the fleets medbay without delay. You feel that you will have lots of explaining to do.")

                                textPanel.addPara("Acquired a new skill.", AbyssUtils.ABYSS_COLOR, AbyssUtils.ABYSS_COLOR)

                                var tooltip = textPanel.beginTooltip()

                                tooltip.setParaFont(Fonts.ORBITRON_12)
                                tooltip.addPara("(Hover over the icon for a detailed description)", 0f, Misc.getGrayColor(), Misc.getGrayColor())
                                var fake = Global.getFactory().createPerson()
                                fake.setFaction("rat_abyssals_deep")
                                fake.stats.setSkillLevel(skillSpec.id, 1f)
                                tooltip.addSkillPanel(fake, 0f)

                                textPanel.addTooltip()

                                Global.getSector().playerPerson.stats.setSkillLevel(skillSpec.id, 2f)

                                createOption("Leave") {
                                    closeDialog()
                                    Misc.fadeAndExpire(interactionTarget)
                                }
                            }
                        }
                    }
                }
            }
        }






        addLeaveOption()
    }

}

