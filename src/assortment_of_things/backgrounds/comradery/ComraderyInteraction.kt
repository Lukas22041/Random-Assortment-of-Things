package assortment_of_things.backgrounds.comradery

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaExtensions.addLunaTextfield
import lunalib.lunaUI.elements.LunaSpriteElement

class ComraderyInteraction(var factionID: String) : RATInteractionPlugin() {

    var scrollerPosition = 0f
    var person = Global.getSector().getFaction(factionID).createRandomPerson()
    var skills = listOf("rat_maintaining_momentum", "rat_perfect_planning", "rat_auto_engineer", "rat_maverick")
    var personalities = listOf("timid", "cautious", "steady", "aggressive", "reckless")

    var selectedSkill = ""
    var selectedPersonality = "steady"

    override fun init() {

        person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, 8f)
        person.memoryWithoutUpdate.set(MemFlags.OFFICER_SKILL_PICKS_PER_LEVEL, 6)
        person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_ELITE_SKILLS, 3)


        textPanel.addPara("Select the details of your partner officer.")

        textPanel.addPara("Your partner has a maximum level of 8, can choose from one of 6 skills on level up, and can have a total of 3 elite skills. " +
                "Their portrait, name and personality can be changed.",
                Misc.getTextColor(), Misc.getHighlightColor(),
                "8", "6", "3")

        textPanel.addPara("You can also select from a list of unique skills that are otherwise hard or impossible to acquire.")

        refreshScreen()

        if (Global.getSettings().modManager.isModEnabled("chatter")) {
            createOption("Select chatter character") {
                dialog.showCustomDialog(400f, 600f, CombatChatterSelector(person))
            }
            optionPanel.setTooltip("Select chatter character", "Allows changing the character that the \"Combat Chatter\" mod uses to decide their personality. Characters with a chance of 0 require the \"comradery\" categoryTag to be displayed.")
        }

        createOption("Confirm") {
            if (selectedSkill != "") {
                person.stats.setSkillLevel(selectedSkill, 1f)
            }
            person.setPersonality(selectedPersonality)

            Global.getSector().memoryWithoutUpdate.set("\$rat_comerade_start_person", person)
            Global.getSector().playerFleet.fleetData.addOfficer(person)
            closeDialog()
        }
        optionPanel.addOptionConfirmation("Confirm", "Are you sure you want to finish the creation of this officer? It can not be changed again at a later point.", "Confirm", "Back")
    }

    fun refreshScreen() {
        var width = 500f
        var height = 500f
        var panel = visualPanel.showCustomPanel(width, height, null)
        var element = panel.createUIElement(width, height, true)


        element.addLunaElement(width - 10, 80f).apply {
            renderBackground = false
            renderBorder = false

            var sprite = innerElement.addLunaSpriteElement(person.portraitSprite, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, 80f, 80f)
            sprite.renderBorder = false
            sprite.renderBackground = false
            sprite.enableTransparency = true

            var randomize = innerElement.addLunaElement(145f, 35f)
            randomize.enableTransparency = true
            randomize.addText("Select Portrait", baseColor = Misc.getBasePlayerColor())
            randomize.centerText()
            randomize.elementPanel.position.rightOfTop(sprite.elementPanel, 10f)
            randomize.onClick {
                playClickSound()
              /*  var randomPerson = Global.getSector().getFaction(factionID).createRandomPerson()
                scrollerPosition = element.externalScroller.yOffset
                person.portraitSprite = randomPerson.portraitSprite
                person.gender = randomPerson.gender*/

                var portraits = ArrayList<String>()
                portraits += Global.getSector().playerFaction.factionSpec.getAllPortraits(FullName.Gender.MALE)
                portraits += Global.getSector().playerFaction.factionSpec.getAllPortraits(FullName.Gender.FEMALE)
                portraits += Global.getSector().getFaction(factionID).factionSpec.getAllPortraits(FullName.Gender.MALE)
                portraits += Global.getSector().getFaction(factionID).factionSpec.getAllPortraits(FullName.Gender.FEMALE)
                portraits = portraits.distinct() as ArrayList<String>

                addPortraitPicker(person.portraitSprite, portraits ) {
                    person.portraitSprite = it
                    refreshScreen()
                }

            }

            randomize.onHoverEnter {
                randomize.borderColor = Misc.getDarkPlayerColor().brighter()
            }
            randomize.onHoverExit {
                randomize.borderColor = Misc.getDarkPlayerColor()
            }

            var genderButton = innerElement.addLunaElement(145f, 35f)
            genderButton.enableTransparency = true
            genderButton.addText("${person.gender.name.lowercase().capitalize()}", baseColor = Misc.getBasePlayerColor())
            genderButton.centerText()
            genderButton.elementPanel.position.rightOfMid(randomize.elementPanel, 10f)
            genderButton.onClick {
                genderButton.playClickSound()

                if (person.gender == FullName.Gender.MALE) {
                    person.gender = FullName.Gender.FEMALE
                }
                else {
                    person.gender = FullName.Gender.MALE
                }
                genderButton.changeText("${person.gender.name.lowercase().capitalize()}")
                genderButton.centerText()
            }

            genderButton.onHoverEnter {
                genderButton.borderColor = Misc.getDarkPlayerColor().brighter()
            }
            genderButton.onHoverExit {
                genderButton.borderColor = Misc.getDarkPlayerColor()
            }

            var firstName = innerElement.addLunaTextfield("${person.name.first}",false,145f, 35f)
            firstName.enableTransparency = true
            firstName.centerText()
            firstName.elementPanel.position.belowLeft(randomize.elementPanel, 10f)
            firstName.advance {
                person.name = FullName(firstName.getText(), person.name.last, person.gender)
            }

            var lastName = innerElement.addLunaTextfield("${person.name.last}",false,145f, 35f)
            lastName.enableTransparency = true
            lastName.centerText()
            lastName.elementPanel.position.rightOfMid(firstName.elementPanel, 10f)
            lastName.advance {
                person.name = FullName((person.name.first), lastName.getText(), person.gender)
            }

        }

        element.addSpacer(10f)

        var personalitiesElement = element.addLunaElement(700f, 40f).apply {
            renderBackground = false
            renderBorder = false
        }
        var lastPersonalityElement: UIPanelAPI? = null
        for (personality in personalities) {
            var luna = personalitiesElement.innerElement.addLunaElement((650f / personalities.size) - (10 * personalities.size), 40f).apply {
                enableTransparency = true
                backgroundAlpha = 0.5f
                borderAlpha = 0.5f

                if (selectedPersonality == personality) {
                    backgroundAlpha = 1f
                }

                onHoverEnter {
                    playScrollSound()
                    borderAlpha = 1f
                }
                onHoverExit {
                    borderAlpha = 0.5f
                }
            }

            luna.addText(personality.capitalize(), baseColor = Misc.getBasePlayerColor())
            luna.centerText()

            if (lastPersonalityElement != null) {
                luna.elementPanel.position.rightOfMid(lastPersonalityElement, 10f)
            }

            luna.onClick {
                luna.playClickSound()
                selectedPersonality = personality
                refreshScreen()
            }

            lastPersonalityElement = luna.elementPanel
        }


        element.addSpacer(10f)
        element.addSectionHeading("Unique Skill", Alignment.MID, 0f)
        element.addSpacer(10f)

        for (skill in skills) {

            var luna = element.addLunaElement(400f, 60f).apply {
                renderBorder = true
                renderBackground = true
                enableTransparency = true
                borderAlpha = 0.5f
                backgroundAlpha = 0.5f

                if (selectedSkill == skill) {
                    backgroundAlpha = 1f
                }
                onHoverEnter {
                    playScrollSound()
                    borderAlpha = 1f
                }
                onHoverExit {
                    borderAlpha = 0.5f
                }
            }

            luna.onClick {
                luna.playClickSound()
                selectedSkill = skill
                refreshScreen()
            }



            var person = Global.getFactory().createPerson()
            person.stats.setSkillLevel(skill, 1f)
            luna.innerElement.addSkillPanel(person, 0f)

            element.addSpacer(10f)
        }

        panel.addUIElement(element)
        element.externalScroller.yOffset = scrollerPosition
    }
}