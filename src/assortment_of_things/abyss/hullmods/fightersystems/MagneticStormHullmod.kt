package assortment_of_things.abyss.hullmods.fightersystems

import activators.ActivatorManager
import assortment_of_things.abyss.activators.MagneticStormActivator
import assortment_of_things.abyss.activators.TemporalAssaultActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils

class MagneticStormHullmod : BaseHullMod() {

    var modID = "rat_magnetic_storm"


    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {
        super.applyEffectsToFighterSpawnedByShip(fighter, ship, id)
        if (fighter == null) return
        ActivatorManager.addActivator(fighter, MagneticStormActivator(fighter))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP)
        {
            stats!!.maxCombatReadiness.modifyFlat(modID, -0.10f)
        }
        else
        {
            stats!!.maxCombatReadiness.modifyFlat(modID, -0.05f)
        }

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Every fighter deployed by the ship gains the \"Magnetic Storm\" fighter-system. This is in addition to their existing system, if they have one.", 0f)
        tooltip!!.addSpacer(5f)

        tooltip.addSectionHeading("Fightersystem: Magnetic Storm", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Unleashes EMP strikes towards nearby targets, both ships and missiles for a duration of 5 seconds. After that it enters a 15 second cooldown.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "EMP strikes", "ships and missiles", "5 seconds", "15 second")
        tooltip.addSpacer(10f)

        tooltip.addPara("Hull Alterations decrease the ships maximum CR by 5%%/5%%/5%%/10%% based on its hullsize.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

    }

}