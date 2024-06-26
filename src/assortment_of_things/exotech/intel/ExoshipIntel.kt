package assortment_of_things.exotech.intel

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class ExoshipIntel(var exoship: SectorEntityToken, var temporary: Boolean = false) : BaseIntelPlugin() {


    init {
        isImportant = true
        if (temporary) {
            endAfterDelay(MathUtils.getRandomNumberInRange(45f, 70f))
        }
        Global.getSector().addScript(this)
    }

    override fun notifyEnded() {
        super.notifyEnded()
        Global.getSector().removeScript(this)
    }

    override fun getName(): String? {
        var name = "Daybreak"
        if (temporary) name += " (Temporary)"
        return name
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {

        var data = ExoUtils.getExoData().getExoshipPlugin()
        var warp = data.npcModule.currentWarp

        info!!.addSpacer(2f)
        info!!.addPara("In ${exoship.containingLocation.nameWithNoType}", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${exoship.containingLocation.nameWithNoType}")

        if (warp != null) {
            info!!.addPara("Plans to relocate in ${warp.daysTilWarp.toInt()} days", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${exoship.containingLocation.nameWithNoType}",
            "${warp.daysTilWarp.toInt()}")
        }

        if (temporary) {
            info.addPara("Intel expires in ${endingTimeRemaining.toInt()} days", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${endingTimeRemaining.toInt()}")
        }
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addSpacer(10f)


        if (temporary) {
            info.addPara("This piece of intel has been acquired from a single beacon, and after ${endingTimeRemaining.toInt()} days its data will become outdated, expiring this piece of information.",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "${endingTimeRemaining.toInt()}")
        }
        else {
            info.addPara("Your fleet managed to connect to remote and scattered navigation network belonging to the Exotech faction. " +
                    "These enable predicting the current location and future destinations of their own Exoship.", 0f)
        }



        info.addSpacer(10f)
        info.addSectionHeading("Data", Alignment.MID, 0f)
        info.addSpacer(10f)

        var data = ExoUtils.getExoData().getExoshipPlugin()

        if (data.isInTransit) {
            info.addPara("The exoship ${exoship.name} is currently in transit towards the ${data.npcModule.currentWarp!!.destination.starSystem.nameWithNoType} system. " +
                    "", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(),
                "${exoship.name}", "${data.npcModule.currentWarp!!.destination.starSystem.nameWithNoType}")
        }

        else if (data.npcModule.currentWarp == null) {
            info.addPara("The exoship ${exoship.name} is currently active in the ${exoship.containingLocation.nameWithNoType} system. " +
                    "There is no information about its next location at the moment.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(),
                "${exoship.name}", "${exoship.containingLocation.nameWithNoType}")
        }

        else {
            info.addPara("The exoship ${exoship.name} is currently active in the ${exoship.containingLocation.nameWithNoType} system. " +
                    "It will begin its warp towards the ${data.npcModule.currentWarp!!.destination.starSystem.name} system in ${data.npcModule.currentWarp!!.daysTilWarp.toInt()} days.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(),
                "${exoship.name}", "${exoship.containingLocation.nameWithNoType}", "${data.npcModule.currentWarp!!.destination.starSystem.name}", "${data.npcModule.currentWarp!!.daysTilWarp.toInt()}")
        }



    }

    override fun getArrowData(map: SectorMapAPI?): MutableList<IntelInfoPlugin.ArrowData> {
        var data = ExoUtils.getExoData()

        var list = mutableListOf<ArrowData>()
        var warp = data.getExoshipPlugin().npcModule.currentWarp

        if (warp != null) {
            var arrow = ArrowData(data.getExoship().starSystem.center, warp.destination)
            list.add(arrow)
        }

        return list
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
