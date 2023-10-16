package assortment_of_things.relics.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList

class DamagedCryosleeperInteraction : RATInteractionPlugin() {

    override fun init() {

        textPanel.addPara("Your fleet approaches the damaged cryosleeper.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        if (!interactionTarget.hasTag("rat_defenders_defeated")) {
            createOption("Continue") {
                clearOptions()

                textPanel.addPara("As your fleet moves in closer, new energy signatures are detected near the cryosleeper.")

                textPanel.addPara("An automated drone is firing upon any kind of debri in its view. It appears that what sealed the fate of this ship is a defect in a protocol of its voyages defender.")

                textPanel.addPara("Any attempt to approach will most likely result in it as picking us as the next piece of \"debri\".")

                triggerDefenders()

            }
        }


        addLeaveOption()
    }

    override fun defeatedDefenders() {
        interactionTarget.addTag("rat_defenders_defeated")

        clearOptions()

        textPanel.addPara("Once the threat has been disabled the crew makes its way towards the cryosleepers interior. As expected the cryopods life-support has long gone offline, not a single one showing a signal of life.")


        textPanel.addPara("This cryosleeper seems rather well supplied with equipment for the construction of an innitial colony. It is especialy well prepared for the habitation of a planet with few ressources.")


        createOption("Salvage the cryosleeper") {
            clearOptions()
            var random = Random(interactionTarget.getSalvageSeed())
            var depth = AbyssUtils.getSystemData(interactionTarget.starSystem).depth

            var dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()
            var dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()
            var drop = SalvageEntityGenDataSpec.DropData()

            drop = SalvageEntityGenDataSpec.DropData()
            drop.group = "basic"
            drop.value = 20000
            dropValue.add(drop)

            drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 1
            drop.group = "rare_tech"
            dropRandom.add(drop)

            drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 1
            drop.group = "blueprints"
            dropRandom.add(drop)

            drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 1
            drop.group = "any_hullmod_medium"
            dropRandom.add(drop)

            var salvage = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, dropValue, dropRandom)

            salvage.addSpecial(SpecialItemData(RATItems.CONSUMEABLE_INDUSTRY_BP, "rat_expedition_hub"), 1f)

            visualPanel.showLoot("Loot", salvage, true) {
                closeDialog()

                interactionTarget.fadeAndExpire(3f)
            }
        }
    }
}