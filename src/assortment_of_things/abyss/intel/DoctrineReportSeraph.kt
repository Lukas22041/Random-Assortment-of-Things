package assortment_of_things.abyss.intel

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.items.cores.officer.SeraphCore
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color
import java.util.*

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

        info.addPara("This design type appears to be more of a backup for abyssal fleets. It appears only in few numbers mixed within abyssal fleets. They also only seem to reside in the deeper parts of the abyss.\n\n" +
                "Despite this they should not be underestimated. Their specs are of higher quality than the abyssal baseline and can easily overwhelm a fleet that isn't prepared for them.\n\n" +
                "They boast a more balanced loadout, having both more armor themself and more tools to deal with armor than their counterpart. ", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "balanced loadout")

        info.addSpacer(10f)

        info.addSpacer(10f)
        info.addSectionHeading("AI Cores", Alignment.MID, 0f)
        info.addSpacer(10f)

        var seraphCore = SeraphCore().createPerson(RATItems.SERAPH_CORE, AbyssUtils.FACTION_ID, Random())
        var seraphCoreIMG = info.beginImageWithText(seraphCore.portraitSprite, 48f)
        seraphCoreIMG.addPara(seraphCore.nameString, 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        seraphCoreIMG.addPara("Seraph ships always come with this specific core. It allows them to both recover hull and increase their damage output by damaging their opponents hull or armor. They become more dangerous over the duration of a battle if left unchecked.", 0f)
        info.addImageWithText(0f)

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
        return mutableSetOf("Abyssal Depths")
    }


}