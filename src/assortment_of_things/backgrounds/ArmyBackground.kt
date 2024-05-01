package assortment_of_things.backgrounds

import assortment_of_things.backgrounds.bounty.BackgroundBountyManager
import assortment_of_things.backgrounds.bounty.BountyFleetIntel
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.interactions.exoship.ExoShipBuyInteraction
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig
import lunalib.lunaUtil.LunaCommons
import org.magiclib.kotlin.isDecentralized

class ArmyBackground : BaseCharacterBackground() {


    override fun shouldShowInSelection(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return RATSettings.backgroundsEnabled!!
    }


    override fun getLongDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        return "You are experienced in leading multiple squadrons and their officers."
    }

    fun getTooltip(tooltip: TooltipMakerAPI) {

        tooltip.addSpacer(10f)

        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        var label = tooltip.addPara(
                "The amount of maximum officers within your fleet is doubled, but their maximum level is reduced by 2, and they can not have more than one elite skill. " +
                        "You are also much more likely to find officers on colonies. \n\n" +
                        "" +
                        "All ships with officers also provide an effective increase of strength in planetary raids of 10/20/35/50 depending on their hullsize and the total number of marines in the fleet.  \n\n" +
                        "" +
                        "The reduction in officer capability may not apply to some special officers.", 0f)


        label.setHighlightColors(hc, nc, nc, hc, hc, hc, hc)
        label.setHighlight("doubled", "2", "can not have more than one elite skill", "10", "20", "35", "50")

    }

    override fun addTooltipForSelection(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?, expanded: Boolean) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded)
        getTooltip(tooltip!!)
    }

    override fun addTooltipForIntel(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.addTooltipForIntel(tooltip, factionSpec, factionConfig)
        getTooltip(tooltip!!)


    }

    override fun onNewGameAfterTimePass(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        super.onNewGameAfterTimePass(factionSpec, factionConfig)




        Global.getSector().addScript(PersonalArmyScript())

        for (officer in Global.getSector().playerFleet.fleetData.officersCopy) {
            for (skill in officer.person.stats.skillsCopy) {
                if (skill.level >= 2f) {
                    skill.level = 1f
                }
            }
        }
    }

}

class PersonalArmyScript : EveryFrameScript {

    var interval = IntervalUtil(1f, 1f)

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        interval.advance(amount)
        if (interval.intervalElapsed()) {

            var stats = Global.getSector().characterData.person.stats

            stats.officerNumber.modifyMult("rat_army_mod", 2f)

            stats.dynamic.getMod(Stats.OFFICER_MAX_LEVEL_MOD).modifyFlat("rat_army_mod", -2f)
            //stats.dynamic.getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).modifyFlat("rat_army_mod", -10000f, "Personal Army")
            stats.dynamic.getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).modifyMult("rat_army_mod", 0f, "Personal Army")

            for (market in Global.getSector().economy.marketsCopy) {
                market.stats.dynamic.getMod(Stats.OFFICER_PROB_MOD).modifyFlat("rat_army_mod", 0.1f)
                market.stats.dynamic.getMod(Stats.OFFICER_ADDITIONAL_PROB_MULT_MOD).modifyFlat("rat_army_mod", 0.1f)
                
                market.stats.dynamic.getMod(Stats.OFFICER_PROB_MOD).modifyMult("rat_army_mod", 1.2f)
                market.stats.dynamic.getMod(Stats.OFFICER_ADDITIONAL_PROB_MULT_MOD).modifyMult("rat_army_mod", 1.2f)
            }
        }
    }

}