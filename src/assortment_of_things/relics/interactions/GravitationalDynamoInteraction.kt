package assortment_of_things.relics.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList

class GravitationalDynamoInteraction : RATInteractionPlugin() {

    override fun init() {

        textPanel.addPara("Your fleet approaches the gravitational dynamo.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        if (interactionTarget.hasTag("rat_dynamo_active")) {
            textPanel.addPara("The dynamo is active and is sending power towards nearby planetery receivers.")
        }

        if (!interactionTarget.hasTag("rat_defenders_defeated")) {
            createOption("Continue") {
                clearOptions()

                textPanel.addPara("As you approach the dynamo, bridge alarms go off as a previously undifferentiated shapes detach themselves from the superstructure and smoothly accelerate towards your fleet.")

                textPanel.addPara("The station's magnetic field should be able to protect the fleet from black hole's harmful radiation, but the unindentified vessels appear to pose a large threat nonetheless.")

                triggerDefenders()

            }
        }


        addLeaveOption()
    }

    override fun defeatedDefenders() {
        interactionTarget.addTag("rat_defenders_defeated")

        clearOptions()

        textPanel.addPara("With the defeat of the defenses the fleet makes its way to dock near the structure.")

        textPanel.addPara("The structure turns out to be much to complex to salvage, however its storage halls hold some objects of interest.")

        createOption("Loot the stations storage") {
            clearOptions()
            var random = Random(interactionTarget.getSalvageSeed())

            var dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()
            var dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()
            var drop = SalvageEntityGenDataSpec.DropData()

            drop = SalvageEntityGenDataSpec.DropData()
            drop.group = "basic"
            drop.value = 25000
            dropValue.add(drop)

            drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 1
            drop.group = "rare_tech"
            dropRandom.add(drop)

            drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 4
            drop.group = "blueprints"
            dropRandom.add(drop)

            drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 3
            drop.group = "any_hullmod_medium"
            dropRandom.add(drop)

            drop = SalvageEntityGenDataSpec.DropData()
            drop.chances = 12
            drop.group = "weapons2"
            dropRandom.add(drop)

            var salvage = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, dropValue, dropRandom)

            salvage.addSpecial(SpecialItemData("rat_alteration_install", "rat_upscale_protocol"), 1f)

            visualPanel.showLoot("Loot", salvage, true) {
                clearOptions()
                textPanel.addPara("After scavenging the contents of the storage facilities you access the control panel. Analysis shows that despite its age, the dynamo is still fully functional, and simply disabled itself due to inactivity")

                textPanel.addPara("Activating the station will provide a 50%% hazard reduction to all planets in the system and an increase their population growth.",
                    Misc.getTextColor(), Misc.getHighlightColor(), "50%", "population growth")

                createOption("Activate") {
                    clearOptions()
                    interactionTarget.addTag("rat_dynamo_active")

                    textPanel.addPara("The Dynamo resumes its normal operation.")

                    addLeaveOption()
                }
            }
        }
    }
}