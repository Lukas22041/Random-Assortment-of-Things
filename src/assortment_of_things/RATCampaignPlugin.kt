package assortment_of_things

import assortment_of_things.abyss.AbyssBattleCreationPlugin
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.boss.GenesisInteraction
import assortment_of_things.abyss.boss.GenesisReencounterInteractionPlugin
import assortment_of_things.abyss.entities.hyper.AbyssalFracture
import assortment_of_things.abyss.interactions.AbyssSensorInteraction
import assortment_of_things.abyss.interactions.ethereal.AbyssalRaphaelInteraction
import assortment_of_things.abyss.interactions.primordial.PrimordialCatalystInteraction
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.interactions.ExoshipLockedOutInteraction
import assortment_of_things.exotech.interactions.ExoshipRemainsInteraction
import assortment_of_things.exotech.interactions.HyperNavBeaconInteraction
import assortment_of_things.exotech.interactions.exoship.NPCExoshipInteraction
import assortment_of_things.exotech.interactions.exoship.PlayerExoshipInteraction
import assortment_of_things.exotech.interactions.questBeginning.BeginningAtExoshipInteraction
import assortment_of_things.exotech.interactions.questBeginning.BeginningQuestEndInteraction
import assortment_of_things.exotech.interactions.warpCatalystMission.ExotechHideoutInteraction
import assortment_of_things.exotech.items.ExoProcessor
import assortment_of_things.relics.RelicsUtils
import assortment_of_things.relics.interactions.*
import assortment_of_things.relics.items.cores.NeuroCore
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*

class RATCampaignPlugin : BaseCampaignPlugin()
{

    override fun isTransient(): Boolean {
        return true
    }

