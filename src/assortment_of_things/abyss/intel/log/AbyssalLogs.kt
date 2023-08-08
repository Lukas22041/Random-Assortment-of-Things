package assortment_of_things.abyss.intel.log

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import kotlin.random.Random

object AbyssalLogs {

    class AbyssalLogEntry(var id: String, var name: String, var date: String)

    private var key = "\$rat_unlocked_logs"

    fun getAvailableLog(seed: Long) : AbyssalLogEntry? {

        var intel = Global.getSector().intelManager.intel.filter { it is AbyssalLogIntel }.map { (it as AbyssalLogIntel).entry.id }
        var availableLogs = logs.filter { !intel.contains(it.id) }

        if (availableLogs.isEmpty()) return null

        var random = Random(seed)
        var pick = availableLogs.random(random)

        return pick
    }

    var logs = arrayListOf<AbyssalLogEntry>(

        AbyssalLogEntry("incident", "Incident", "12. November"),

        )

}

