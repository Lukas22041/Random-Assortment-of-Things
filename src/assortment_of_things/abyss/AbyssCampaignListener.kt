package assortment_of_things.abyss

import assortment_of_things.scripts.RATBaseCampaignEventListener
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin
import com.fs.starfarer.api.campaign.SpecialItemData

class AbyssCampaignListener : RATBaseCampaignEventListener() {

    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {
        super.reportEncounterLootGenerated(plugin, loot)

        if (plugin?.loser?.faction?.id == "rat_abyssals_primordials") {
            loot?.addSpecial(SpecialItemData("rat_ai_core_special", RATItems.PRIMORDIAL), 1f)

            loot!!.addWeapons("rat_abaddon_lance", 1)
            loot!!.addWeapons("rat_lucifers_wrath", 1)

            loot!!.addWeapons("rat_azazel_blade", 4)
            loot!!.addWeapons("rat_apollyons_end", 3)

        }

    }
}