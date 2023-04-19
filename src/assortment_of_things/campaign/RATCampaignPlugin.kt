package assortment_of_things.campaign

import assortment_of_things.campaign.interactions.DimensionalGateInteraction
import assortment_of_things.campaign.interactions.*
import assortment_of_things.campaign.items.cores.admin.JeffCoreAdmin
import assortment_of_things.campaign.items.cores.officer.AmberProcessorCore
import assortment_of_things.campaign.items.cores.officer.AzureProcessorCore
import assortment_of_things.campaign.items.cores.officer.ScarletProcessorCore
import assortment_of_things.strings.RATItems
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*

class RATCampaignPlugin : BaseCampaignPlugin()
{

    override fun isTransient(): Boolean {
        return true
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if (interactionTarget == null) return null


        if (interactionTarget.hasTag(RATTags.TAG_OUTPOST_PLANET))
        {
            return PluginPick(OutpostPlanetInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }
        if (interactionTarget.hasTag(RATTags.TAG_OUTPOST_TRAINING_FACILITY))
        {
            return PluginPick(TrainingStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(RATTags.TAG_DIMENSIONAL_GATE))
        {
            return PluginPick(DimensionalGateInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(RATTags.TAG_CHIRAL_STATION1))
        {
            return PluginPick(NonChiralStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }
        if (interactionTarget.hasTag(RATTags.TAG_CHIRAL_STATION2))
        {
            return PluginPick(ChiralStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }
        /*if (interactionTarget.customEntityType == "rat_chiral_station2")
        {
            return PluginPick(ChiralStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }*/

        if (interactionTarget.hasTag(RATTags.TAG_CHIRAL_NEBULA))
        {
            return PluginPick(ChiralNebulaInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }


        return null
    }

    override fun pickAICoreAdminPlugin(commodityId: String?): PluginPick<AICoreAdminPlugin>? {
        if (commodityId == RATItems.JEFF) return PluginPick(JeffCoreAdmin(), CampaignPlugin.PickPriority.HIGHEST)
        return null
    }

    override fun pickAICoreOfficerPlugin(commodityId: String?): PluginPick<AICoreOfficerPlugin>? {
        if (commodityId == RATItems.SCARLET_PROCESSOR) return PluginPick(ScarletProcessorCore(), CampaignPlugin.PickPriority.HIGHEST)
        if (commodityId == RATItems.AZURE_PROCESSOR) return PluginPick(AzureProcessorCore(), CampaignPlugin.PickPriority.HIGHEST)
        if (commodityId == RATItems.AMBER_PROCESSOR) return PluginPick(AmberProcessorCore(), CampaignPlugin.PickPriority.HIGHEST)
        return null
    }
}