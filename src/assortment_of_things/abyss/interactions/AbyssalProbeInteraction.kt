package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.InteractionDialogImageVisual
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.*
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity

import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import second_in_command.SCUtils
import java.util.*
import kotlin.collections.ArrayList

class AbyssalProbeInteraction : RATInteractionPlugin() {

    override fun init() {

        /*var path = "graphics/illustrations/rat_abyss_wreckage.jpg"
        var sprite = Global.getSettings().getAndLoadSprite(path)
        var interactionImage = InteractionDialogImageVisual(path, sprite.width, sprite.height)
        visualPanel.showImageVisual(interactionImage)*/

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleet's steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the abyssal droneship.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Explore") {
            clearOptions()

            if (interactionTarget.hasTag("rat_abyssal_xo_entity")) {

                textPanel.addPara("The crew navigates through the few maintenance corridors of the droneship. " + "The ship has been dead for a while, but seemingly not as long as some of the other infrastructure around. Its drone bays appear empty with no sign of where they went.")

                textPanel.addPara("Soon though the crew discovers a location of note, a room filled with cryo-pods, something that seems amiss for the type of vessel this is.",
                    Misc.getTextColor(), Misc.getHighlightColor(), "cryo-pods")

                createOption("Continue") {
                    clearOptions()

                    textPanel.addPara("Your crew further inspects this site, it appears all cryo-pods, with the exception of one, are empty. " +
                            "They look through the logs to find that over the cycles, the pod with someone inside has defrosted and refrozen multiple times.",
                        Misc.getTextColor(), Misc.getHighlightColor(), "")

                    textPanel.addPara("You consider your options, but to gather more information, only one seems worth pursuing.")

                    createOption("Recover the person in the cryo-pod") {
                        clearOptions()

                        textPanel.addPara("The crew innitates the procedure for recovering the person inside the cryo-pod and brings them abboard the fleet. It takes a while, but after a few hours they appear to be ready to talk.")

                        textPanel.addPara("But any hopes for leads is immediately erased, as it appears that they have forgotten some things. They still remember who they are, but have no recollection of what transpired the few times they awoke from their cryosleep.",
                            Misc.getTextColor(), Misc.getHighlightColor(), "")

                        createOption("Continue") {
                            clearOptions()

                            var officer = SCUtils.createRandomSCOfficer("rat_abyssal")
                            officer.increaseLevel(2)

                            textPanel.addPara("As a token of gratitude, they offer their skills to use for your fleet, " +
                                    "based on their words, they have a speciality in working with autonomous drones, perhaps being why they were stationed here." +
                                    "", Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "autonomous drones")

                            textPanel.addParagraph("${officer.person.nameString} (level ${officer.getCurrentLevel()}) has joined your fleet",  Misc.getPositiveHighlightColor())

                            SCUtils.showSkillOverview(dialog, officer)

                            SCUtils.getPlayerData().addOfficerToFleet(officer)
                            SCUtils.getPlayerData().setOfficerInEmptySlotIfAvailable(officer)

                            interactionTarget.removeTag("rat_abyssal_xo_entity")

                            addSalvageOption()
                        }
                    }
                }

            } else {
                textPanel.addPara("The crew navigates through the few maintenance corridors of the droneship. " + "The ship has been dead for a while, but seemingly not as long as some of the other infrastructure around. Its drone bays appear empty with no sign of where they went.")

                textPanel.addPara("As one of the dozens of droneships in the abyss, its served a variety of purposes, salvaging its remains may prove worthwhile.")

                addSalvageOption()
            }
        }

        addLeaveOption()

    }

    fun addSalvageOption() {
        createOption("Begin salvage operations") {
            clearOptions()
            var random = Random(interactionTarget.getSalvageSeed())
            var depth = AbyssUtils.getSystemData(interactionTarget.starSystem).depth

            var dropRandom = ArrayList<DropData>()
            var dropValue = ArrayList<DropData>()
            var drop = DropData()



            drop = DropData()
            drop.group = "basic"
            drop.value = 5000
            dropValue.add(drop)



            drop = DropData()
            drop.chances = 2
            drop.group = "rat_abyss_probe"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 2
            drop.group = "rat_abyss_weapons"
            dropRandom.add(drop)



            drop = DropData()
            drop.chances = 1
            drop.group = "rare_tech_low"
            drop.valueMult = 0.25f
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "blueprints"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "any_hullmod_low"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 3
            drop.group = "weapons2"
            dropRandom.add(drop)


            var mult = when(depth) {
                AbyssDepth.Shallow -> 1f
                AbyssDepth.Deep -> 1.5f
                else -> 1f
            }

            var salvage = SalvageEntity.generateSalvage(random, mult, mult, 1f, 1f, dropValue, dropRandom)

            ArtifactUtils.generateArtifactLoot(salvage, "abyss", 0.025f, 1, random)

            visualPanel.showLoot("Loot", salvage, true) {
                closeDialog()

                interactionTarget.fadeAndExpire(3f)
            }

        }

        addLeaveOption()
    }
}