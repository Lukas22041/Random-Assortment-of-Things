package assortment_of_things.campaign.intel

import assortment_of_things.misc.RATEntities
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class BlackmarketWarningBeaconIntel(var beacon: SectorEntityToken) : BaseIntelPlugin() {

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)

        val c = getTitleColor(mode)
        val tc = getBulletColorForMode(mode)
        info.addPara("Damaged Warning Beacon", c, 0f)

        bullet(info)

        info.addPara("Danger level: ???", 0f, tc, tc)

        unindent(info)
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {

        val desc = Global.getSettings().getDescription(RATEntities.BLACKMARKET_WARNING_BEACON, Description.Type.CUSTOM)
        info!!.addPara(desc.text1FirstPara, 0f)

        if (beacon.isInHyperspace) {
            val system = Misc.getNearbyStarSystem(beacon, 1f)
            if (system != null) {
                info.addPara("\nThis beacon is located near the " + system.nameWithLowercaseType + ", warning of dangers that presumably lie within.", 0f)
            }
        }
    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("intel", "beacon_blackmarket")
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags = super.getIntelTags(map)
        tags.add(Tags.INTEL_BEACON)

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