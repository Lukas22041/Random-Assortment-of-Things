package assortment_of_things.relics.conditions

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.getMaxIndustries
import org.magiclib.kotlin.getNumIndustries

class EngineeredUtopia : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {
        var industry = market?.industries?.find { it.spec.hasTag("farming") }

        if (market.getNumIndustries() < market.getMaxIndustries()) {

            market.hazard.modifyFlat(id, -0.25f, condition.name)
            market.accessibilityMod.modifyFlat(id, 0.25f, condition.name)

            if (industry != null) {
                if (industry.isFunctional) {
                    industry.supply(id, Commodities.FOOD, 2, condition.name)
                }
                else {
                    industry.getSupply(Commodities.FOOD).quantity.unmodifyFlat(id)
                }
            }
        }
        else {
            market.hazard.unmodifyFlat(id)
            market.accessibilityMod.unmodifyFlat(id)

            if (industry != null) {
                industry.getSupply(Commodities.FOOD).quantity.unmodifyFlat(id)
            }
        }
    }

    override fun unapply(id: String?) {
        market.stats.dynamic.getMod(Stats.MAX_INDUSTRIES).unmodify(id)
    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        var base = 0 + market.size

        tooltip!!.addSpacer(10f)
        tooltip.addPara("This planet was once heavily terraformed to get it in to a better state. Much of the effort hasnt been able to survive past the collapse, but the mechanisms that are still active improve the conditons of this planet drasticly compared to less fortunate planets." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "")
        tooltip.addSpacer(10f)

        tooltip.addPara("However, the ecosystem is in a tight balance and any large scale industrial effort risks to disrupt it. The bonuses below are only active if the amount of industries is below the maximum that can be build.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "the amount of industries is below the maximum")

        tooltip.addSpacer(10f)
        tooltip.addPara("+25%% accessibility", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+25%")
        tooltip.addPara("-25%% hazard rating", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "-25%")
        tooltip.addPara("+2 food production (Farming)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+2")

    }

}