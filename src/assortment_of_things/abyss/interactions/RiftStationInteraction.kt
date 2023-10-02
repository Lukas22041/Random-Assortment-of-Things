package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.magiclib.kotlin.getSalvageSeed
import java.util.*

class RiftStationInteraction : RATInteractionPlugin() {
    override fun init() {


        textPanel.addPara("The fleet approaches the station at the center of the rift. It appears that the station houses some kind of devide to keep this rift active, its purpose however is yet unknown. There doesnt appear to be any hostile activity around it.")

        var fleet = interactionTarget.memoryWithoutUpdate.get("\$defenderFleet")
        if (fleet != null ) {
           /* textPanel.addPara("However as we attempt to close in, as if out of nowhere a fleet emerges in the vicinity of the station. " +
                    "It contains signatures that haven't been observed anywhere else within the abyss, it's adviced to proceed with extreme caution.")*/

            textPanel.addPara("However as we attempt to close in, as if out of nowhere a fleet emerges in the vicinity of the station. " +
                    "Its threat level is beyond any fleet we have yet observed, it's adviced to proceed with extreme caution.")

            triggerDefenders()
        }
        else {
          /*  closeDialog()
            Misc.fadeAndExpire(interactionTarget)*/

            textPanel.addPara("With nothing in it's way, the fleet docks at the station. Unlike most other entities within the abyss so far, this one seems to be of more recent origin.")

            addOptions()
        }

    }


    fun addOptions() {

        var rewardShip = interactionTarget.memoryWithoutUpdate.get("\$rewardShip")
        if (rewardShip != null) {

            textPanel.addPara("One of the defending ships that now hover disabled around the station appears to be in good enough conditions to be recovered by our repair crew.")
            createOption("Recover the unique hull") {

            }
        }

        var looted = interactionTarget.memoryWithoutUpdate.get("\$rat_looted")
        if (looted == null) {
            textPanel.addPara("The station appears to be full with recoverable items, many appearing to be of the less common kind.")
            createOption("Loot the station") {
                loot()
            }
        }


        addLeaveOption()

    }

    fun loot() {

        var random = Random(interactionTarget.getSalvageSeed())

        var dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()
        var dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()
        var drop = SalvageEntityGenDataSpec.DropData()

        drop = SalvageEntityGenDataSpec.DropData()
        drop.chances = 6
        drop.group = "rat_abyss_fabricator"
        dropRandom.add(drop)

        drop = SalvageEntityGenDataSpec.DropData()
        drop.chances = 2
        drop.group = "rare_tech"
        drop.valueMult = 0.25f
        dropRandom.add(drop)

        drop = SalvageEntityGenDataSpec.DropData()
        drop.chances = 1
        drop.group = "blueprints"
        dropRandom.add(drop)

        drop = SalvageEntityGenDataSpec.DropData()
        drop.chances = 1
        drop.group = "any_hullmod_medium"
        dropRandom.add(drop)

        drop = SalvageEntityGenDataSpec.DropData()
        drop.chances = 8
        drop.group = "weapons2"
        dropRandom.add(drop)

        drop = SalvageEntityGenDataSpec.DropData()
        drop.chances = 12
        drop.group = "rat_abyss_weapons"
        dropRandom.add(drop)

        drop = SalvageEntityGenDataSpec.DropData()
        drop.group = "basic"
        drop.value = 15000
        dropValue.add(drop)

        var mult = 3f

        var salvage = SalvageEntity.generateSalvage(random, mult, mult, 1f, 1f, dropValue, dropRandom)

        salvage.addSpecial(SpecialItemData("rat_alteration_install", "rat_primordial_stream"), 1f)

        var picker = WeightedRandomPicker<String>()
        picker.random = Random(interactionTarget.getSalvageSeed())
        picker.add("rat_automated_conversion")
        picker.add("rat_unaffected")

        salvage.addSpecial(SpecialItemData("rat_alteration_install", picker.pick()), 1f)
        ArtifactUtils.generateArtifactNoDupe(salvage, "abyss", Random(interactionTarget.getSalvageSeed()))

        visualPanel.showLoot("Loot", salvage, true) {

            interactionTarget.removeTag(AbyssTags.LOOTABLE)
            interactionTarget.memoryWithoutUpdate.set("\$rat_looted", true)
            reset()
        }
    }

    override fun defeatedDefenders() {
        clearOptions()
        interactionTarget.memoryWithoutUpdate.set("\$defenderFleet", null)

        textPanel.addPara("With the defending fleet disabled, the fleet proceeds to dock at the station. Unlike most other entities within the abyss so far, this one seems to be of more recent origin.")




        addOptions()
    }
}