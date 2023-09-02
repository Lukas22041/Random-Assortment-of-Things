package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class TransdimensionalAccumalatorArtifact : BaseArtifactPlugin() {

    var modID = "rat_accummalator_artifact"

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Halves the zero flux speed boost of all ships in the fleet, but it is now always active while a ship is below 25%% of its maximum flux.\n\n" +
                "Ships with the \"Safety Overrides\" hullmod are excluded from this effect.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
        "Halves", "zero flux speed boost", "25%", "Safety Overrides")
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

        if (!ship.variant.hasHullMod(HullMods.SAFETYOVERRIDES)) {
            ship.mutableStats.zeroFluxMinimumFluxLevel.modifyFlat(id, 0.25f)
            ship.mutableStats.zeroFluxSpeedBoost.modifyMult(id, 0.5f)
        }

    }


    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {

    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {

    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {

    }

}