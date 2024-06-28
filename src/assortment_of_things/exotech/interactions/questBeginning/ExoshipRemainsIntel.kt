package assortment_of_things.exotech.interactions.questBeginning

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.loadTextureCached
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ExoshipRemainsIntel : BaseIntelPlugin() {

    init {
        isImportant = true
        //endImmediately()
    }
    override fun getName(): String? {
        return "Frozen in time"
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)

        var data = ExoUtils.getExoData()
        if (!data.foundExoshipRemains && data.QuestBeginning_StartedFromExoship) {
            info.addPara("Investigate the provided coordinates", 0f, tc, tc)
        }

        if (data.readyToRepairExoship) {
            info.addPara("Return to the broken exoship and innitiate the repair procedure.", 0f, tc, Misc.getHighlightColor(), "innitiate the repair procedure")
        }
        else if (data.foundExoshipRemains) {
            info.addPara("Acquire a \"Warp Catalyst\"", 0f, tc, Misc.getHighlightColor(), "Warp Catalyst")
        }
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addSpacer(10f)

        var data = ExoUtils.getExoData()
        if (!data.foundExoshipRemains && data.QuestBeginning_StartedFromExoship) {
            info.addPara("An unknown figure requested you to investigate the provided coordinates. They appear to be located within the Persean Abyss, travel towards the location may proof difficult.", 0f)
        }

        if (data.foundExoshipRemains) {
            info.addPara("You discovered an exoship floating within the persean abyss, inactive but repairable, however a \"Warp Catalyst\" is required for the finishing touches.\n\n" +
                    "There are no records within the fleets own archives about where to acquire such a device.",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "Warp Catalyst")
        }
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add("rat_exotech")
        return tags
    }

    override fun getIcon(): String {
        var path =  "graphics/icons/intel/rat_frozen_in_time.png"
        Global.getSettings().loadTextureCached(path)
        return path
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        return ExoUtils.getExoData().exoshipRemainsEntity!!
    }

}