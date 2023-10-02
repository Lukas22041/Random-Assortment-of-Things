package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.getPersonalityName

class SkillStationInteraction : RATInteractionPlugin() {



    override fun init() {

        var spec = interactionTarget.customEntitySpec
        var skillSpec = when(spec.id) {
            "rat_bioengineering_station" -> Global.getSettings().getSkillSpec("rat_biomutant")
            "rat_augmentation_station" -> Global.getSettings().getSkillSpec("rat_augmented")
            "rat_neural_laboratory" -> Global.getSettings().getSkillSpec("rat_mass_interface")
            else -> Global.getSettings().getSkillSpec("rat_biomutant")
        }

        textPanel.addPara("Your fleet approaches a ${spec.defaultName.lowercase()}")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Explore") {
            clearOptions()

            textPanel.addPara("Your salvage team works through the facility, shifting through offices oppressed with inert and burnt out personal computers, stalking through laboratories littered with fine but broken scientific equipment, and passing through patient rooms rotted with mold and dust.\n\n" + "Suddenly there's a ripple through the comms, first one salvor, then another chirping loudly in amazement at their discovery. You watch as the telemetry feed is put onto the main screen and...there it is.\n\n" + " A bio-reconstructor. ", Misc.getTextColor(), Misc.getHighlightColor(), "bio-reconstructor")

            createOption("Continue") {
                clearOptions()

                textPanel.addPara("You scarcely believe that you have stumbled upon it - these miraculous devices were nothing more than myth during the time of the Domain, and after its fall these devices were consigned firmly to drunken rumor and magical cure-alls used in movie-holos.\n\n" + "But here one sits, active and functional on your telemetry feed.\n\n" + "You consider your options as the salvors gawk at the device, and your Sensor Officer records every detail they can of the device under the badgering of your head Engineer.",
                    Misc.getTextColor(), Misc.getHighlightColor(), "active and functional")

                textPanel.addPara("This specific device can perform the following change.")

                var tooltip = textPanel.beginTooltip()

                tooltip.setParaFont(Fonts.ORBITRON_12)
                tooltip.addPara("(Hover over the icon for a detailed description)", 0f, Misc.getGrayColor(), Misc.getGrayColor())
                var fake = Global.getFactory().createPerson()
                fake.stats.setSkillLevel(skillSpec.id, 1f)
                tooltip.addSkillPanel(fake, 0f)

                textPanel.addTooltip()

                textPanel.addPara("Any officer can be selected for the procedure. This change is permanent and can only applied to a single person.")

                //addOfficers(skillSpec)

                createOption("Select an officer") {
                    dialog.showCustomDialog(320f, 440f, object: BaseCustomDialogDelegate() {

                        var selected: PersonAPI? = null

                        override fun createCustomDialog(panel: CustomPanelAPI?, callback: CustomDialogDelegate.CustomDialogCallback?) {

                            var width = panel!!.position.width
                            var height = panel.position.height

                            var element = panel!!.createUIElement(width, height, true)
                            element.position.inTL(0f, 0f)

                            element.addSpacer(5f)
                            // element.addPara("", 0f).position.inTL(0f, 0f)

                            for (officer in listOf(Global.getSector().playerPerson) + Global.getSector().playerFleet.fleetData.officersCopy.map { it.person }) {
                                element.addLunaElement(width - 10, 85f).apply {
                                    enableTransparency = true
                                    backgroundAlpha = 0.4f

                                    borderAlpha = 0.5f

                                    selectionGroup = "people"

                                    var unapplicable = (officer == Global.getSector().playerPerson && skillSpec.id == "rat_augmented") || officer.hasTag("rat_dont_allow_for_skills")

                                    if (!unapplicable) {
                                        onClick {
                                            playClickSound()
                                            selected = officer
                                        }
                                    }



                                    advance {
                                        if (officer == selected) {
                                            backgroundAlpha = 0.7f
                                        } else {
                                            backgroundAlpha = 0.4f
                                        }
                                    }

                                    onHoverEnter {
                                        playScrollSound()
                                        borderAlpha = 1f
                                    }
                                    onHoverExit {
                                        borderAlpha = 0.5f
                                    }

                                    innerElement.addSpacer(10f)
                                    var img = innerElement.beginImageWithText(officer.portraitSprite, 64f)
                                    img.addPara("Name: ${officer.nameString}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Name:")
                                    img.addPara("Personality: ${officer.getPersonalityName()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Personality:")
                                    img.addSpacer(5f)
                                    if (unapplicable) img.addNegativePara("Can not be applied to this person.")
                                    innerElement.addImageWithText(0f)
                                }

                                element.addTooltipToPrevious( object : BaseTooltipCreator() {
                                    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                                        tooltip!!.addSkillPanel(officer, 0f)
                                    }
                                }, TooltipMakerAPI.TooltipLocation.RIGHT)



                                element.addSpacer(10f)
                            }




                            panel.addUIElement(element)
                            element.position.inTL(0f, 0f)

                        }

                        override fun hasCancelButton(): Boolean {
                            return true
                        }

                        override fun customDialogConfirm() {
                            if (selected == null) return

                            Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

                            clearOptions()


                            textPanel.addPara("Choosen ${selected!!.nameString}", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

                            textPanel.addPara("Youve choosen ${selected!!.nameString} as the participant of the procedure, and hours later, they awaken as something new.")

                            textPanel.addPara("> ${selected!!.nameString} acquired the ${skillSpec.name} skill", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())

                            selected!!.stats.setSkillLevel(skillSpec.id, 1f)

                            createOption("Leave") {
                                closeDialog()
                                Misc.fadeAndExpire(interactionTarget)
                            }
                            optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);
                        }
                    })

                }
                addLeaveOption()
            }

        }

        addLeaveOption()
    }

}

