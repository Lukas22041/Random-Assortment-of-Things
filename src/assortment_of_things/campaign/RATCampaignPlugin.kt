package assortment_of_things.campaign

import assortment_of_things.campaign.intel.DimensionalTearInteraction
import assortment_of_things.campaign.interactions.ChiralStationInteraction
import assortment_of_things.campaign.interactions.OutpostPlanetInteraction
import assortment_of_things.campaign.interactions.SpacersGambitInteraction
import assortment_of_things.misc.RATStrings
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions

class RATCampaignPlugin : BaseCampaignPlugin()
{

    override fun isTransient(): Boolean {
        return true
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        if (interactionTarget == null) return null

        if (interactionTarget.market != null && interactionTarget.market.hasTag(RATStrings.TAG_BLACKMARKET_PLANET) )
        {
            return PluginPick(SpacersGambitInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(RATStrings.TAG_OUTPOST_PLANET))
        {
            return PluginPick(OutpostPlanetInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.hasTag(RATStrings.TAG_DIMENSIONAL_TEAR))
        {
            return PluginPick(DimensionalTearInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        if (interactionTarget.customEntityType == "rat_chiral_station2")
        {
            return PluginPick(ChiralStationInteraction(), CampaignPlugin.PickPriority.HIGHEST)
        }

        return null
    }

}