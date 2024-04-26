package assortment_of_things.misc

import assortment_of_things.frontiers.FrontiersUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.CharacterBackgroundUtils

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

                    var moduleSlots = member.variant.moduleSlots
                    for (slot in moduleSlots) {
                        var module = member.variant.getModuleVariant(slot)
                        module.addMod("rat_controller")
                    }
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

        if (Global.getSettings().modManager.isModEnabled("nexerelin")) {
            if (CharacterBackgroundUtils.isBackgroundActive("rat_force_in_numbers")) {
                if (hullSize == ShipAPI.HullSize.FRIGATE || hullSize == ShipAPI.HullSize.DESTROYER) {
                    stats.maxCombatReadiness.modifyFlat("rat_force_in_numbers", 0.15f, "Force in numbers")
                    stats.peakCRDuration.modifyFlat("rat_force_in_numbers", 60f)

                    stats.maxSpeed.modifyMult("rat_force_in_numbers", 1.2f)
                    stats.acceleration.modifyMult("rat_force_in_numbers", 1.2f)
                    stats.deceleration.modifyMult("rat_force_in_numbers", 1.2f)
                    stats.turnAcceleration.modifyMult("rat_force_in_numbers", 1.2f)
                    stats.maxTurnRate.modifyMult("rat_force_in_numbers", 1.2f)

                    stats.fluxDissipation.modifyMult("rat_force_in_numbers", 1.1f)
                    stats.energyRoFMult.modifyMult("rat_force_in_numbers", 1.1f)
                    stats.ballisticRoFMult.modifyMult("rat_force_in_numbers", 1.1f)
                    stats.missileRoFMult.modifyMult("rat_force_in_numbers", 1.1f)
                    stats.energyAmmoRegenMult.modifyMult("rat_force_in_numbers", 1.1f)
                    stats.ballisticAmmoRegenMult.modifyMult("rat_force_in_numbers", 1.1f)
                    stats.missileAmmoRegenMult.modifyMult("rat_force_in_numbers", 1.1f)

                    stats.armorBonus.modifyMult("rat_force_in_numbers", 1.15f)
                }
                if (hullSize == ShipAPI.HullSize.CRUISER || hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                    stats.maxCombatReadiness.modifyFlat("rat_force_in_numbers", -0.15f, "Force in numbers")
                    stats.maxCombatReadiness.modifyMult("rat_force_in_numbers", 0.333f, "Force in numbers")
                    stats.peakCRDuration.modifyMult("rat_force_in_numbers", 0.333f)

                    stats.suppliesPerMonth.modifyMult("rat_force_in_numbers", 1.5f)
                    stats.fuelUseMod.modifyMult("rat_force_in_numbers", 1.5f)
                    stats.maxBurnLevel.modifyFlat("rat_force_in_numbers", -1f)

                    stats.cargoMod.modifyMult("rat_force_in_numbers", 0.8f)
                    stats.fuelMod.modifyMult("rat_force_in_numbers", 0.8f)
                }
            }

            if (CharacterBackgroundUtils.isBackgroundActive("rat_personal_army")) {
                var flat = when(hullSize) {
                    ShipAPI.HullSize.FRIGATE -> 10f
                    ShipAPI.HullSize.DESTROYER -> 20f
                    ShipAPI.HullSize.CRUISER -> 35f
                    ShipAPI.HullSize.CAPITAL_SHIP -> 50f
                    else -> 10f
                }
                stats.dynamic.getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, flat)
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