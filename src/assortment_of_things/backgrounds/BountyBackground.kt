package assortment_of_things.backgrounds

import assortment_of_things.backgrounds.bounty.BackgroundBountyManager
import assortment_of_things.backgrounds.bounty.BountyFleetIntel
import assortment_of_things.exotech.ExoUtils
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

class BountyBackground : BaseCharacterBackground() {


    override fun shouldShowInSelection(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return RATSettings.backgroundsEnabled!!
    }


    override fun getLongDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        return "Your security is compromised. You are being watched from the shadows, with your position periodically relayed to third parties interested in having you gone for good."
    }

    fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip!!.addSpacer(10f)

        tooltip!!.addPara("Factions will occasionally send bounty fleets towards your general location, increasing in strength as your own fleet increases in size. " +
                "Bounty fleets only know your location if you have recently visited a populated volume of space. Defeating enough of them may send the message that your ascent to power cannot be stopped.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "bounty fleets", "increasing in strength", "populated volume of space", "defeating enough of them")
    }

    override fun onNewGameAfterTimePass(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.onNewGameAfterTimePass(factionSpec, factionConfig)

       /* var markets = Global.getSector().economy.marketsCopy.filter { it.faction.relToPlayer.isHostile && !it.isHidden && it.size >= 3 }
        var market = markets.randomOrNull()
        if (market != null) {
            var bountyFleet = BountyFleetIntel(market.factionId, market)
            bountyFleet.startEvent()
        }*/

        Global.getSector().addScript(BackgroundBountyManager())
    }

    override fun addTooltipForSelection(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?, expanded: Boolean) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded)
        getTooltip(tooltip!!)
    }

    override fun addTooltipForIntel(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.addTooltipForIntel(tooltip, factionSpec, factionConfig)

        var manager = Global.getSector().scripts.find { it is BackgroundBountyManager } as BackgroundBountyManager?
        if (manager != null && manager.finished) {
            tooltip!!.addSpacer(10f)
            tooltip!!.addPara("Your repeated victories have successfully deterred your enemies; no more bounties will be placed on your fleet.")
            return
        }

        getTooltip(tooltip!!)


    }

}