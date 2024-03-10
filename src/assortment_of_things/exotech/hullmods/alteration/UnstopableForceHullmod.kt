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

class UnstopableForceHullmod : BaseAlteration() {

    val missileAmmoBonus = 20f
    val missileSpeedMult = 1.33f
    var modID = "rat_unstopable_force"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.missileAmmoBonus.modifyPercent(id, missileAmmoBonus)

        stats!!.missileMaxSpeedBonus.modifyMult(id, missileSpeedMult)
        stats!!.missileAccelerationBonus.modifyMult(id, missileSpeedMult)
        stats!!.missileMaxTurnRateBonus.modifyMult(id, missileSpeedMult)
        stats!!.missileTurnAccelerationBonus.modifyMult(id, missileSpeedMult)

        stats.missileHealthBonus.modifyMult(id, 1.25f)
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }

    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Every missile weapon gains an additional 20%% to their maximum count of missiles. Missiles launched by the ship are 33%% faster and have 25%% more hitpoints.", 0f
        ,Misc.getTextColor(), Misc.getHighlightColor(), "20%", "33%", "25%")

    }


}