package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomDialogDelegate
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement

class CryochamberInteraction : RATInteractionPlugin() {
    override fun init() {
        textPanel.addPara("Your fleet approaches the cryochamber station.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Explore") {

            clearOptions()

            textPanel.addPara("The crew makes it through the facility. It is filled with active yet abandoned cryopods. " +
                    "Not all inhabitants have any surviving records, but the few that do have myths attached to their name that survived through centuries.\n\n" +
                    "" +
                    "Deeper in to the facility the crew discovers the cryo-revival machine. Immediately there is bad news though, as it only has enough juice left to bring back a single person.\n\n" +
                    "" +
                    "While most residents of this station would likely have prefered to awake in better times, this may aswell be the last chance for them to ever wake up. ")

            createOption("Select person to revive") {
                dialog.showCustomDialog(310f, 600f, object: BaseCustomDialogDelegate() {


                    var selected: PersonAPI? = null

                    override fun createCustomDialog(panel: CustomPanelAPI?, callback: CustomDialogDelegate.CustomDialogCallback?) {

                        var width = panel!!.position.width
                        var height = panel.position.height

                        var people = ArrayList<PersonAPI>()
                        for (i in 0 until 10) {
                            var person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()
                            people.add(person)
                            person.stats.setSkillLevel(Skills.HELMSMANSHIP, 2f)
                        }

                        var element = panel!!.createUIElement(width, height, true)
                        element.position.inTL(0f, 0f)

                        element.addSpacer(5f)
                       // element.addPara("", 0f).position.inTL(0f, 0f)


                        for (person in people) {
                            element.addLunaElement(width - 15, 90f).apply {
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
                                    }
                                    else {
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
                                img.addPara("  Name: ${person.nameString}", 0f)
                                img.addSkillPanel(person, 0f)
                                innerElement.addImageWithText(0f)


                            }

                            element.addTooltipToPrevious(object : BaseTooltipCreator() {


                                override fun getTooltipWidth(tooltipParam: Any?): Float {
                                   return 600f
                                }

                                override fun createTooltip(tooltip: TooltipMakerAPI?,expanded: Boolean, tooltipParam: Any?) {
                                    tooltip!!.addSpacer(5f)

                                    tooltip!!.addPara("Name: ${person.nameString}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Name: ")
                                    tooltip!!.addPara("Occupation: ", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Occupation: ")
                                    tooltip.addSpacer(5f)
                                    tooltip.addPara("Background: ", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Background: ")

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

                    }
                })
            }

            addLeaveOption()
        }

        addLeaveOption()
    }
}