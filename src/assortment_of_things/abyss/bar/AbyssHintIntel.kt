package assortment_of_things.abyss.bar

import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.adjustReputationWithPlayer
import java.awt.Color

class AbyssHintIntel() : BaseIntelPlugin() {


    override fun getName(): String? {
        return "The Abyssal Depths"
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("Hyperspace Phenonema in the south-western sector.", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "")
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {

        info.addSpacer(10f)

        info.addPara("You've been informed of a unique zone of hyperspatial topography within the persean abyss of the south-western sector. Not much is known of it, other than the fact that there may be many untouched domain-era artifacts laying around.")

    }


    override fun advance(amount: Float) {
        super.advance(amount)

    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()

        Global.getSector().removeScript(this)
    }

    override fun shouldRemoveIntel(): Boolean {
        return false
    }

    override fun getIcon(): String? {
        return Global.getSettings().getSpriteName("intel", "fleet_log")
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? {
        return Global.getSector().hyperspace.jumpPoints.find { it.hasTag("rat_abyss_entrance") }
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add("Abyssal Depths")
        return tags
    }
}