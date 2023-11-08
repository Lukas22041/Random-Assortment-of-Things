package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc

class RATControllerHullmod : BaseHullMod() {

    companion object {
        fun ensureAddedControllerToFleet() {
            var playerfleet = Global.getSector().playerFleet
            for (member in playerfleet.fleetData.membersListCopy) {
                if (!member.variant.hasHullMod("rat_controller")) {
                    if (member.variant.source != VariantSource.REFIT) {
                        var variant = member.variant.clone();
                        variant.originalVariant = null;
                        variant.hullVariantId = Misc.genUID()
                        variant.source = VariantSource.REFIT
                        member.setVariant(variant, false, true)
                    }

                    member.variant.addMod("rat_controller")
                }
            }
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        var playerfleet = Global.getSector().playerFleet

        if (stats!!.variant.hasHullMod(HullMods.AUTOMATED)) {
            var hasAutoEngineer = playerfleet?.fleetData?.membersListCopy?.any { it.captain?.stats?.hasSkill("rat_auto_engineer") == true } ?: false

            if (hasAutoEngineer) {
                stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
                stats.getBreakProb().modifyMult(id, 0f);

                stats.maxCombatReadiness.modifyFlat(id, 0.10f, "Auto-Engineer")
            }
        }
    }
}