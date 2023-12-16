package assortment_of_things.frontiers.data

data class SettlementFacilitySpec(
    var id: String,
    var name: String,
    var buildTime: Int,
    var shortDesc: String,
    var icon: String,
    var cost: Float,
    var plugin: String)