package assortment_of_things.scripts

import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.AbilityPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.EngagementResultAPI

open class RATBaseCampaignEventListener : CampaignEventListener {
    override fun reportPlayerOpenedMarket(market: MarketAPI?) {

    }

    override fun reportPlayerClosedMarket(market: MarketAPI?) {

    }

    override fun reportPlayerOpenedMarketAndCargoUpdated(market: MarketAPI?) {

    }

    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {

    }

    override fun reportPlayerMarketTransaction(transaction: PlayerMarketTransaction?) {

    }

    override fun reportBattleOccurred(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

    }

    override fun reportBattleFinished(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

    }

    override fun reportPlayerEngagement(result: EngagementResultAPI?) {

    }

    override fun reportFleetDespawned(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?, param: Any?) {

    }

    override fun reportFleetSpawned(fleet: CampaignFleetAPI?) {

    }

    override fun reportFleetReachedEntity(fleet: CampaignFleetAPI?, entity: SectorEntityToken?) {

    }

    override fun reportFleetJumped(fleet: CampaignFleetAPI?,  from: SectorEntityToken?,  to: JumpPointAPI.JumpDestination?) {

    }

    override fun reportShownInteractionDialog(dialog: InteractionDialogAPI?) {

    }

    override fun reportPlayerReputationChange(faction: String?, delta: Float) {

    }

    override fun reportPlayerReputationChange(person: PersonAPI?, delta: Float) {

    }

    override fun reportPlayerActivatedAbility(ability: AbilityPlugin?, param: Any?) {

    }

    override fun reportPlayerDeactivatedAbility(ability: AbilityPlugin?, param: Any?) {

    }

    override fun reportPlayerDumpedCargo(cargo: CargoAPI?) {

    }

    override fun reportPlayerDidNotTakeCargo(cargo: CargoAPI?) {

    }

    override fun reportEconomyTick(iterIndex: Int) {

    }

    override fun reportEconomyMonthEnd() {

    }
}