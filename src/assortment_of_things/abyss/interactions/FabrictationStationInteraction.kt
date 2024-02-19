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

class FabrictationStationInteraction : RATInteractionPlugin() {

    override fun init() {

       /* var path = "graphics/illustrations/rat_abyss_wreckage.jpg"
        var sprite = Global.getSettings().getAndLoadSprite(path)
        var interactionImage = InteractionDialogImageVisual(path, sprite.width, sprite.height)
        visualPanel.showImageVisual(interactionImage)*/

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the fabrication station.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Explore") {
            clearOptions()
            textPanel.addPara("You navigate through long corridors of extractors, refineries and processors. " + "This type of station appears to have been specialised in using the exotic matter of the abyss for the production of unique tools.")

            textPanel.addPara("The material present all around the abyss seems to be useful for ship components that wouldn't be possible with other materials but it can not be safely transported elsewhere, meaning all production had to be done localy. " + "Due to this, none of the products seem to have any presence in the present day sector.")

            createOption("Begin salvage operations") {
                clearOptions()
                var random = Random(interactionTarget.getSalvageSeed())
                var depth = AbyssUtils.getSystemData(interactionTarget.starSystem).depth

                var dropRandom = ArrayList<DropData>()
                var dropValue = ArrayList<DropData>()
                var drop = DropData()



                drop = DropData()
                drop.group = "basic"
                drop.value = 10000
                dropValue.add(drop)

               /* drop = DropData()
                drop.chances = 1
                drop.group = "abyss_guaranteed_alt"
                dropRandom.add(drop)*/

                drop = DropData()
                drop.chances = 3
                drop.group = "rat_abyss_fabricator"
                dropRandom.add(drop)

                drop = DropData()
                drop.chances = 3
                drop.group = "rat_abyss_weapons"
                dropRandom.add(drop)



                drop = DropData()
                drop.chances = 1
                drop.group = "rare_tech"
                drop.valueMult = 0.25f
                dropRandom.add(drop)

                drop = DropData()
                drop.chances = 1
                drop.group = "blueprints"
                dropRandom.add(drop)

                drop = DropData()
                drop.chances = 1
                drop.group = "any_hullmod_medium"
                dropRandom.add(drop)

                drop = DropData()
                drop.chances = 4
                drop.group = "weapons2"
                dropRandom.add(drop)


                var mult = when(depth) {
                    AbyssDepth.Shallow -> 1f
                    AbyssDepth.Deep -> 1.5f
                    else -> 1f
                }

                var salvage = SalvageEntity.generateSalvage(random, mult, mult, 1f, 1f, dropValue, dropRandom)

                ArtifactUtils.generateArtifactLoot(salvage, "abyss", 0.05f, 1, random)

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