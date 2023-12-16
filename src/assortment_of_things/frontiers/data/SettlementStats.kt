package assortment_of_things.frontiers.data

import com.fs.starfarer.api.combat.MutableStat

class SettlementStats {



    var income = MutableStat(5000f)

    var productionBudget = MutableStat(250000f)
    var productionBudgetPerMonth = MutableStat(25000f)
    var productionQuality = MutableStat(1f)

    private var dynamic = HashMap<String, MutableStat>()

    fun getDynamic(key: String, default: Float) : MutableStat {
        var stat: MutableStat? = dynamic.get(key)
        if (stat == null) {
            stat = MutableStat(default)
            dynamic.put(key, stat)
        }
        return stat
    }
}