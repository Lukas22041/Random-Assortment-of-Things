package assortment_of_things.backgrounds

import assortment_of_things.backgrounds.bounty.BackgroundBountyManager
import assortment_of_things.backgrounds.bounty.BountyFleetIntel
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.interactions.exoship.ExoShipBuyInteraction
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig
import lunalib.lunaUtil.LunaCommons
import org.magiclib.kotlin.isDecentralized

class ForceBackground : BaseCharacterBackground() {


    override fun shouldShowInSelection(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return RATSettings.backgroundsEnabled!!
    }


    override fun getLongDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        return "Small crafts exceed within your fleet, but the use of larger craft is severely limited."
    }

    fun getTooltip(tooltip: TooltipMakerAPI) {

        tooltip.addSpacer(10f)

        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        var label = tooltip!!.addPara(
                "All frigates and destroyers in your fleet gain several improvements to their performance within combat. " +
                "Their combat readiness is increased by 15% and they can stay deployed for an additional 60 seconds before degrading in ability. \n\n" +
                "They also receive a 20% increase in the maximum speed and maneuverability, 10% improved fire rate and flux dissipation, and have armor increased by 15%. \n\n" +
                "" +
                "However as your excellence lies in small craft, cruiser and capital class ships within your fleet are almost entirely unuseable due to your lack of competence. This also applies to logistical ships.", 0f)


        label.setHighlight("frigates and destroyers", "15%","60","20%","10%","15%","cruiser and capital", "almost entirely unuseable")
        label.setHighlightColors(hc, hc, hc, hc, hc, hc, nc, nc)

    }

    override fun addTooltipForSelection(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?, expanded: Boolean) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded)
        getTooltip(tooltip!!)
    }

    override fun addTooltipForIntel(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.addTooltipForIntel(tooltip, factionSpec, factionConfig)
        getTooltip(tooltip!!)


    }

}