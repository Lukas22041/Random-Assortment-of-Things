package assortment_of_things.exotech.intel.missions

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.getExoData
import assortment_of_things.misc.loadTextureCached
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.CommMessageAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.makeImportant
import java.awt.Color
import java.util.*
import kotlin.collections.LinkedHashSet

class WarpCatalystMissionIntel : BaseIntelPlugin() {

    var hideout: SectorEntityToken
    var finished = false
    var interval = IntervalUtil(0.1f, 0.1f)

    init {
        isImportant = true

        hideout = ExoUtils.getExoData().hideout!!

        Global.getSector().addScript(this)
    }

    fun finish() {
        finished = true
        Global.getSector().getExoData().finishedCurrentMission = true
        Global.getSector().getExoData().finishedWarpCatalystMission = true
        Global.getSector().campaignUI.addMessage(this, CommMessageAPI.MessageClickAction.INTEL_TAB)
    }

    override fun notifyEnded() {
        super.notifyEnded()
        Global.getSector().removeScript(this)
    }

    override fun getName(): String? {
        var name = "Exo-Defectors"
        return name
    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {

        info!!.addSpacer(2f)

        if (!finished) {
            info.addPara("Destroy the fleet and recover the Warp Catalyst", 0f, tc, Misc.getHighlightColor(), "Warp Catalyst")
        }
        else {
            info.addPara("The mission has been completed. Return to Xander", 0f, tc, Misc.getHighlightColor())
        }

    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addSpacer(10f)

        if (!finished) {

            info.addPara("Amelie has been assigned to destroy a defectors fleet and has transfered the task to you. " +
                    "The target is known to be somewhere within the ${hideout.starSystem.nameWithNoType} system, but we have no more information aside of that.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "${hideout.starSystem.nameWithNoType}")

            info.addSpacer(10f)

            info.addPara("Due to much confusion around the incident, the composition of the fleet is unknown to us.", 0f)

        } else {
            info.addPara("The mission has been fullfilled, return to Xander to finish it.", 0f)
        }

    }

    override fun getArrowData(map: SectorMapAPI?): MutableList<IntelInfoPlugin.ArrowData> {
        var list = mutableListOf<IntelInfoPlugin.ArrowData>()
        return list
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add("rat_exotech")
        tags.add(Tags.INTEL_MISSIONS)
        return tags
    }

    override fun getIcon(): String {
        var path = "graphics/icons/intel/missions/rat_warp_catalyst_mission.png"
        Global.getSettings().loadTextureCached(path)
        return path
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? {
        if (finished) return null
        return hideout.starSystem.center
    }








}