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

class AbyssalResearchStationInteraction : RATInteractionPlugin() {

    override fun init() {

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
            textPanel.addPara("As you make your way through the station, you encounter lots of strange devices. Many of them have logs explaining their functionality, however practicaly all of them are defunct.")

            textPanel.addPara("Extended salvage procedures may be able to find something of use within this graveyard of junk.")

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

                drop = DropData()
                drop.chances = 1
                drop.group = "abyss_guaranteed_alt"
                dropRandom.add(drop)

                drop = DropData()
                drop.chances = 2
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

                ArtifactUtils.generateArtifactLoot(salvage, "abyss", 1f, 1, random)

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