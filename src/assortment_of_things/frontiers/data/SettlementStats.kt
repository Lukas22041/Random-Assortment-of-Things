package assortment_of_things.frontiers.data

import com.fs.starfarer.api.combat.MutableStat

class SettlementStats {

    var income = MutableStat(5000f)
    var incomeMod = MutableStat(1f)

    var productionBudget = MutableStat(250000f)
    var productionBudgetPerMonth = MutableStat(25000f)
    var productionQuality = MutableStat(1f)

}