package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.*
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity

import com.fs.starfarer.api.loading.Description
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList

class AccumalatorStationInteraction : RATInteractionPlugin() {

    override fun init() {

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the abyssal accumalator.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Explore") {
            clearOptions()
            textPanel.addPara("You make your way through the tight corridors of the station, the construction style that has been used for the collection of matter not allowing for much human space.")

            textPanel.addPara("The local collectors and refineries give no sign of activity, but some of its final production seems to still remain in the cargo hold.")

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

                /*drop = DropData()
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
                    AbyssDepth.Shallow -> 1.5f
                    AbyssDepth.Deep -> 2f
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