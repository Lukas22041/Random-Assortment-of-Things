package assortment_of_things.relics.conditions

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AncientMilitaryNexusCondition : BaseMarketConditionPlugin() {




    override fun apply(id: String?) {

        var hasDefenseStructure = market?.industries?.any { it.isFunctional && (it.spec.hasTag("grounddefenses")) }
        if (hasDefenseStructure != null && hasDefenseStructure) {
            market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1.3f, condition.name)
        }
        else {
            market.stats.dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id)
        }

        var hasMilitaryStructure = market?.industries?.any { it.isFunctional && (it.spec.hasTag("patrol") || it.spec.hasTag("military") ||  it.spec.hasTag("command")) }
        if (hasMilitaryStructure != null && hasMilitaryStructure) {
            market.stats.dynamic.getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, 0.25f, condition.name)
        }
        else {
            market.stats.dynamic.getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(id)
        }
    }

    override fun unapply(id: String?) {

    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {

        tooltip!!.addSpacer(10f)
        tooltip.addPara(
            "Military might was once what defined this planet, until that military itself collapsed. Despite this, much of the infrastructure remains re-useable for a new military to make use of.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
        )
        tooltip.addSpacer(10f)

        tooltip.addPara("x1.3 defense rating if a defensive structure is present", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "x1.3")
        tooltip.addPara("+25%% fleet size if a military structure is present", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+25%")

    }

}