package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

class TrainingStationInteraction : RATInteractionPlugin() {
    override fun init() {
        textPanel.addPara("Your fleet approaches the training facility.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Explore") {
            clearOptions()

            textPanel.addPara("You enter the station and enter what seems to be some kind of lobby. You walk closer towards one of the hallways, but within the blink of the eye the door closed.")

            textPanel.addPara("\"///ERROR: UNAUTHORISED INDIVIDUAL DETECTED///\"")

            textPanel.addPara("As you take a step away, the door opens once again. When you send an officer to the same spot, the door remains open. " + "It appears that for some unknown reason only your officers can enter the the training chambers. ")

            textPanel.addPara("Your engineers perform an analysis on the facilities power grid, with what is left only one officer has the option of entering a chamber. " + "Performing training in this station may unlock further potential for an officer of your choice.", Misc.getTextColor(), Misc.getHighlightColor(), "" + "one")

            createOption("Pick an officer") {
                createOfficerPicker(true) {

                    Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                    clearOptions()

                    textPanel.addPara("After just a few minutes ${it.nameString} returns from the chamber, however ${it.heOrShe} appears to have grown a beard in the time he spend within.")

                    var currentLevel = it.memoryWithoutUpdate.getInt(MemFlags.OFFICER_MAX_LEVEL)
                    var currentElite = it.memoryWithoutUpdate.getInt(MemFlags.OFFICER_MAX_ELITE_SKILLS)

                    textPanel.addPara("> ${it.nameString} maximum level is increased from $currentLevel to ${currentLevel + 1} ", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())
                    textPanel.addPara("> ${it.nameString} maximum elite skills increased from $currentElite to ${currentElite + 1} ", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())

                    it.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, currentLevel + 10)
                    it.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_ELITE_SKILLS, currentElite + 10)



                    createOption("Leave") {
                        closeDialog()
                        Misc.fadeAndExpire(interactionTarget)
                    }
                    optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);
                }
            }

            addLeaveOption()
        }

        addLeaveOption()
    }
}