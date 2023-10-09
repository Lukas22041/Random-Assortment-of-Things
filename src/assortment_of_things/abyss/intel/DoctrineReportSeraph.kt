package assortment_of_things.abyss.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class DoctrineReportSeraph() : BaseIntelPlugin() {


    override fun getName(): String? {
        return "Doctrine Report"
    }


    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)

        info.addPara("Seraph Doctrine", 0f, Color(196, 20, 35,255), Color(196, 20, 35,255))

    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {

        info.addSpacer(5f)
        info.addPara("Over multiple encounters with the unknown threat, the fleets intelligence office was able to construct a comprehensive list of notes specifying details of the opponent." +
                "This document portraits details about the \"Seraph\" doctrine.", 0f, Misc.getTextColor(), Color(196, 20, 35,255), "Seraph")
        info.addSpacer(10f)

        info.addSectionHeading("Details", Alignment.MID, 0f)
        info.addSpacer(10f)

        info.addPara("This type of ship appears to be more of a \"backup\" for abyssal fleets. It appears in few numbers within the abyssal fleets. Despite this, ships of this type are not to be underestimated.\n\n" +
                "They boast a more balanced loadout, having both more armor themself and more tools to deal with armor than their counterpart. ", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "balanced loadout")

        info.addSpacer(10f)

        info.addPara("They always come eequipped with a \"Seraph\" type core. This core is dangerous if left unchecked, as it becomes stronger over longer deployment.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "")

        info.addSpacer(10f)
        info.addSectionHeading("Hullmods", Alignment.MID, 0f)
        info.addSpacer(10f)


        var graceHullmodSpec = Global.getSettings().getHullModSpec("rat_seraphs_grace")
        var graceIMG = info.beginImageWithText(graceHullmodSpec.spriteName, 48f)
        graceIMG.addPara(graceHullmodSpec.displayName, 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        graceIMG.addPara("This hullmod allows the ship to temporarily become stronger after coming under heavy fire. Caution is advised against any ship that is still functional despite heavy damage. \n\n" +
                "Additionaly it causes Seraphs to be stronger while in the abyss. This effect makes them a large threat within the enviroment they call home.", 0f)
        info.addImageWithText(0f)

        info.addSpacer(10f)

        var gridHullmodSpec = Global.getSettings().getHullModSpec("rat_abyssal_grid")
        var gridIMG = info.beginImageWithText(gridHullmodSpec.spriteName, 48f)
        gridIMG.addPara(gridHullmodSpec.displayName, 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        gridIMG.addPara("Improves the flux efficiency of energy weapons and increases their range. It also provides the ships with EMP resistance and total immunity to abyssal storms of any kind.", 0f)
        info.addImageWithText(0f)

        info.addSpacer(10f)

    }



    override fun getIcon(): String? {
        return Global.getSettings().getSpriteName("intel", "fleet_log")
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        return mutableSetOf("Abyss")
    }


}