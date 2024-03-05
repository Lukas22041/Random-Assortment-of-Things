package assortment_of_things.exotech.interactions

import assortment_of_things.exotech.items.ExoProcessor
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.fadeAndExpire
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.asJavaRandom

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
                        "They have likely been assigned to defend the wreckage from any salvager trying to acquire anything left of worth. " +
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

        textPanel.addPara("After defeating the drones, the fleet prepares to close on to the wreckage.")

        textPanel.addPara("There likely isnt much left in terms of raw materials, atleast in a useable state, but the drones must have been defending the place for a reason. " +
                "Investigating the wreckages interiors should proof worthwhile.")

        createOption("Loot Wreckage") {
            var loot = Global.getFactory().createCargo(true)

            //loot.addCommodity("rat_exo_processor", 1f)
            loot.addSpecial(SpecialItemData("rat_ai_core_special", "rat_exo_processor"), 1f)

            loot.addSpecial(SpecialItemData("rat_alteration_install", "rat_autonomous_bays"), 1f)

            loot.addWeapons("rat_hyper_javelin", 1)
            loot.addWeapons("rat_hyper_dart", 2)
            loot.addFighters("rat_dawnblade_wing", 1)


            visualPanel.showLoot("Salvage", loot, true) {

                clearOptions()

                textPanel.addPara("After looting whatevers left, the salvage crew returns with both unique items and some remaining logs.")

                textPanel.addPara("It appears that even after its destruction, even with almost all compontents not functioning, the few compartments remaining with just minor damage were used for developing further technologies.\n\n" +
                        "This kept them out of the eyes of the major factions to avoid repeated retaliation, atleast aslong as it took for their projects to bear fruit. According to the logs, the \"Arkas-Class\", and a unique phase-alligned AI-Core were developed here.")

                textPanel.addPara("But all of this has been far in the past, after the projects were finished and the faction had a way to defend itself, all new projects were moved towards their remaining exoships. This station has been left alone with its drones for dozens of cycles.")

                createOption("Leave") {
                    closeDialog()
                }

            }
        }
    }

    fun generateFleet() : CampaignFleetAPI {
        var fleet = Global.getFactory().createEmptyFleet("rat_exotech", "Automated Defenses", false)
        fleet.name = "Automated Defenses"
        fleet.isNoFactionInName = true
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
            member.variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
            member.repairTracker.cr = 0.85f
            member.variant.addTag(Tags.TAG_NO_AUTOFIT)

            for (slot in ArrayList(member.variant.moduleSlots)) {
                var module = member.variant.getModuleVariant(slot) ?: continue

                var variant = module.clone();
                variant.originalVariant = null;
                variant.hullVariantId = Misc.genUID()
                variant.source = VariantSource.REFIT
                member.variant.setModuleVariant(slot, variant)

                variant.addPermaMod(HullMods.AUTOMATED)
                variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
            }

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
        //arkas.variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
        arkas.variant.addTag(Tags.SHIP_RECOVERABLE)
        tylos1.variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE)
        //tylos1.variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
        tylos1.variant.addTag(Tags.SHIP_RECOVERABLE)

        fleet.inflateIfNeeded()
        interactionTarget.memoryWithoutUpdate.set("\$defenderFleet", fleet)

        return fleet
    }

}