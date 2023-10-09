package assortment_of_things.abyss.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class DoctrineReportAbyssal() : BaseIntelPlugin() {


    override fun getName(): String? {
        return "Doctrine Report"
    }


    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)

        info.addPara("Abyssal Doctrine", 0f, Color(16, 154, 100,255), Color(16, 154, 100,255))

    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {

        info.addSpacer(5f)
        info.addPara("Over multiple encounters with the unknown threat, the fleets intelligence office was able to construct a comprehensive list of notes specifying details of the opponent." +
                "This document portraits details about the \"Abyssal\" doctrine.", 0f, Misc.getTextColor(), Color(16, 154, 100,255), "Abyssal")
        info.addSpacer(10f)

        info.addSectionHeading("Details", Alignment.MID, 0f)
        info.addSpacer(10f)

        info.addPara("The threats fleets are filled with automated ships that boast very capable shields, however their ships are not heavily armored. " +
                "Because of this, once the shields are broken through they can be made quick work off, with the shield breaking being the tricky part.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "capable shields", "are not heavily armored")

        info.addSpacer(10f)

        info.addPara("Additionaly, their fleets seem to overinvest in to kinetic weaponry, making armored ships a viable tactic against their forces.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "capable shields", "are not heavily armored")

        info.addSpacer(10f)
        info.addSectionHeading("Hullmods", Alignment.MID, 0f)
        info.addSpacer(10f)


        var synergyHullmodSpec = Global.getSettings().getHullModSpec("rat_abyssal_core")
        var synergyIMG = info.beginImageWithText(synergyHullmodSpec.spriteName, 48f)
        synergyIMG.addPara(synergyHullmodSpec.displayName, 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        synergyIMG.addPara("The ship are capable of adjusting between two different shipsystems each. This is toggled through the type of AI core piloting the ship. A ship without a core or a core from outside of this enviroment seem inable of activating the ships system.", 0f)
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