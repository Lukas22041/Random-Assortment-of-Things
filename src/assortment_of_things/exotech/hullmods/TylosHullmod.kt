package assortment_of_things.exotech.hullmods

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class TylosHullmod : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {
        stats.suppliesPerMonth.modifyMult(id, 1.5f)
        stats.cargoMod.modifyMult(id, 1.5f)
        stats.missileAmmoBonus.modifyPercent(id, 50f)
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip!!.addSpacer(10f)

        tooltip!!.addPara("The ship is in a constant state of superposition. Most compartments exist in two states, which can be toggled between. " +
                "This expands the total storage volume, increasing both cargo and missile capacity by 50%%. " +
                "\n\n" +
                "The need for additional components between states requires an excessive amount of supplies, increasing the monthly supply cost by 50%%." +
                "", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "50%", "50%")

        tooltip.addSpacer(10f)
        tooltip.addSectionHeading("Superposition", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        tooltip.addPara("Activating the shipsystem toggles the ship between its two states. Each state can have different hullmods and weapons equiped. " +
                "\n\n" +
                "When changing between states, the percentage of current hitpoints and flux are shared, The armor for both states is seperate. " +
                "\n\n" +
                "The inactive state is essentialy frozen in time, preventing the recharge of ammunition and weapon cooldowns.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "percentage", "recharge")

        tooltip.addSpacer(10f)
        tooltip.addSectionHeading("AI", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        tooltip.addPara("The AI will attempt to switch towards the currently most suitable state. " +
                "If one state is build for kinetic, and one is build for explosive damage, the AI changes to the most effective state at the moment.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "kinetic", "explosive")
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun getNameColor(): Color {
        return Color(217, 164, 57)
    }

    override fun getBorderColor(): Color {
        return Color(217, 164, 57)
    }
}