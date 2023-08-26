package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class MiniatureForgeArtifact : BaseArtifactPlugin() {

    var deploymentModID = "deployment_points_mod"

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Decreases the deployment cost of all frigates and destroyers by 1.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
        "deployment", "frigates", "destroyers", "1")
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

    }


    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {
        if (size == ShipAPI.HullSize.FRIGATE || size == ShipAPI.HullSize.DESTROYER)
        {

            stats.getSuppliesToRecover().modifyFlat(id, -1f);
            stats.getDynamic().getMod(deploymentModID).modifyFlat(id, -1f);
        }
    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {

    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {

    }

}