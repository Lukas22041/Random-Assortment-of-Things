package assortment_of_things.exotech.hullmods.alteration

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.addNegativePara
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.combat.PhaseCloakStats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.util.*

class AutonomousBaysHullmod : BaseAlteration() {

    var modID = "rat_autonomous_bays"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.numFighterBays.modifyFlat(id, 1f)
        stats.dynamic.getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 0f)

    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }

    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {
        var ai = AICoreOfficerPluginImpl().createPerson(Commodities.GAMMA_CORE, Factions.PLAYER, Random())
        fighter!!.captain = ai
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("The ship is modified to deploy fighters that have been manually alterated to allow for the control from a central gamma-core AI. This negates the fighter crew loss, allows for an additional bay to be installed and provides each individual fighter with gamma-core skills.", 0f
        ,Misc.getTextColor(), Misc.getHighlightColor(), "crew loss", "additional bay", "gamma-core")

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return member!!.baseOrModSpec().fighterBays != 0
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        if (member!!.baseOrModSpec().fighterBays == 0) {
            tooltip!!.addNegativePara("Can only be installed on ships that can deploy fighters even without installing other modifications.")
        }
    }
}