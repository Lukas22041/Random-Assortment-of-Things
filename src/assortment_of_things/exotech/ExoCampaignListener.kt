package assortment_of_things.exotech

import assortment_of_things.scripts.RATBaseCampaignEventListener
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin

class ExoCampaignListener : RATBaseCampaignEventListener() {

    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {
        super.reportEncounterLootGenerated(plugin, loot)

        if (plugin?.loser?.memoryWithoutUpdate?.get("\$rat_rapid_response_fleet") == true) {
            loot!!.addFighters("rat_supernova_wing", 5)
            plugin.loser.memoryWithoutUpdate.set("\$rat_rapid_response_fleet", false)
        }
    }
}