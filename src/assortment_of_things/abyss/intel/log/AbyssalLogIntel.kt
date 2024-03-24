package assortment_of_things.abyss.intel.log

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement

class AbyssalLogIntel(var descriptionId: String) : BaseIntelPlugin() {


    fun getTitle() : String {
        return Global.getSettings().getDescription(descriptionId, Description.Type.CUSTOM).text1
    }

    fun getText() : String {
        return Global.getSettings().getDescription(descriptionId, Description.Type.CUSTOM).text2
    }

    override fun getName(): String {
        return "Log: " + getTitle()
    }

    override fun hasLargeDescription(): Boolean {
        return false
    }

    override fun hasSmallDescription(): Boolean {
        return true
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {

        info!!.addSpacer(10f)

        info.addPara("\"${getText()}\"", 0f)

        info.addSpacer(10f)

    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Abyssal Depths")
    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("intel", "fleet_log")
    }
}