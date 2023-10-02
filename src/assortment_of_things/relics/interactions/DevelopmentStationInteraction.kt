package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.util.ArrayList
import kotlin.random.Random
import kotlin.random.asJavaRandom

class DevelopmentStationInteraction : RATInteractionPlugin() {

    override fun init() {
        textPanel.addPara("Your fleet approaches the development station.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Continue") {
            clearOptions()
            textPanel.addPara("You dock at the station and take a look through the its dozens of compartments. Many of those seem to be dedicated for the production and research of non-standardised modspecs that dont seem to have made it out in to the sector before the collapse.\n\n" + "Not much seems to have stood the test of time, but atleast some of it appears recoverable.")

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
                drop.group = "blueprints_low"
                dropRandom.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 4
                drop.group = "weapons2"
                dropRandom.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 1
                drop.valueMult = 0.3f
                drop.group = "rare_tech_low"
                dropRandom.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 1
                drop.group = "relics_guaranteed_alt"
                dropRandom.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 1
                drop.group = "relics_alteration"
                dropRandom.add(drop)

                var salvage = SalvageEntity.generateSalvage(random.asJavaRandom(), 2f, 1f, 1f, 1f, dropValue, dropRandom)

                visualPanel.showLoot("Loot", salvage, true) {
                    closeDialog()

                    interactionTarget.fadeAndExpire(3f)
                }
            }
        }

        addLeaveOption()
    }

}