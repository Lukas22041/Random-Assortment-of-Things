package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SuperSerumArtifact : BaseArtifactPlugin() {

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Decreases the crew required in the fleet by 20%% and reduces the amount of crew casualties in combat by 25%%.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
        "20%", "25%")
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

    }


    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {
        stats.minCrewMod.modifyMult(id, 0.8f)
        stats.crewLossMult.modifyMult(id, 0.75f)
    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {

    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {

    }

}