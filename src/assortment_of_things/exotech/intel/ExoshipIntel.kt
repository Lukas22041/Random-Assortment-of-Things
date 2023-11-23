package assortment_of_things.exotech.intel

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ExoshipIntel(var exoship: SectorEntityToken) : BaseIntelPlugin() {

    var data = ExoUtils.getExoshipData(exoship)

    override fun getName(): String? {
        return "Exoship: ${data.name}"
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("In ${exoship.containingLocation.nameWithNoType}", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${exoship.containingLocation.nameWithNoType}")
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addSpacer(10f)
        info.addPara("The exoship ${data.name} is currently active in the ${exoship.containingLocation.nameWithNoType} system. It is scheduled to move towards its next location within ${data.getTimeTilNextMove().toInt()} days.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(),
            "${data.name}", "${exoship.containingLocation.nameWithNoType}", "${data.getTimeTilNextMove().toInt()}")
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add("rat_exotech")
        return tags
    }

    override fun getIcon(): String {
        return "graphics/icons/intel/rat_exoship_intel.png"
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        return exoship.starSystem.hyperspaceAnchor ?: exoship
    }

}