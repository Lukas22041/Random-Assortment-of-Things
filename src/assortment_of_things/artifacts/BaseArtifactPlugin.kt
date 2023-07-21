package assortment_of_things.artifacts

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

abstract class BaseArtifactPlugin() {


    abstract fun addDescription(tooltip: TooltipMakerAPI)

    open fun advanceInCombat(ship: ShipAPI, amount: Float)  {

    }

    abstract fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String)
    abstract fun applyEffectsBeforeShipCreation(size: HullSize, stats: MutableShipStatsAPI, id: String)

    abstract fun onInstall(fleet: CampaignFleetAPI, id: String)

    abstract fun onRemove(fleet: CampaignFleetAPI, id: String)

}