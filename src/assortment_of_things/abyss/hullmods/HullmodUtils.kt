package assortment_of_things.abyss.hullmods

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.StatBonus

object HullmodUtils {

    //Idea & Mechanic from Sthurm Protocol in VIC rewritten for easier useage
    fun negateAllStatChanges(id: String, stat: MutableStat, stats: MutableShipStatsAPI) {

        var allFlats = 0f
        for (flat in stat.flatMods ) {
            allFlats += flat.value.value
        }
        stat.modifyFlat(id, -allFlats)


        var allMults = 1f
        for (mult in stat.multMods) {
            allMults *= mult.value.value
        }
        stat.modifyMult(id, 1 / allMults)


        var allPercent = 0f
        for (percent in stat.percentMods) {
            allPercent += percent.value.value
        }
        stat.modifyPercent(id, -allPercent)
    }

    fun negateStatIncreases(id: String, stat: MutableStat, stats: MutableShipStatsAPI) {

        var allFlats = 0f
        for (flat in stat.flatMods) {
            if (flat.value.value > 0 && flat.key != id) {
                allFlats += flat.value.value
            }
        }
        stat.modifyFlat(id, -allFlats)

        var allMults = 1f
        for (mult in stat.multMods) {
            if (mult.value.value > 1 && mult.key != id) {
                allMults *= mult.value.value
            }
        }
        stat.modifyMult(id, 1 / allMults)


        var allPercent = 0f
        for (percent in stat.percentMods) {
            if (percent.value.value > 0 && percent.key != id) {
                allPercent += percent.value.value
            }
        }
        stat.modifyPercent(id, -allPercent)
    }

    fun negateStatIncreases(id: String, stat: StatBonus, stats: MutableShipStatsAPI) {

        var allFlats = 0f
        for (flat in stat.flatBonuses) {
            if (flat.value.value > 0 && flat.key != id) {
                allFlats += flat.value.value
            }
        }
        stat.modifyFlat(id, -allFlats)

        var allMults = 1f
        for (mult in stat.multBonuses) {
            if (mult.value.value > 1 && mult.key != id) {
                allMults *= mult.value.value
            }
        }
        stat.modifyMult(id, 1 / allMults)


        var allPercent = 0f
        for (percent in stat.percentBonuses) {
            if (percent.value.value > 0 && percent.key != id) {
                allPercent += percent.value.value
            }
        }
        stat.modifyPercent(id, -allPercent)
    }

    fun removeNegation(id: String, stat: StatBonus, stats: MutableShipStatsAPI) {
        stat.modifyMult(id, 1f)
        stat.modifyFlat(id, 0f)
        stat.modifyPercent(id, 0f)
    }

    fun negateStatDecrease(id: String, stat: MutableStat, stats: MutableShipStatsAPI) {

        var allFlats = 0f
        for (flat in stat.flatMods) {
            if (flat.value.value < 0 && flat.key != id) {
                allFlats += flat.value.value
            }
        }
        stat.modifyFlat(id, -allFlats)

        var allMults = 1f
        for (mult in stat.multMods) {
            if (mult.value.value < 1&& mult.key != id) {
                allMults *= mult.value.value
            }
        }
        stat.modifyMult(id, 1 / allMults)

        var allPercent = 0f
        for (percent in stat.percentMods) {
            if (percent.value.value < 0 && percent.key != id) {
                allPercent += percent.value.value
            }
        }
        stat.modifyPercent(id, -allPercent)
    }

    fun negateStatDecrease(id: String, stat: StatBonus, stats: MutableShipStatsAPI) {

        var allFlats = 0f
        for (flat in stat.flatBonuses) {
            if (flat.value.value < 0) {
                allFlats += flat.value.value
            }
        }
        stat.modifyFlat(id, -allFlats)

        var allMults = 1f
        for (mult in stat.multBonuses) {
            if (mult.value.value < 1) {
                allMults *= mult.value.value
            }
        }
        stat.modifyMult(id, 1 / allMults)

        var allPercent = 0f
        for (percent in stat.percentBonuses) {
            if (percent.value.value < 0) {
                allPercent += percent.value.value
            }
        }
        stat.modifyPercent(id, -allPercent)
    }

}