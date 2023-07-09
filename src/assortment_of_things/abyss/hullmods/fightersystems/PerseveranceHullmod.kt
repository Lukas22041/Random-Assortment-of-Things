package assortment_of_things.abyss.hullmods.fightersystems

import activators.ActivatorManager
import assortment_of_things.abyss.activators.PerseveranceActivator
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class PerseveranceHullmod : BaseHullMod() {


    var modID = "rat_perseverance"


    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {
        super.applyEffectsToFighterSpawnedByShip(fighter, ship, id)
        if (fighter == null) return
        ActivatorManager.addActivator(fighter, PerseveranceActivator(fighter))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Every fighter deployed by the ship gains the \"Perseverance\" fighter-system. This is in addition to their existing system, if they have one.", 0f)
        tooltip!!.addSpacer(5f)

        tooltip.addSectionHeading("Fightersystem: Perseverance", Alignment.MID, 0f)
        tooltip.addSpacer(5f)
        tooltip.addPara("Decreases all damage received by the fighter by 70%% for 8 seconds, afterwards this system goes on a 15 second cooldown.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "damage received", "70%", "8 seconds", "15 second")


    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }
    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Alterations can only be installed through the associated item."
    }

}