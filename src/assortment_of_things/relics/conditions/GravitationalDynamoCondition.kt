package assortment_of_things.relics.conditions

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class GravitationalDynamoCondition : BaseMarketConditionPlugin(), MarketImmigrationModifier {

    override fun apply(id: String?) {
        if (isDynamoActive()) {
            market.hazard.modifyFlat(id, -0.50f, condition.name)
            market.addTransientImmigrationModifier(this)
        }
        else {
            market.hazard.unmodify(id)
            market.removeTransientImmigrationModifier(this)
        }
    }

    override fun modifyIncoming(market: MarketAPI?, incoming: PopulationComposition) {
        incoming.weight.modifyFlat(modId, getImmigrationBonus(), Misc.ucFirst(condition.name.lowercase()))
        incoming.add(Factions.TRITACHYON, 5f)
    }

    override fun unapply(id: String?) {
        market.hazard.unmodify(id)
        market.removeTransientImmigrationModifier(this)
    }

    fun getImmigrationBonus(): Float {
        return Math.max(0, market.size - 2).toFloat()
    }

    override fun isTransient(): Boolean {
        return true
    }

    fun isDynamoActive() : Boolean {
        try {
            var dynamo = market?.starSystem?.customEntities?.find { it.customEntityType == "rat_gravitational_dynamo" } ?: return false
            if (dynamo.hasTag("rat_dynamo_active")) return true
        } catch (e: Throwable) {}
        return false
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {

        var color = market.textColorForFactionOrPlanet
        tooltip!!.addTitle(condition.getName(), color)
        tooltip.addSpacer(10f)

        tooltip.addPara("This planet is located in a system with a Gravitational Dynamo, a Megastructure that can extract energy from gravitational phenonema of the local blackhole. \n\n" +
                "The Planet has the necessary infrastructure to receive energy redirected from one at a moments notice.", 0f)

        tooltip.addSpacer(10f)

        if (isDynamoActive()) {
            tooltip.addPara("The Dynamo is actively sending power to this planet, reducing its hazard by 50%% and increasing population growth by ${getImmigrationBonus()} (based on colony size).", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "50%", "${getImmigrationBonus()}")
        }
        else {
            tooltip.addPara("The receivers sit idly as no power is being transmitted.", 0f)
        }

        tooltip.addSpacer(10f)

    }
}