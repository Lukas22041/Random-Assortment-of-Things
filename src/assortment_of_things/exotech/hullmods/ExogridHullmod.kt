package assortment_of_things.exotech.hullmods

import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ExogridHullmod : BaseHullMod() {

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {
        stats.sensorProfile.modifyMult(id, 0.5f)
        if (stats.variant.baseOrModSpec().hints.contains(ShipTypeHints.PHASE)) {
            stats.sensorProfile.modifyMult(id, 0.5f)
        }
        else {
            stats.sensorProfile.modifyMult(id, 0.75f)
        }

        stats.ventRateMult.modifyMult(id,  0.75f)
        //stats.fluxDissipation.modifyMult(id, 0.9f)

        var vents = stats.variant.numFluxVents
        var fluxDecrease = 5f * vents
        stats.fluxDissipation.modifyFlat(id, -fluxDecrease)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        Global.getCombatEngine().addLayeredRenderingPlugin(ExogridRenderer(ship))
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
    }


    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip.addSpacer(10f)

        tooltip.addPara("A unique approach at a flux grid that heavily integrates phase-components, enhancing both shipsystems and cloaks. This direct integration allows the speed of phase-cloaks to be much less effected by rising flux levels. " +
                "The grid itself also exerts forces that put the ship under a constant 10%% increase in timeflow and causes a reduction in the EMP damage taken of 25%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
        "phase-cloaks", "10%", "timeflow", "EMP damage", "25%")

        tooltip.addSpacer(10f)

        tooltip.addPara("This design has its downsides however, venting large amounts of flux with this grid creates spatial instabilities, and to avoid critical failure, the active venting rate is capped at 75%% of its maximum output. " +
                "Additional flux vents also only increase the flux dissipation by half of their normal value.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "the active venting rate is capped at 75% of its maximum output", "flux dissipation", "half")

        tooltip.addSpacer(10f)

        tooltip.addPara("Due to the use of p-space components, the ships sensor profile is decreased by 25%%. If the ship also has a functional phase-cloak, the reduction is increased to 50%%", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "25%", "50%")
    }

    override fun getNameColor(): Color {
        return Color(217, 164, 57)
    }

    override fun getBorderColor(): Color {
        return Color(217, 164, 57)
    }
}