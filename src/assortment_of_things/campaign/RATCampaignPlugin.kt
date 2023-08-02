package assortment_of_things.campaign

import assortment_of_things.abyss.interactions.DomainResearchInteraction
import assortment_of_things.abyss.entities.AbyssalFracture
import assortment_of_things.abyss.interactions.AbyssalWreckInteraction
import assortment_of_things.abyss.interactions.CacheInteraction
import assortment_of_things.abyss.interactions.TransmitterInteraction
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.impl.campaign.ids.Entities

class RATCampaignPlugin : BaseCampaignPlugin()
{

    override fun isTransient(): Boolean {
        return true
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if (interactionTarget == null) return null

        var plugin = interactionTarget.customPlugin
        if (plugin is AbyssalFracture)  {
            if (plugin.connectedEntity != null) {
                Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(plugin.connectedEntity, ""), 0.01f)
            }
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

        return null
    }
}