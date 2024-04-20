package assortment_of_things.backgrounds.bounty

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.listeners.FleetEventListener

class BountyFleetListener : FleetEventListener {
    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?,  param: Any?) {


    }

    /**
     * "fleet" will be null if the listener is registered with the ListenerManager, and non-null
     * if the listener is added directly to a fleet.
     * @param fleet
     * @param primaryWinner
     * @param battle
     */
    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        if (battle == null) return
        if (battle!!.isPlayerInvolved) {
            fleet!!.memoryWithoutUpdate.set("\$fought_player_in_battle", true)
        }
    }

}