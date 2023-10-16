package assortment_of_things.relics.conditions

import assortment_of_things.relics.conditions.scripts.DefensiveDronesFleetMananger
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class DefensiveDronesCondition : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {
        try {
            if (market.primaryEntity != null) {
                var entity = market.primaryEntity
                if (!entity.hasScriptOfClass(DefensiveDronesFleetMananger::class.java)) {
                    entity.addScript(DefensiveDronesFleetMananger(entity, 1000f, 90f, 2, 3))
                }
            }
        } catch (e: Throwable) {}

        var industry = market?.industries?.find { it.spec.hasTag("population") } ?: return

        if (industry.isFunctional) {
            industry.getDemand(Commodities.SHIPS).quantity.modifyFlat(id, 1f, condition.name)
        }
        else {
            industry.getDemand(Commodities.SHIPS).quantity.unmodify(id)
        }
    }

    override fun unapply(id: String?) {

    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
      /*  var color = market.textColorForFactionOrPlanet
        tooltip!!.addTitle(condition.getName(), color)*/
        var base = 0 + market.size

        tooltip!!.addSpacer(10f)
        tooltip.addPara("This planet hosts several autonomous launch platforms for defensive drones. Despite their age, they are still functional, even if severely weaker than in their prime.\n\n" +
                "Automaticly launches up to 3 derelict drone fleets to protect the planet and system. The strength of the fleets depends on the planets fleet size multiplier." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "3", "derelict", "fleet size")
        tooltip.addSpacer(10f)
        tooltip.addPara("+1 demand for ship hulls and weapons", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+1")



    }

}