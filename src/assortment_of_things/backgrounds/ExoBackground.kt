package assortment_of_things.backgrounds

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.interactions.exoship.ExoShipBuyInteraction
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig
import lunalib.lunaUtil.LunaCommons

class ExoBackground : BaseCharacterBackground() {

    var tokens = 7500f

    fun isUnlocked() : Boolean {
        return LunaCommons.get("assortment_of_things", "rat_exo_start") != null
    }

    override fun getTitle(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        if (isUnlocked()) return spec.title
        else return "${spec.title} [Locked]"
    }

    override fun shouldShowInSelection(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return RATSettings.backgroundsEnabled!! && RATSettings.exoEnabled!!
    }

    override fun canBeSelected(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return isUnlocked()
    }

    override fun getShortDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        if (!isUnlocked()) return "Gain a partnership with the Exo-Tech faction to unlock this background."
        else return "Your fleet is a trusted partner of the exo-tech corperation, A minor-faction hiding within the fringes of the sector on their mobile colonies."
    }

    override fun getLongDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        if (!isUnlocked()) return ""
        else {
            return "You made faithfull \"Donations\" and got rewarded with some of their technologies in exchange. " +
                    "Despite this, the faction still doesnt recognize you as one of their own, leaving you to fend for yourself."
        }
    }

    /*override fun getSpawnLocationOverwrite(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): SectorEntityToken {
        return ExoUtils.getExoData().exoships.random()
    }*/

    override fun onNewGameAfterTimePass(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        ExoUtils.getExoData().interactedWithExoship = true
        ExoUtils.getExoData().hasPartnership = true
        ExoUtils.getExoData().tokens = tokens

        ExoShipBuyInteraction.unlockExoIntel(null, true)
    }

    override fun addTooltipForSelection(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?, expanded: Boolean) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded)

        if (!isUnlocked()) return

        tooltip!!.addSpacer(10f)

        var tokensString = Misc.getDGSCredits(tokens)
        tooltip!!.addPara("You begin your journey with $tokensString of the factions tokens. " +
                "You also get the same benefits as with a partnership.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "$tokensString", "vicinity", "partnership")
    }

    override fun getOrder(): Float {
        if (!isUnlocked()) return (Int.MAX_VALUE - spec.order).toFloat()
        return spec.order
    }
}