package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.addOfficerGainText
import org.magiclib.kotlin.getPersonalityName
import org.magiclib.kotlin.isMercenary

class CryochamberInteraction : RATInteractionPlugin() {

    override fun init() {
        textPanel.addPara("Your fleet approaches the cryochamber station.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Approach") {

            clearOptions()

            textPanel.addPara("Approaching a Punitive Cryostation is an agonizingly slow and tense process. You start and stop on your approach, making sure to scan and stop and throw the appropriate level of ECM and spoofed ancient Domain codes to keep the slumbering explosive at its core at bay. Once you’re close enough, your salvors, thoroughly prepped with extra oxygen and thoroughly paid with danger money, slowly emerge from the ship and slowly drift towards the structure.\n\n" + "They take their time, cutting like termites through bulkheads and walls and avoiding the detectors in the rooms and hallways. Eventually your crew manages to crack into the heart of the station. Inside several cryopods lie beneath an ominous sphere of a thermonuclear payload. After a tense couple of moments, the nuclear payload is deactivated. Your crew chief sighs in relief as your salvor team gets to work investigating the pods and their records.")


            createOption("Continue") {
                clearOptions()

                var people = interactionTarget.memoryWithoutUpdate.get("\$rat_prisoners") as List<PersonAPI>

                textPanel.addPara("You are soon notified that 3 individuals have survived, the rest either passed on or too dangerous to even consider unleashing. You can awaken only one of these somnolents - due to a lack of codes specific to this prison you’ll only be able to get one before the systems identify intruders are present and kill the other sleepers.",
                    Misc.getTextColor(), Misc.getHighlightColor(), "3", "only one")

                var tooltip = textPanel.beginTooltip()

                for (person in people) {
                    var img = tooltip.beginImageWithText(person.portraitSprite, 64f)
                    img.addPara("${person.nameString}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${person.nameString}")
                    var backstory = person.memoryWithoutUpdate.get("\$rat_prisoner_backstory") as String
                    img.addPara(backstory, 0f)
                    tooltip.addImageWithText(0f)
                    tooltip.addSpacer(10f)
                }

                textPanel.addTooltip()

                createOption("Select an individual to revive") {
                    dialog.showCustomDialog(320f, 340f, object: BaseCustomDialogDelegate() {

                        var selected: PersonAPI? = null

                        override fun createCustomDialog(panel: CustomPanelAPI?, callback: CustomDialogDelegate.CustomDialogCallback?) {

                            var width = panel!!.position.width
                            var height = panel.position.height

                            var element = panel!!.createUIElement(width, height, true)
                            element.position.inTL(0f, 0f)

                            element.addSpacer(5f)
                            // element.addPara("", 0f).position.inTL(0f, 0f)

                            for (person in people) {
                                element.addLunaElement(width - 10, 100f).apply {
                                    enableTransparency = true
                                    backgroundAlpha = 0.4f

                                    borderAlpha = 0.5f

                                    selectionGroup = "people"

                                    onClick {
                                        playClickSound()
                                        selected = person
                                    }

                                    advance {
                                        if (person == selected) {
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
                                    var img = innerElement.beginImageWithText(person.portraitSprite, 64f)
                                    img.addPara(" Name: ${person.nameString}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Name:")
                                    img.addPara(" Personality: ${person.getPersonalityName()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Personality:")
                                    img.addSkillPanel(person, 0f)
                                    innerElement.addImageWithText(0f)


                                }



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

                            clearOptions()

                            textPanel.addPara("The cryopod works through the de-cryogenesis processes of your target as the other pods begin to rapidly flatline one by one. Groggily they're extracted from the pod and transferred to your fleet, your medical team bringing them up to speed and ensuring they know exactly how thankful they should be for their survival.")
                            textPanel.addOfficerGainText(selected!!)
                            Global.getSector().playerFleet.fleetData.addOfficer(selected)


                            createOption("Leave") {
                                closeDialog()
                                Misc.fadeAndExpire(interactionTarget)
                            }
                            optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);

                        }
                    })

                }

                var maxOfficers = Global.getSector().characterData.person.stats.officerNumber.modifiedValue
                var officersInFleetWithoutMercs = Global.getSector().playerFleet.fleetData.officersCopy.filter { !it.person.isMercenary() }.size

                if (maxOfficers <= officersInFleetWithoutMercs) {
                    optionPanel.setEnabled("Select an individual to revive", false)
                    optionPanel.setTooltip("Select an individual to revive", "Fleet is at maximum number of officers.")
                }

                addLeaveOption()

            }


        }

        addLeaveOption()
    }
}