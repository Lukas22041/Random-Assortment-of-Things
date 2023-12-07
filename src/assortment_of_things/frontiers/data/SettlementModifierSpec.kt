package assortment_of_things.frontiers.data

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
    var plugin: String)