package assortment_of_things.abyss.intel

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.strings.RATEntities
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssWarningBeaconIntel(var beacon: SectorEntityToken, var faction: FactionAPI) : BaseIntelPlugin() {

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)

        val c = getTitleColor(mode)
        val tc = getBulletColorForMode(mode)
        info.addPara("Warning Beacon", c, 0f)

        bullet(info)

        info.addPara("Danger level: High", 0f, tc, AbyssUtils.ABYSS_COLOR, "High")

        unindent(info)
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {

        val desc = Global.getSettings().getDescription("rat_abyss_warning_beacon", Description.Type.CUSTOM)
        info!!.addPara(desc.text1FirstPara, 0f)

    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("rat_intel", "abyss_warning_beacon")
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags = super.getIntelTags(map)
        tags.add(Tags.INTEL_BEACON)
        tags.add("Abyssal Depths")

        return tags
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        return beacon
    }

    override fun shouldRemoveIntel(): Boolean {
        return false
    }

    override fun getCommMessageSound(): String? {
        return "ui_discovered_entity"
    }
}