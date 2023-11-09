package assortment_of_things.relics.bar

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.adjustReputationWithPlayer
import java.awt.Color

class ArchivistIntel(var entity: SectorEntityToken, var archivist: PersonAPI) : BaseIntelPlugin() {

    var system = entity.starSystem
    var remove = false
    var entityFound = false

    override fun getName(): String? {
        return "A Relic of the Past"
    }



    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)
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

      /*  var desc = Global.getSettings().getDescription(entity.customDescriptionId, Description.Type.CUSTOM)
        if (desc != null && desc.text4 != "") {

            info.addPara("She left you with a short description of the object", 0f)

            info.addPara("\"${desc.text4}\"", 0f)

            info.addSpacer(10f)
        }*/


        if (entityFound) {
            info.addPara("The object has been confirmed to be in the system.", 0f, Misc.getGrayColor(), Misc.getGrayColor())
        }
        else {
            info.addPara("The archivist could not provide location data for this objective.", 0f, Misc.getGrayColor(), Misc.getGrayColor())
        }



        var delete = addGenericButton(info, width, "Remove Intel", "DELETE")
        delete.setShortcut(Keyboard.KEY_G, true)
    }

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        if (buttonId == "DELETE") {
            remove = true
            ui!!.recreateIntelUI()
        }
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        if (!entity.isDiscoverable && !entityFound) {

            entityFound = true

            Global.getSector().campaignUI.addMessage( object : BaseIntelPlugin() {
                override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {
                    info!!.addPara("Confirmed the existance of the ${entity.fullName}", 0f)
                }

            })
            archivist.adjustReputationWithPlayer(0.1f, null)
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
        tags.add(Tags.INTEL_ACCEPTED)
        return tags
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        if (entity != null && !entity.isExpired) return entity
        return system.center
    }

}