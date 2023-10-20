package assortment_of_things.relics.conditions

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class BionicPlantlifeCondition : BaseMarketConditionPlugin() {


    private var foodSupply = 2f
    private var organicDemand = 1f

    override fun apply(id: String?) {
        var industry = market?.industries?.find { it.spec.hasTag("farming") } ?: return

        if (industry.isFunctional) {
            industry.getSupply(Commodities.FOOD).quantity.modifyFlat(id, foodSupply, condition.name)
            industry.getDemand(Commodities.ORGANICS).quantity.modifyFlat(id, organicDemand, condition.name)

        }
        else {
            industry.getDemand(Commodities.ORGANICS).quantity.unmodifyFlat(id)
        }
    }

    override fun unapply(id: String?) {
    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {

        tooltip!!.addSpacer(10f)
        tooltip.addPara("To accelerate the growth of local flora, it has once been biongineered to feed the planets population. While this was successful, this causes large-scale farming to be impossible without supplying additional resources to the ecosystem." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "")
        tooltip.addSpacer(10f)


        tooltip.addPara("+${foodSupply.toInt()} food production (Farming)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+${foodSupply.toInt()}")
        tooltip.addPara("+${organicDemand.toInt()} organics demand (Farming)", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+${organicDemand.toInt()}")

    }

}