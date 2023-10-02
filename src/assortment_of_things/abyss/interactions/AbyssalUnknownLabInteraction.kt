package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.Misc
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

            textPanel.addPara("You make your way through the silent halls of this station. It appears to be filled with medical facilities, but there are is no indication of what has transpired here. " + "As you delve deeper, you discover the remaining logs of the station. ")

            textPanel.addPara("The stations purpose seems to have been the experimentation of the effects of abyssal matter on biological subjects. ")

            createOption("Continue") {
                clearOptions()

                textPanel.addPara("You skim through the logs to find more details and stumble upon records of an experimental drug that has been developed and successfuly tested on patients. " + "The logistical records display that a single sample of that drug remains within the stations cargo holds.")

                var tooltip = textPanel.beginTooltip()

                tooltip.setParaFont(Fonts.ORBITRON_12)
                tooltip.addPara("(Hover over the icon for a detailed description)", 0f, Misc.getGrayColor(), Misc.getGrayColor())
                var fake = Global.getFactory().createPerson()
                fake.stats.setSkillLevel(skillSpec.id, 1f)
                tooltip.addSkillPanel(fake, 0f)

                textPanel.addTooltip()

                createOption("Use the drug on an officer") {
                    createOfficerPicker(false) {selected ->

                        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

                        clearOptions()


                        textPanel.addPara("Choosen ${selected!!.nameString}", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

                        textPanel.addPara("${selected!!.nameString} has been choosen as the \"lucky\" participant to receive the drug. The drug is carefully injected in to their bloodstream, and just as it started, the procedure was already done.")

                        textPanel.addPara("> ${selected!!.nameString} acquired the ${skillSpec.name} skill", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())

                        selected!!.stats.setSkillLevel(skillSpec.id, 1f)

                        createOption("Leave") {
                            closeDialog()
                            Misc.fadeAndExpire(interactionTarget)
                        }
                        optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);
                    }

                }
                addLeaveOption()
            }

        }

        addLeaveOption()
    }

}

