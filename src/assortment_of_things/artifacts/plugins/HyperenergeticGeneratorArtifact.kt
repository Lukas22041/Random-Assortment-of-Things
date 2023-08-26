package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class HyperenergeticGeneratorArtifact : BaseArtifactPlugin() {


    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the flux dissipation of all ships within the fleet by 10%%.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
        "flux dissipation", "10%")
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

    }


    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {
        stats.fluxDissipation.modifyMult(id, 1.1f)
    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {

    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {

    }
}