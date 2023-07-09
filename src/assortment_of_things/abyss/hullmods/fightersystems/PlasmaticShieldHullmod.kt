package assortment_of_things.abyss.hullmods.fightersystems

import activators.ActivatorManager
import assortment_of_things.abyss.activators.MagneticStormActivator
import assortment_of_things.abyss.activators.PlasmaticShieldActivator
import assortment_of_things.abyss.activators.TemporalAssaultActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils

class PlasmaticShieldHullmod : BaseHullMod() {

    var modID = "rat_plasmatic_shield"


    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {
        super.applyEffectsToFighterSpawnedByShip(fighter, ship, id)
        if (fighter == null) return
        ActivatorManager.addActivator(fighter, PlasmaticShieldActivator(fighter))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Every fighter deployed by the ship gains the \"Plasmatic Shield\" fighter-system. This is in addition to their existing system, if they have one.", 0f)
        tooltip!!.addSpacer(5f)

        tooltip.addSectionHeading("Fightersystem: Plasmatic Shield", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Generates a temporary shield around the fighter. After activation it enters a 15 second cooldown.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "temporary shield", "15 second")

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }
    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }

}