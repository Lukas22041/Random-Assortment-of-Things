package assortment_of_things.frontiers.data

import com.fs.starfarer.api.campaign.econ.MarketConditionAPI

data class SettlementModifierSpec(
    var id: String,
    var name: String,
    var desc: String,
    var isResource: Boolean,
    var canBeRefined: Boolean,
    var icon: String,
    var iconRefined: String,
    var iconSil: String,
    var chance: Float,
    var conditions: Map<String, Int>,
    var tiers: Map<String, Int>,
    var plugin: String) {

    fun getIncomeForCondition(marketConditions: List<MarketConditionAPI>) : Int {
        var income = conditions.get(marketConditions.find { conditions.contains(it.id) }?.id)
        if (income == null) {
            income = 0
        }
        return income
    }
}