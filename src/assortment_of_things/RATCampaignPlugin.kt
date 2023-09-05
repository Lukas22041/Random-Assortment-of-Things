package assortment_of_things

import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.interactions.*
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.exonova.interactions.exoship.ExoshipInteractions
import assortment_of_things.relics.RelicsUtils
import assortment_of_things.relics.interactions.*
import assortment_of_things.relics.items.cores.TacticalCore
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.impl.campaign.ids.Commodities

class RATCampaignPlugin : BaseCampaignPlugin()
{

    override fun isTransient(): Boolean {
        return true
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if (interactionTarget == null) return null

        if (interactionTarget is CustomCampaignEntityAPI && interactionTarget.customEntitySpec.id == "rat_exoship") {
            return PluginPick(ExoshipInteractions(), CampaignPlugin.PickPriority.HIGHEST)
        }


        //Relics
        if (interactionTarget.hasTag(RelicsUtils.RELICS_ENTITY_TAG)) {

            var id = interactionTarget.customEntitySpec.id

            when(id) {
                "rat_development_station" -> return PluginPick(DevelopmentStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_orbital_construction_station" -> return PluginPick(AssemblyStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_refurbishment_station" -> return PluginPick(RefurbishmentStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_cryochamber" -> return PluginPick(CryochamberInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_spatial_laboratory" -> return PluginPick(SpatialLaboratoryInteraction(), CampaignPlugin.PickPriority.HIGHEST)
                "rat_medical_laboratory" -> return PluginPick(MedicalLaboratoryInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }

            if (id == "rat_bioengineering_station" || id == "rat_augmentation_station" || id == "rat_neural_laboratory") {
                return PluginPick(SkillStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
            }
        }


        //Abyss
        var plugin = interactionTarget.customPlugin
        if (plugin is AbyssalFracture)  {
            if (plugin.connectedEntity != null) {
                Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(plugin.connectedEntity, ""), 0.01f)
            }
        }

        if (interactionTarget is CustomCampaignEntityAPI && interactionTarget.customEntitySpec.id == "rat_abyss_rift_station") {
            return PluginPick(RiftStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget is CustomCampaignEntityAPI && interactionTarget.customEntitySpec.id == "rat_abyss_cache_singularity") {
            return PluginPick(SingularityCrateInteration(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(AbyssTags.ABYSS_WRECK)) {
            return PluginPick(AbyssalWreckInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(AbyssTags.DOMAIN_RESEARCH))
        {
            return PluginPick(DomainResearchInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }
        if (interactionTarget.hasTag(AbyssTags.TRANSMITTER))
        {
            return PluginPick(TransmitterInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }
        if (interactionTarget.hasTag(AbyssTags.LOST_CRATE))
        {
            return PluginPick(CacheInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        return null
    }

   /* override fun pickAICoreAdminPlugin(commodityId: String?): PluginPick<AICoreAdminPlugin>? {
        if (commodityId == RATItems.JEFF) return PluginPick(JeffCoreAdmin(), CampaignPlugin.PickPriority.HIGHEST)
        return null
    }*/

    override fun pickAICoreOfficerPlugin(commodityId: String?): PluginPick<AICoreOfficerPlugin>? {

        if (commodityId == RATItems.COSMOS_CORE) return PluginPick(CosmosCore(), CampaignPlugin.PickPriority.HIGHEST)
        if (commodityId == RATItems.CHRONOS_CORE) return PluginPick(ChronosCore(), CampaignPlugin.PickPriority.HIGHEST)
        if (commodityId == RATItems.PRIMORDIAL) return PluginPick(PrimordialCore(), CampaignPlugin.PickPriority.HIGHEST)

        if (commodityId == "rat_tactical_core") return PluginPick(TacticalCore(), CampaignPlugin.PickPriority.HIGHEST)

        return null
    }
}