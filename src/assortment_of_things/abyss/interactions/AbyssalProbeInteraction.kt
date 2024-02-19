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
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
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
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the abyssal droneship.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Explore") {
            clearOptions()
            textPanel.addPara("The crew navigates through the few maintenance corridors of the droneship. " + "The ship has been dead for a while, but seemingly not aslong as some of the other infrastructure around. Its drone bays appear empty with no sign of where they went.")

            textPanel.addPara("As one of the dozens of droneships in the abyss, its served a variety of purposes, salvaging its remains may proof worthwhile.")

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

        addLeaveOption()

    }
}