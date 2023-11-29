package assortment_of_things.backgrounds

import assortment_of_things.backgrounds.commander.CommanderLocationInteraction
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig

class CommanderBackground : BaseCharacterBackground() {


    override fun shouldShowInSelection(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig): Boolean {
        return RATSettings.backgroundsEnabled!!/* && factionSpec.id != Factions.PLAYER*/
    }

    override fun getShortDescription(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig?): String {
        return "You are the commander of a small station in ${factionSpec.displayName} space, coming with its own sets of benefits."
    }

    override fun getLongDescription(factionSpec: FactionSpecAPI, factionConfig: NexFactionConfig): String {
        return "You command a small station that provides you with some tools. The station can not be expanded, but it provides you with storage and a limited amount of custom production orders. Also provides some income."
    }

    override fun addTooltipForIntel(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.addTooltipForIntel(tooltip, factionSpec, factionConfig)

        var station = Global.getSector().memoryWithoutUpdate.getEntity("\$rat_base_commander_station")
        var system = station.starSystem

        tooltip!!.addSpacer(10f)
        tooltip.addPara("The station is located in the ${system.name}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${system.name}")
    }

    override fun onNewGameAfterTimePass(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {

        Global.getSector().addScript( object : EveryFrameScript {

            var timestamp = Global.getSector().clock.timestamp
            var requiredDays = 0.1f
            var done = false
            var id = factionSpec!!.id

            override fun isDone(): Boolean {
                return done
            }


            override fun runWhilePaused(): Boolean {
                return false
            }

            override fun advance(amount: Float) {
                if (Global.getSector().clock.getElapsedDaysSince(timestamp) >= requiredDays) {
                    if (Global.getSector().campaignUI != null && !Global.getSector().campaignUI.isShowingDialog) {
                        done = true
                        Global.getSector().campaignUI.showInteractionDialog(CommanderLocationInteraction(factionSpec!!), Global.getSector().playerFleet)
                    }
                }
            }
        })
    }
}