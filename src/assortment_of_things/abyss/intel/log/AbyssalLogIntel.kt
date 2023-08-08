package assortment_of_things.abyss.intel.log

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

class AbyssalLogIntel(var entry: AbyssalLogs.AbyssalLogEntry) : BaseIntelPlugin() {

    override fun getName(): String {
        return "Log: " + entry.name
    }

    override fun hasLargeDescription(): Boolean {
        return false
    }

    override fun hasSmallDescription(): Boolean {
        return true
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {

        info!!.addSpacer(10f)

        var text = Global.getSettings().loadText("data/strings/abyss/logs/${entry.id}.txt")

        info.addSpacer(5f)

        info.addPara("Author: Undocumented", 0f)
        info.addPara("Date: ${entry.date} | unknown cycle", 0f)

        info.addSpacer(10f)

        info.addPara(text, 0f)

    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Abyss")
    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("intel", "fleet_log")
    }
}