package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class DefensiveArrayArtifact : BaseArtifactPlugin() {

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the range of all point-defense weapons in the fleet by 100 units and ensures the best possible target leading for them.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
        "point-defense", "100", "target leading")
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

    }


    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {
        stats.beamPDWeaponRangeBonus.modifyFlat(id, 100f)
        stats.nonBeamPDWeaponRangeBonus.modifyFlat(id, 100f)
        stats.dynamic.getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f)
        stats.dynamic.getMod(Stats.PD_BEST_TARGET_LEADING).modifyFlat(id, 1f)
    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {

    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {

    }

}