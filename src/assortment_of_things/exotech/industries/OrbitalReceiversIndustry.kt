package assortment_of_things.exotech.industries

import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class OrbitalReceiversIndustry : BaseIndustry() {


    override fun apply() {
        if (isFunctional) {

            getSupply(Commodities.FUEL).quantity.modifyFlat(id, MathUtils.clamp(market.size - 1f, 0f, 10f), spec.name)
            getSupply(Commodities.VOLATILES).quantity.modifyFlat(id, MathUtils.clamp(market.size - 3f, 0f, 10f), spec.name)

            market.accessibilityMod.modifyFlat(id, 0.2f, spec.name)
            market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1.5f, spec.name)

            market.stats.dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(id, 1f, spec.name)
            market.stats.dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(id, 1f, spec.name)
            market.stats.dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(id, 1f, spec.name)
        }
        else {
            unmodify()
        }
    }

    override fun unapply() {
        unmodify()
    }

    fun unmodify() {
        getSupply(Commodities.FUEL).quantity.unmodifyFlat(id)
        getSupply(Commodities.VOLATILES).quantity.unmodifyFlat(id)

        market.accessibilityMod.unmodifyFlat(id)
        market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id)

        market.stats.dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(id)
        market.stats.dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(id)
        market.stats.dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(id)
    }

    override fun addPostDemandSection(tooltip: TooltipMakerAPI?, hasDemand: Boolean, mode: Industry.IndustryTooltipMode?) {
        tooltip!!.addSpacer(10f)
        tooltip.addPara("+20%% accessibility", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+20%")
        tooltip.addSpacer(10f)
        tooltip.addPara("+1 maximum amount of patrol fleets of each size.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+1")
        tooltip.addSpacer(10f)
        tooltip.addPara("+1.5x ground defense strength.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+1.5x")

    }

    override fun isAvailableToBuild(): Boolean {
        return false
    }

    override fun showWhenUnavailable(): Boolean {
        return false
    }

    override fun canShutDown(): Boolean {
        return false
    }

    override fun showShutDown(): Boolean {
        return true
    }

    override fun getCanNotShutDownReason(): String {
        return "This structure can not be shut down."
    }

    override fun canInstallAICores(): Boolean {
        return false
    }


}