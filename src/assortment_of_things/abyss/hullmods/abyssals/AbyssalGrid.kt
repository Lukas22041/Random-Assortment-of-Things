package assortment_of_things.abyss.hullmods.abyssals

import com.fs.starfarer.api.campaign.CampaignUIAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssalGrid : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {


        stats!!.energyWeaponFluxCostMod.modifyMult(id, 0.9f)
        stats!!.energyWeaponRangeBonus.modifyFlat(id, 100f)
        //stats!!.beamWeaponRangeBonus.modifyMult(id, 100f)

        stats!!.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 0f);
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  width: Float, isForModSpec: Boolean) {
        tooltip!!.addSpacer(5f)
        tooltip.addPara("This ships flux grid is highly optimised for the use of energy weapons. " +
                "This allows them to operate at a 10%% lower flux cost than normal and increases their base range by 100 units. " +
                "\n\n" +
                "This unique grid can also absorb the impact of abyssal storms and similar hazards, providing the ship full immunity against those." +
                "",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "energy weapons", "10%", "100", "abyssal storms")
    }




    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }
}