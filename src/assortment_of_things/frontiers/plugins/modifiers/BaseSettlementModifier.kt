package assortment_of_things.frontiers.plugins.modifiers

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.frontiers.data.SettlementModifierSpec
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

open class BaseSettlementModifier() {

    var specId: String = ""
    lateinit var settlement: SettlementData
    lateinit var conditionId: String

    fun getSpec() : SettlementModifierSpec {
        return FrontiersUtils.getModifierByID(specId)
    }

    open fun getID() : String {
        return getSpec().id
    }

    open fun getName() : String {
        return getSpec().name
    }

    open fun getDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("${getName()}:\n${getSpec().desc}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${getName()}")
    }



    open fun getIcon() : String {
        return getSpec().icon
    }

    open fun getRefinedIcon() : String {
        return getSpec().iconRefined
    }

    open fun includeForCondition(conditionID : String) : Boolean {
        return getSpec().conditions.contains(conditionID)
    }

    open fun getChance() : Float {
        return getSpec().chance
    }

    open fun apply() {

    }

    open fun unapply() {

    }

    open fun advance(amount: Float) {

    }

    open fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {
        return null
    }

    open fun getTier() : Int {
        return getSpec().tiers.get(conditionId)!!
    }

    open fun reportEconomyTick(iterIndex: Int) {

    }

    open fun reportEconomyMonthEnd() {

    }


}