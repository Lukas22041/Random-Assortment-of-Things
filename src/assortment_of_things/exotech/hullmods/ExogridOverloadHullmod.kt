package assortment_of_things.exotech.hullmods

import assortment_of_things.misc.addNegativePara
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.impl.hullmods.PhaseAnchor
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.util.MagicIncompatibleHullmods
import java.awt.Color

class ExogridOverloadHullmod : BaseHullMod() {

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.zeroFluxMinimumFluxLevel.modifyFlat(id, 0.10f)

        stats.energyWeaponDamageMult.modifyMult(id, 1.1f)

        stats.fluxCapacity.modifyMult(id, 1.2f)

        if(stats.getVariant().getHullMods().contains(HullMods.SAFETYOVERRIDES)){
            //if someone tries to install heavy armor, remove it
            MagicIncompatibleHullmods.removeHullmodWithWarning(
                stats.getVariant(),
                HullMods.SAFETYOVERRIDES,
                "rat_exogrid_overload"
            );
        }

        if(stats.getVariant().getHullMods().contains(HullMods.PHASE_ANCHOR)){
            //if someone tries to install heavy armor, remove it
            MagicIncompatibleHullmods.removeHullmodWithWarning(
                stats.getVariant(),
                HullMods.PHASE_ANCHOR,
                "rat_exogrid_overload"
            );
        }
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {

    }

    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        var id = "rat_exogrid_${ship.id}"
        var stats = ship.mutableStats
        var player = ship == Global.getCombatEngine().playerShip

        val shipTimeMult = 1.1f
        stats.getTimeMult().modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        var phased = ship.isPhased
        if (ship.phaseCloak != null && ship.phaseCloak.isChargedown) {
            phased = false
        }

        if (phased) {
            var mult = 1.5f
            stats.fluxDissipation.modifyMult(id, mult)
            stats.ballisticRoFMult.modifyMult(id, mult)
            stats.energyRoFMult.modifyMult(id, mult)
            stats.missileRoFMult.modifyMult(id, mult)
            stats.ballisticAmmoRegenMult.modifyMult(id, mult)
            stats.energyAmmoRegenMult.modifyMult(id, mult)
            stats.missileAmmoRegenMult.modifyMult(id, mult)

        } else {
            stats.fluxDissipation.unmodifyMult(id)
            stats.ballisticRoFMult.unmodifyMult(id)
            stats.energyRoFMult.unmodifyMult(id)
            stats.missileRoFMult.unmodifyMult(id)
            stats.ballisticAmmoRegenMult.unmodifyMult(id)
            stats.energyAmmoRegenMult.unmodifyMult(id)
            stats.missileAmmoRegenMult.unmodifyMult(id)
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?,  ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip.addSpacer(10f)
        tooltip.addPara("The ships exogrid is forced to run semi-actively for the entire deployment. This allows the ship to gain the zero-flux boost while it is below 10%% of its flux capacity. Additionaly it further amplifies the grids effect on timeflow by another 10%%", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "zero-flux boost", "10%", "timeflow", "10%")

        tooltip.addSpacer(10f)

        tooltip.addPara("If the ship is phased, it receives an 1.5${Strings.X} increase in the soft flux dissipation and weapon recharge rate.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "1.5${Strings.X}")

        tooltip.addSpacer(10f)
        tooltip.addPara("The spatialy influenced grid is also able to hold 20%% more flux within its capacitators, furthermore this influence is carried over to the plasma generated by the ships energy weapons, increasing their damage by 10%%.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "20%", "energy", "10%")

        tooltip.addSpacer(10f)
        tooltip.addSectionHeading("Incompatibilities", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        tooltip.addNegativePara("-> Incompatible with Safety Overrides.")
        tooltip.addSpacer(3f)
        tooltip.addNegativePara("-> Incompatible with Phase Anchor.")
        tooltip.addSpacer(3f)
        tooltip.addNegativePara("-> Can not be build in to the hull.")
    }

    override fun showInRefitScreenModPickerFor(ship: ShipAPI?): Boolean {
        if (ship == null) return false
        return ship.variant.hasHullMod("rat_exogrid")
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        if (ship!!.baseOrModSpec().hullId == "rat_apheidas") return false
        if (ship!!.variant.hasHullMod(HullMods.SAFETYOVERRIDES)) return false
        if (ship!!.variant.hasHullMod(HullMods.PHASE_ANCHOR)) return false
        return super.isApplicableToShip(ship)
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String? {
        if (ship!!.baseOrModSpec().hullId == "rat_apheidas") {
            return "Can not be installed on the Apheidas"
        }
        if (ship!!.variant.hasHullMod(HullMods.SAFETYOVERRIDES)) {
            return "Incompatible with Safety Overrides"
        }
        if (ship!!.variant.hasHullMod(HullMods.PHASE_ANCHOR)) {
            return "Incompatible with Phase Anchor"
        }
        return super.getUnapplicableReason(ship)
    }

    override fun getNameColor(): Color {
        return Color(217, 164, 57)
    }

    override fun getBorderColor(): Color {
        return Color(217, 164, 57)
    }



}