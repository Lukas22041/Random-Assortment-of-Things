package assortment_of_things.artifacts.plugins

import assortment_of_things.artifacts.BaseArtifactPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class OrdnanceOverwriteArtifact : BaseArtifactPlugin() {

    override fun addDescription(tooltip: TooltipMakerAPI) {
        var label = tooltip.addPara("Decreases the range of all ballistic/energy weapons in the entire fleet by 20% but increases the firerate by 15% without increasing flux consumption.", 0f)

        label.setHighlight("ballistic", "energy", "20%", "15%")
        label.setHighlightColors(Misc.getBallisticMountColor(), Misc.getEnergyMountColor(),  Misc.getNegativeHighlightColor(), Misc.getHighlightColor())
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {

    }

    override fun applyEffectsBeforeShipCreation(size: ShipAPI.HullSize, stats: MutableShipStatsAPI, id: String) {
        stats.energyWeaponRangeBonus.modifyMult(id, 0.8f)
        stats.ballisticWeaponRangeBonus.modifyMult(id, 0.8f)

        stats.energyRoFMult.modifyMult(id, 1.15f)
        stats.ballisticRoFMult.modifyMult(id, 1.15f)

        stats.energyWeaponFluxCostMod.modifyMult(id, 0.85f)
        stats.missileWeaponFluxCostMod.modifyMult(id, 0.85f)
        stats.ballisticWeaponFluxCostMod.modifyMult(id, 0.85f)

    }


    override fun onInstall(fleet: CampaignFleetAPI, id: String) {

    }

    override fun onRemove(fleet: CampaignFleetAPI, id: String) {

    }

}