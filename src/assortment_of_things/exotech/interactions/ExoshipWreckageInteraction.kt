package assortment_of_things.exotech.interactions

import assortment_of_things.exotech.items.ExoProcessor
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.loading.Description
import java.util.*

class ExoshipWreckageInteraction : RATInteractionPlugin() {
    override fun init() {
        textPanel.addPara("Your fleet approaches the wreckage.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        var defeated = interactionTarget.memoryWithoutUpdate.get("\$defeated")

        if (defeated == null) {

            var defender = interactionTarget.memoryWithoutUpdate.get("\$defenderFleet")
            if (defender == null) {
                defender = generateFleet()
            }

            createOption("Continue") {
                clearOptions()

                textPanel.addPara("As your fleet further approaches the exoship, a fleet of ships emerge from the hangars. " +
                        "They are quickly identified as automated drones based on exo-tech specs." +
                        "\n\n" +
                        "They have been likely assigned to defend the wreckage from any salvager trying to acquire anything left of worth. " +
                        "Their flagship seems to be piloted by a custom ai, caution is recommended."
                )

                triggerDefenders()

            }
        }
        else {
            textPanel.addPara("All worthwhiles within this place have already been acquired.")
            addLeaveOption()
        }

    }

    override fun defeatedDefenders() {
        interactionTarget.memoryWithoutUpdate.set("\$defeated", true)

        addLeaveOption()
    }

    fun generateFleet() : CampaignFleetAPI {
        var fleet = Global.getFactory().createEmptyFleet("rat_exotech", "Automated Defenses", false)
        fleet.name = "Automatedd Defenses"
        fleet.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true)
        fleet.inflateIfNeeded()

        var arkas = fleet.fleetData.addFleetMember("rat_arkas_Strike")
        var arkasCore = ExoProcessor().createPerson("rat_exo_processor", "rat_exotech", Random())
        arkas.captain = arkasCore
        //fleet.fleetData.setFlagship(arkas)

        var leanira = fleet.fleetData.addFleetMember("rat_leanira_Support")
        var leaniraCore = AICoreOfficerPluginImpl().createPerson(Commodities.BETA_CORE, "rat_exotech", Random())
        leanira.captain = leaniraCore

        var tylos1 = fleet.fleetData.addFleetMember("rat_tylos_Standard")
        fleet.fleetData.addFleetMember("rat_tylos_Standard")
        fleet.fleetData.addFleetMember("rat_tylos_Standard")
        fleet.fleetData.addFleetMember("rat_tylos_Standard")

        fleet.fleetData.addFleetMember("rat_thestia_Support")
        fleet.fleetData.addFleetMember("rat_thestia_Support")
        fleet.fleetData.addFleetMember("rat_thestia_Support")

        for (member in fleet.fleetData.membersListCopy) {
            member.fixVariant()
            member.variant.addPermaMod(HullMods.AUTOMATED)
            member.repairTracker.cr = 0.85f
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)

            if (member != tylos1 && member != arkas) {
                member.variant.addTag(Tags.UNRECOVERABLE)
                member.variant.addTag(Tags.VARIANT_UNBOARDABLE)
            }

            if (member.captain?.isDefault == true) {
                var core = AICoreOfficerPluginImpl().createPerson(Commodities.GAMMA_CORE, "rat_exotech", Random())
                member.captain = core
            }
        }

        arkas.variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE)
        arkas.variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
        arkas.variant.addTag(Tags.SHIP_RECOVERABLE)
        tylos1.variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE)
        tylos1.variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
        tylos1.variant.addTag(Tags.SHIP_RECOVERABLE)

        fleet.inflateIfNeeded()
        interactionTarget.memoryWithoutUpdate.set("\$defenderFleet", fleet)

        return fleet
    }

}