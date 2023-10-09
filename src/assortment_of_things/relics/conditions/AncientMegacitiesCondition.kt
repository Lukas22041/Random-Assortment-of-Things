package assortment_of_things.relics.conditions

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AncientMegacitiesCondition : BaseMarketConditionPlugin(), MarketImmigrationModifier {

    override fun apply(id: String?) {
        market.addTransientImmigrationModifier(this)
    }

    override fun modifyIncoming(market: MarketAPI?, incoming: PopulationComposition) {
        incoming.weight.modifyFlat(modId, getImmigrationBonus(), Misc.ucFirst(condition.name.lowercase()))
        incoming.add(Factions.TRITACHYON, 5f)
    }

    override fun unapply(id: String?) {
        market.removeTransientImmigrationModifier(this)
    }

    fun getImmigrationBonus(): Float {
        return Math.max(0, market.size - 2).toFloat()
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {

        var color = market.textColorForFactionOrPlanet
        tooltip!!.addTitle(condition.getName(), color)
        tooltip.addSpacer(10f)

        tooltip.addPara("Large and abandoned Megacities span across this world, ready to be inhabited once again.", 0f)

        tooltip.addSpacer(10f)

        tooltip.addPara("+${getImmigrationBonus().toInt()} population growth (based on colony size).", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "+${getImmigrationBonus().toInt()}")



    }
}