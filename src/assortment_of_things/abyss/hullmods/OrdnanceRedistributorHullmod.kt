package assortment_of_things.abyss.hullmods

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.hullmods.HeavyBallisticsIntegration
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class OrdnanceRedistributorHullmod : BaseHullMod() {

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.dynamic.getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -1f)
        stats!!.dynamic.getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -2f)
        stats!!.dynamic.getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -3f)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Decreases the ordnance cost of energy weapons by 1/2/3 based on mount size.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "ordnance cost", "energy weapons", "1/2/3")

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun affectsOPCosts(): Boolean {
        return true
    }

}