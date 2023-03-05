package assortment_of_things.campaign

import assortment_of_things.campaign.intel.DimensionalTearInteraction
import assortment_of_things.campaign.interactions.ChiralStationInteraction
import assortment_of_things.campaign.interactions.OutpostPlanetInteraction
import assortment_of_things.campaign.interactions.SpacersGambitInteraction
import assortment_of_things.campaign.items.cores.AmberProcessorCore
import assortment_of_things.campaign.items.cores.AzureProcessorCore
import assortment_of_things.campaign.items.cores.ScarletProcessorCore
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

        if (interactionTarget.market != null && interactionTarget.market.hasTag(RATTags.TAG_BLACKMARKET_PLANET) )
        {
            return PluginPick(SpacersGambitInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(RATTags.TAG_OUTPOST_PLANET))
        {
            return PluginPick(OutpostPlanetInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(RATTags.TAG_DIMENSIONAL_TEAR))
        {
            return PluginPick(DimensionalTearInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.customEntityType == "rat_chiral_station2")
        {
            return PluginPick(ChiralStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        return null
    }

  /*  override fun pickAICoreAdminPlugin(commodityId: String?): PluginPick<AICoreAdminPlugin>? {
        if (commodityId == "rat_scarlet_processor") return PluginPick(TestCoreAdmin(), CampaignPlugin.PickPriority.HIGHEST)
        return null


    }*/

    override fun pickAICoreOfficerPlugin(commodityId: String?): PluginPick<AICoreOfficerPlugin>? {
        if (commodityId == RATItems.SCARLET_PROCESSOR) return PluginPick(ScarletProcessorCore(), CampaignPlugin.PickPriority.HIGHEST)
        if (commodityId == RATItems.AZURE_PROCESSOR) return PluginPick(AzureProcessorCore(), CampaignPlugin.PickPriority.HIGHEST)
        if (commodityId == RATItems.AMBER_PROCESSOR) return PluginPick(AmberProcessorCore(), CampaignPlugin.PickPriority.HIGHEST)
        return null
    }
}