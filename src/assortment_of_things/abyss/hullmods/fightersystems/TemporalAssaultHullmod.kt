package assortment_of_things.abyss.hullmods.fightersystems

import assortment_of_things.abyss.activators.*
import assortment_of_things.abyss.hullmods.BaseAlteration
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.subsystems.MagicSubsystemsManager

class TemporalAssaultHullmod : BaseAlteration() {


    var modID = "rat_temporal_assault"


    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {
        super.applyEffectsToFighterSpawnedByShip(fighter, ship, id)
        if (fighter == null) return
        MagicSubsystemsManager.addSubsystemToShip(fighter, TemporalAssaultActivator(fighter))
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Every fighter deployed by the ship gains the \"Temporal Assault\" system. This is in addition to their existing system, if they have one.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
            "Temporal Assault")

        tooltip.addSpacer(5f)
        tooltip.addPara("Upon activation, the fighter experience a short-term increase in its perceived timeflow.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "perceived timeflow")
    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return variant!!.hullSpec.fighterBays != 0
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        tooltip!!.addPara("Can only be installed in to hulls that have atleast 1 fighter bay, bays from modifications are not accounted for.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }

}