package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.hullmods.Automated
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ComputationalMatrix : BaseArtifactPlugin() {

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("All automated ships that are affected by the combat readiness penalty get an additional 15%% to their maximum CR.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
        "automated ships", "combat readiness penalty", "15%")
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

    }


    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {
        if (stats.variant.hasHullMod(HullMods.AUTOMATED)) {
            if (!Automated.isAutomatedNoPenalty(stats))
            {
                stats.maxCombatReadiness.modifyFlat(id, 0.15f, "Computation Matrix")
            }
        }
    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {

    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {

    }

}