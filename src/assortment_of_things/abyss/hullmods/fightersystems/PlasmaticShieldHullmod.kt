package assortment_of_things.abyss.hullmods.fightersystems

import activators.ActivatorManager
import assortment_of_things.abyss.activators.MagneticStormActivator
import assortment_of_things.abyss.activators.PlasmaticShieldActivator
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

class PlasmaticShieldHullmod : BaseAlteration() {

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
        tooltip!!.addPara("Every fighter deployed by the ship gains the \"Plasmatic Shield\" system. This is in addition to their existing system, if they have one.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
            "Plasmatic Shield")

        tooltip.addSpacer(5f)
        tooltip.addPara("Upon activation, the fighter gains a temporariy shield that will last for the active duration or until it is destroyed.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "temporariy shield")
    }

    override fun addItemPostDescription(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip!!.addPara("Can only be installed in to hulls that have atleast 1 fighter bay, bays from modifications are not accounted for.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }

    override fun alterationInstallFilter(fleet: List<FleetMemberAPI>): List<FleetMemberAPI> {
        return fleet.filter { it.hullSpec.fighterBays != 0 }
    }

}