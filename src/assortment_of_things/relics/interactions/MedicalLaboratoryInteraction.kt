package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.util.ArrayList
import kotlin.random.Random
import kotlin.random.asJavaRandom

class MedicalLaboratoryInteraction : RATInteractionPlugin() {

    override fun init() {
        textPanel.addPara("Your fleet approaches the medical laboratory.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Continue") {
            clearOptions()
            textPanel.addPara("You dock near the station and make your way through the corridors of medical machinery, once set to produce medical equipment at record pace, but are now decayed far beyond use.")

            textPanel.addPara("After surveying the station for a while, it appears that there is a good amount of drug supply that hasnt left the station, including some experimental ones " + "that dont appear to have ever made it to the market.")

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

                salvage.addCommodity(Commodities.DRUGS, MathUtils.getRandomNumberInRange(250f, 500f))

                salvage.addSpecial(SpecialItemData("rat_artifact", "rat_super_serum"),1f)

                visualPanel.showLoot("Loot", salvage, true) {
                    closeDialog()

                    interactionTarget.fadeAndExpire(3f)
                }
            }
        }

        addLeaveOption()
    }

}