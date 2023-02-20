package assortment_of_things.campaign.intel

import assortment_of_things.misc.RATEntities
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

class OutpostWarningBeaconIntel(var beacon: SectorEntityToken, var faction: FactionAPI) : BaseIntelPlugin() {

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        super.createIntelInfo(info, mode)

        val c = getTitleColor(mode)
        val tc = getBulletColorForMode(mode)
        info.addPara("Warning Beacon", c, 0f)

        bullet(info)

        info.addPara("Danger level: active ${faction.displayName.lowercase()} fleets", 0f, tc, faction.color, "active ${faction.displayName.lowercase()} fleets")

        unindent(info)
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {

        val desc = Global.getSettings().getDescription(RATEntities.OUTPOST_WARNING_BEACON, Description.Type.CUSTOM)
        info!!.addPara(desc.text1FirstPara, 0f)

        var label = info.addPara("The beacon identifies the system as under ${faction.displayName} control and advises anyone to turn away.\n\nFleets in this system are likely hostile, even towards some of their closest allies.", 0f);
        label.setHighlight(faction.getDisplayName())
        label.setHighlightColors(faction.getBaseUIColor())

        if (beacon.isInHyperspace) {
            val system = Misc.getNearbyStarSystem(beacon, 1f)
            if (system != null) {
                info.addPara("\nThis beacon is located near the " + system.nameWithLowercaseType + "", 0f)
            }
        }
    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("intel", "beacon_outpost")
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