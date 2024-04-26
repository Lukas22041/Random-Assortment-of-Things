package assortment_of_things.backgrounds

import assortment_of_things.backgrounds.comradery.ComraderyInteraction
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig

class ComraderyBackground : BaseCharacterBackground() {

    override fun shouldShowInSelection(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return RATSettings.backgroundsEnabled!!
    }

    override fun getLongDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        return "At the start of the game, you get to create an officer of high potential, together with a unique skill from a variety of choices."
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
                        Global.getSector().campaignUI.showInteractionDialog(ComraderyInteraction(id), Global.getSector().playerFleet)
                    }
                }
            }
        })

    }

    override fun addTooltipForSelection(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?, expanded: Boolean) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded)

        tooltip!!.addSpacer(10f)
        tooltip.addPara("Your partner has a max level of 8, can select from 6 skills at level up and can have 3 elite skills.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "8", "6", "3")

    }

    override fun addTooltipForIntel(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.addTooltipForIntel(tooltip, factionSpec, factionConfig)

        var person = Global.getSector().memoryWithoutUpdate.get("\$rat_comerade_start_person") as PersonAPI?
        if (person != null) {
            tooltip!!.addSpacer(10f)
            var img = tooltip!!.beginImageWithText(person!!.portraitSprite, 50f)
            img.addPara("${person.nameString} is doing ${person.hisOrHer} best to work towards both of your dreams", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "${person.nameString}")
            tooltip.addImageWithText(0f)
        }

    }
}