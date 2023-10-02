package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.util.ArrayList
import kotlin.random.Random
import kotlin.random.asJavaRandom

class SpatialLaboratoryInteraction : RATInteractionPlugin() {

    override fun init() {
        textPanel.addPara("Your fleet approaches the spatial laboratory.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Continue") {
            clearOptions()
            textPanel.addPara("You dock near the station and make your way through the maze of abandoned test chambers, particle accelerators and antimatter production facilities. " + "Eventually you make it to the inventory area of the station, full of distinctive treasure.")

            createOption("Loot") {
                var random = Random(interactionTarget.getSalvageSeed())

                var dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()
                var dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()
                var drop = SalvageEntityGenDataSpec.DropData()

                drop = SalvageEntityGenDataSpec.DropData()
                drop.group = "basic"
                drop.value = 10000
                dropValue.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 1
                drop.group = "any_hullmod_medium"
                dropRandom.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 1
                drop.valueMult = 0.2f
                drop.group = "rare_tech_low"
                dropRandom.add(drop)

                var salvage = SalvageEntity.generateSalvage(random.asJavaRandom(), 2f, 1f, 1f, 1f, dropValue, dropRandom)

                salvage.addSpecial(SpecialItemData("rat_artifact", "rat_transdimensional_accumalator"),1f)

                visualPanel.showLoot("Loot", salvage, true) {
                    closeDialog()

                    interactionTarget.fadeAndExpire(3f)
                }
            }
        }

        addLeaveOption()
    }

}