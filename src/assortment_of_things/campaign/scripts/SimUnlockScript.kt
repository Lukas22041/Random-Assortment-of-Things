package assortment_of_things.campaign.scripts

import assortment_of_things.misc.getOriginalVariantRAT
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.SimulatorPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.SimUpdateIntel
import com.fs.starfarer.api.util.Misc

/*Add to fleets that should be codex unlockable*/
class SimUnlockerListener(var factionToUnlockFor: String = "rat_abyssals_sim") : FleetEventListener {

    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?, param: Any?) {

    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

        var plugin = Misc.getSimulatorPlugin()

        if (plugin !is SimulatorPluginImpl) return
        if (battle?.nonPlayerSideSnapshot == null) return

        //CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

//		unlocksData.factions.clear();
//		unlocksData.variants.clear();
        //unlocksData.variants.addAll(Global.getSettings().getSimOpponents());
        val addedFactions = LinkedHashSet<String>()
        var addedVariants = LinkedHashSet<String?>()

        for (fleet in battle.nonPlayerSideSnapshot) {/*if (fleet.faction == null || fleet.faction.factionSpec == null || fleet.faction.factionSpec.custom == null) continue
            val json = fleet.faction.factionSpec.custom.optJSONObject("simulatorData") ?: continue
            val show = json.optBoolean("showInSimulator")
            if (!show) continue*/

            val members = Misc.getSnapshotMembersLost(fleet)

            if (!plugin.unlocksData.factions.contains(factionToUnlockFor)) {
                plugin.unlocksData.factions.add(factionToUnlockFor)
                addedFactions.add(factionToUnlockFor)
            }

            for (member in members) {

                val vid: String? = plugin.getStockVariantId(member)

                if (vid != null) {
                    if (!plugin.unlocksData.variants.contains(vid)) {
                        plugin.unlocksData.variants.add(vid)
                        addedVariants.add(vid)
                    }
                }
            }
        }

        if (!addedVariants.isEmpty()) {
            addedVariants = LinkedHashSet(SimulatorPluginImpl.getVariantIDList(SimulatorPluginImpl.sortVariantList(
                SimulatorPluginImpl.getVariantList(LinkedHashSet(addedVariants)))))
        }

        if (!addedFactions.isEmpty() || !addedVariants.isEmpty()) {
            plugin.saveUnlocksData()

            SimUpdateIntel(addedFactions, addedVariants)
        }

    }

    fun getStockVariantId(member: FleetMemberAPI): String? {
        val v = member.variant
        if (!isAcceptableSimVariant(v, true)) return null

        var vid: String? = null
        if (v.isStockVariant) {
            vid = v.hullVariantId
        }
        var og = member.variant.getOriginalVariantRAT() //Use my OG variant method instead.
        if (vid == null && og != null) {
            vid = og
        }
        return vid
    }

    fun isAcceptableSimVariant(v: ShipVariantAPI?, forLearning: Boolean): Boolean {
        val allowAll = SimulatorPluginImpl.isSimFullyUnlocked() && !forLearning
        if (v == null) return false
        if ((v.hullSpec.hasTag(Tags.NO_SIM) || v.hasTag(Tags.NO_SIM)) && !allowAll) return false
        //if (v.hullSpec.hasTag(Tags.RESTRICTED) && !allowAll) return false
        if (v.hullSpec.hints.contains(ShipTypeHints.STATION)) return false
        return true
    }
}