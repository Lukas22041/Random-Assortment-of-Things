package assortment_of_things.campaign.scripts

import assortment_of_things.campaign.items.AICoreSpecialItemPlugin
import assortment_of_things.scripts.RATBaseCampaignEventListener
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.listeners.CargoScreenListener

class AICoreDropReplacerScript : RATBaseCampaignEventListener() {
    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {
        super.reportEncounterLootGenerated(plugin, loot)

        if (loot == null) return
        for (stack in loot.stacksCopy) {
            if (stack.isCommodityStack) {
                var id = stack.commodityId
                var quantity = stack.size
                if (AICoreSpecialItemPlugin.cores.containsKey(id)) {
                    loot.addSpecial(SpecialItemData("rat_ai_core_special", id), quantity)
                    loot.removeCommodity(id, quantity)
                }
            }
        }
    }
}