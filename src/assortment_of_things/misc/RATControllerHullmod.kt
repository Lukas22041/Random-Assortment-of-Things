package assortment_of_things.misc

import assortment_of_things.frontiers.FrontiersUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc

class RATControllerHullmod : BaseHullMod() {

    companion object {
        fun ensureAddedControllerToFleet() {
            var playerfleet = Global.getSector().playerFleet ?: return
            if (playerfleet.fleetData?.membersListCopy == null) return
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

        if (FrontiersUtils.hasSettlement()) {
            var settlement = FrontiersUtils.getSettlementData()
            if (settlement.hasFacility("quality_control")) {

                var mod = when(hullSize) {
                    ShipAPI.HullSize.CAPITAL_SHIP -> 0.95f
                    ShipAPI.HullSize.CRUISER -> 0.9f
                    ShipAPI.HullSize.DESTROYER -> 0.85f
                    ShipAPI.HullSize.FRIGATE -> 0.8f
                    else -> 0.8f
                }

                stats.suppliesPerMonth.modifyMult("rat_quality_control", mod)

                stats.dynamic.getMod(Stats.SHIP_RECOVERY_MOD).modifyFlat(id, 2f)
            }
            if (settlement.hasFacility("specialised_training")) {
                stats.minCrewMod.modifyMult("rat_specialised_training", 0.85f)
            }
            if (settlement.hasFacility("logistics_base")) {
                stats.cargoMod.modifyMult("logistics_base", 1.25f)
                stats.fuelMod.modifyMult("logistics_base", 1.25f)
            }
        }

        //Since Nex Backgrounds is still in beta and i just noticed this will crash on non-beta versions
       /* try {
            //Will require slight changes to this system to work, noteably i need to make the hullmod be applied from another script, since so far it has been just used for skills
            if (CharacterBackgroundUtils.isBackgroundActive("")) {

            }
        } catch (e: Throwable) {}*/


    }
}