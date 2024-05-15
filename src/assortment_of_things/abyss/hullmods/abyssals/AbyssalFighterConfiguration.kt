package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement

class AbyssalFighterConfiguration : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats!!.dynamic.getMod(Stats.FIGHTER_COST_MOD).modifyMult("rat_seraph_fighter", 0.5f)
        stats!!.dynamic.getMod(Stats.INTERCEPTOR_COST_MOD).modifyMult("rat_seraph_fighter", 0.5f)
        stats!!.dynamic.getMod(Stats.SUPPORT_COST_MOD).modifyMult("rat_seraph_fighter", 0.5f)
    }

    override fun affectsOPCosts(): Boolean {
        return true
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  width: Float, isForModSpec: Boolean) {

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight)
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip.addSpacer(5f)
        tooltip.addPara("Reduces the ordnance point cost of all fighters (excluding bombers) by 50%%.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "ordnance point","excluding bombers", "50%")


        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
        }
    }

    override fun getDisplaySortOrder(): Int {
        return 3
    }


    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }
}