    override fun pickBattleCreationPlugin(opponent: SectorEntityToken?): PluginPick<BattleCreationPlugin>? {

        if (opponent?.containingLocation == AbyssUtils.getData().system) {
            return PluginPick<BattleCreationPlugin>(AbyssBattleCreationPlugin(), CampaignPlugin.PickPriority.HIGHEST)
        }

        return super.pickBattleCreationPlugin(opponent)
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if (interactionTarget == null) return null

        var exoData = ExoUtils.getExoData()

        if (interactionTarget.hasTag("hypernav_beacon")) {
            return PluginPick(HyperNavBeaconInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget is CustomCampaignEntityAPI && interactionTarget.customEntitySpec.id == "rat_exoship") {
            var plugin = interactionTarget.customPlugin as ExoshipEntity

            if (exoData.lockedOutOfQuest) {
                return PluginPick(ExoshipLockedOutInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }
            else if (exoData.foundExoshipRemains && exoData.QuestBeginning_Active && !exoData.QuestBeginning_Done) {
                return PluginPick(BeginningQuestEndInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }
            else if (!exoData.QuestBeginning_StartedFromRemains && !exoData.QuestBeginning_Done) {
                return PluginPick(BeginningAtExoshipInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }
            else if (plugin.playerModule.isPlayerOwned) {
                return PluginPick(PlayerExoshipInteraction(false), CampaignPlugin.PickPriority.HIGHEST)
            }
            else {
                return PluginPick(NPCExoshipInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }
        }

        if (interactionTarget is CustomCampaignEntityAPI && interactionTarget.customEntitySpec.id == "rat_exoship_broken") {
            return PluginPick(ExoshipRemainsInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            //return PluginPick(ExoshipWreckageInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }




        //Relics
        if (interactionTarget.hasTag(RelicsUtils.RELICS_ENTITY_TAG)) {

            var id = interactionTarget.customEntitySpec.id

            when(id) {
                //"rat_development_station" -> return PluginPick(DevelopmentStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_orbital_construction_station" -> return PluginPick(AssemblyStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_refurbishment_station" -> return PluginPick(RefurbishmentStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_cryochamber" -> return PluginPick(CryochamberInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_spatial_laboratory" -> return PluginPick(SpatialLaboratoryInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_medical_laboratory" -> return PluginPick(MedicalLaboratoryInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_training_station" -> return PluginPick(TrainingStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_gravitational_dynamo" -> return PluginPick(GravitationalDynamoInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_neural_laboratory" -> return PluginPick(NeuralLaboratoryInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_damaged_cryosleeper" -> return PluginPick(DamagedCryosleeperInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_exo_cache" -> return PluginPick(ExoCacheInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }

            if (id == "rat_bioengineering_station" || id == "rat_augmentation_station") {
                return PluginPick(SkillStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }
        }


        //Abyss

        if (interactionTarget is CampaignFleetAPI) {
            if (interactionTarget.hasTag("rat_genesis_fleet")) {
                return PluginPick(GenesisInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }
        }

        if (interactionTarget.hasTag("rat_genesis_refight")) {
            return PluginPick(GenesisReencounterInteractionPlugin(), CampaignPlugin.PickPriority.HIGHEST)
        }

        var plugin = interactionTarget.customPlugin

        if (plugin is AbyssalFracture)  {
            if (plugin.connectedEntity != null) {
                Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(plugin.connectedEntity, ""), 0.01f)
                return null
            }
        }

        /*
        if (plugin is AbyssalFractureSmall)  {

            var system = AbyssUtils.getAbyssData().lastExitFractureSystem
            var token = system!!.createToken(AbyssUtils.getAbyssData().lastExitFractureDestination)

            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(token, ""), 0.01f)

        }*/
        if (interactionTarget.hasTag("rat_abyss_entrance")) {

            var fracture = interactionTarget.memoryWithoutUpdate.get("\$rat_jumpoint_destination_override") as SectorEntityToken
            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(fracture, ""), 0.01f)
            return PluginPick(null, CampaignPlugin.PickPriority.HIGHEST)
        }



        if (interactionTarget is CustomCampaignEntityAPI) {

            var id = interactionTarget.customEntitySpec.id

            if (interactionTarget.hasTag("rat_abyss_sierra_raphael")) {
                return PluginPick(AbyssalRaphaelInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }

            when (id) {
                "rat_abyss_sensor" -> return PluginPick(AbyssSensorInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_decaying_abyss_sensor" -> return PluginPick(AbyssSensorInteraction(), CampaignPlugin.PickPriority.HIGHEST)

                "rat_abyss_primordial_activator" -> return PluginPick(PrimordialCatalystInteraction(), CampaignPlugin.PickPriority.HIGHEST)

            }

            /*

            if (interactionTarget.hasTag(AbyssTags.ABYSS_WRECK)) {
                return PluginPick(AbyssalWreckInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }*/

            if (interactionTarget.hasTag("rat_exo_hideout")) {
                return PluginPick(ExotechHideoutInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }


            /*when (id) {
                "rat_abyss_rift_station" -> return PluginPick(RiftStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_abyss_fabrication" -> return PluginPick(FabrictationStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_abyss_accumalator" -> return PluginPick(AccumalatorStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_abyss_drone" -> return PluginPick(AbyssalProbeInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_abyss_transmitter" -> return PluginPick(TransmitterInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_abyss_research" -> return PluginPick(AbyssalResearchStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_abyss_unknown_lab" -> return PluginPick(AbyssalUnknownLabInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_military_outpost" -> return PluginPick(AbyssalMilitaryOutpostInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_sariel_outpost" -> return PluginPick(AbyssSarielOutpostInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }*/
        }


        //Backgrounds

        if (interactionTarget is CustomCampaignEntityAPI) {

            var id = interactionTarget.id
            var specID = interactionTarget.customEntitySpec.id


        }

        return null
    }


   /* override fun pickAICoreAdminPlugin(commodityId: String?): PluginPick<AICoreAdminPlugin>? {
        if (commodityId == RATItems.JEFF) return PluginPick(JeffCoreAdmin(), CampaignPlugin.PickPriority.HIGHEST)
        return null
    }*/

    override fun pickAICoreOfficerPlugin(commodityId: String?): PluginPick<AICoreOfficerPlugin>? {

        if (commodityId == RATItems.COSMOS_CORE) return PluginPick(CosmosCore(), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        if (commodityId == RATItems.CHRONOS_CORE) return PluginPick(ChronosCore(), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        if (commodityId == RATItems.SERAPH_CORE) return PluginPick(SeraphCore(), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        if (commodityId == RATItems.PRIMORDIAL) return PluginPick(PrimordialCore(), CampaignPlugin.PickPriority.MOD_SPECIFIC)

        if (commodityId == "rat_neuro_core") {
            return PluginPick(NeuroCore(), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        }

        if (commodityId == "rat_exo_processor") {
            return PluginPick(ExoProcessor(), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        }

        return null
    }

}