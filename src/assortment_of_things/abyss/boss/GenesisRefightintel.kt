package assortment_of_things.abyss.boss

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.addPara
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

class GenesisRefightintel() : BaseIntelPlugin() {


    override fun getName(): String? {
        return "Primordial Memory"
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)
        info!!.addPara("Relive a past experience", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "")
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {

        info.addSpacer(10f)

        info.addPara("A memory of an unforgetable encounter.", 0f)

        info.addSpacer(5f)

        var delete = addGenericButton(info, width, "Recall", "RECALL")
        delete.setShortcut(Keyboard.KEY_G, true)
    }

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        if (buttonId == "RECALL") {

            Global.getSector().campaignUI.showCoreUITab(null)
            //ui!!.showDialog(Global.getSector().playerFleet, GenesisReencounterInteractionPlugin())


        }
    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }


    override fun shouldRemoveIntel(): Boolean {
        return false
    }

    override fun getIcon(): String? {
        return "graphics/portraits/cores/rat_primordial_core.png"
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        return mutableSetOf("Abyssal Depths")
    }
}