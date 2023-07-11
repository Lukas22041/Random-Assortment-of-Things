package assortment_of_things.abyss.hullmods.fightersystems

import activators.ActivatorManager
import assortment_of_things.abyss.activators.MagneticStormActivator
import assortment_of_things.abyss.activators.TemporalAssaultActivator
import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils

class MagneticStormHullmod : BaseAlteration() {

    var modID = "rat_magnetic_storm"


    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {
        super.applyEffectsToFighterSpawnedByShip(fighter, ship, id)
        if (fighter == null) return
        ActivatorManager.addActivator(fighter, MagneticStormActivator(fighter))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)


    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Every fighter deployed by the ship gains the \"Magnetic Storm\" system. This is in addition to their existing system, if they have one.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(),
        "Magnetic Storm")

        tooltip.addSpacer(5f)
        tooltip.addPara("Upon activation, unleashes emp strikes from the fighters position towards nearby hostile ships and missiles.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "emp strikes", "ships and missiles")

    }

    override fun addItemPostDescription(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip!!.addPara("Can only be installed in to hulls that have atleast 1 fighter bay, bays from modifications are not accounted for.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }

    override fun alterationInstallFilter(fleet: List<FleetMemberAPI>): List<FleetMemberAPI> {
        return fleet.filter { it.hullSpec.fighterBays != 0 }
    }



}