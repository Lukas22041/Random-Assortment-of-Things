package assortment_of_things.exotech.hullmods.alteration

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.addNegativePara
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
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

class OvertunedTargetingHullmod : BaseAlteration() {

    var modID = "rat_overtuned_targeting"

    var extraRange = mapOf(
        HullSize.FRIGATE to 100f,
        HullSize.DESTROYER to 150f,
        HullSize.CRUISER to 200f,
        HullSize.CAPITAL_SHIP to 250f)

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.energyWeaponRangeBonus.modifyFlat(id, extraRange.get(hullSize)!!)
        stats!!.ballisticWeaponRangeBonus.modifyFlat(id, extraRange.get(hullSize)!!)

        stats.energyWeaponDamageMult.modifyMult(id, 0.85f)
        stats.ballisticWeaponDamageMult.modifyMult(id, 0.85f)
    }


    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Increases the range of energy & ballistic weapons by 100/150/200/250, depending on hullsize. This modification however causes the weapons to be unable to more accurately hit vital targets, decreasing damage dealt by 15%%.", 0f
        ,Misc.getTextColor(), Misc.getHighlightColor(), "100", "150", "200", "250", "15%")

    }

}