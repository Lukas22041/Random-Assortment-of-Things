package assortment_of_things.abyss.scripts

import assortment_of_things.scripts.RATBaseCampaignEventListener
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.listeners.CargoScreenListener

//Required because appearenently some salvage preventing tags dont work when the mod is smodded
class HullmodRemoverListener : RATBaseCampaignEventListener() {
    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {
        super.reportEncounterLootGenerated(plugin, loot)

        if (loot == null) return
        for (stack in loot.stacksCopy) {
            if (stack.hullModSpecIfHullMod != null) {
                var hullmod = stack.hullModSpecIfHullMod
                if (hullmod.hasTag("rat_alteration")) {
                    loot.removeStack(stack)
                }
            }
        }
    }
}