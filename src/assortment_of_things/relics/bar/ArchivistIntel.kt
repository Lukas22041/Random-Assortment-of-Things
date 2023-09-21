package assortment_of_things.relics.bar

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.adjustReputationWithPlayer
import java.awt.Color
import java.util.LinkedHashSet

class ArchivistIntel(var entity: SectorEntityToken, var archivist: PersonAPI) : BaseIntelPlugin() {

    var system = entity.starSystem
    var remove = false

    override fun getName(): String? {
        return "A Relic of the Past"
    }



    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addPara("${entity.name}", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${entity.name}")
        info!!.addPara("In ${system.name}", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${system.name}")
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {

        info.addSpacer(10f)

        var img = info.beginImageWithText(archivist.portraitSprite, 64f)
        img.addPara("${archivist.nameString} has asked you to look for a ${entity.name} in the ${system.nameWithNoType} system. " +
                "${archivist.heOrShe.capitalize()} could not offer pay, but has set no restriction on what you end up doing with the structure.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
        "${archivist.nameString}", "${entity.name}", "${system.nameWithNoType}")

        info.addImageWithText(0f)

        info.addSpacer(5f)
        info.addRelationshipBar(archivist, info.widthSoFar, 0f)

        info.addSpacer(10f)

        info.addPara("The archivist could not provide location data for this objective.", 0f, Misc.getGrayColor(), Misc.getGrayColor())
    }


    override fun advance(amount: Float) {
        super.advance(amount)

        if (!entity.isDiscoverable) {

            Global.getSector().campaignUI.addMessage( object : BaseIntelPlugin() {
                override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {
                    info!!.addPara("Confirmed the existance of the ${entity.fullName}", 0f)
                }

            })
            archivist.adjustReputationWithPlayer(0.1f, null)

            remove = true
        }

        if (entity.isExpired) {
            remove = true
        }
    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()

        Global.getSector().removeScript(this)
    }

    override fun shouldRemoveIntel(): Boolean {
        return remove
    }

    override fun getIcon(): String? {

        return "graphics/icons/intel/rat_archivist_intel.png"
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add(Tags.INTEL_EXPLORATION)
        return tags
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        if (entity != null && !entity.isExpired) return entity
        return system.center
    }

}