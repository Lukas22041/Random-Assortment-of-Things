package assortment_of_things.backgrounds

import assortment_of_things.backgrounds.bounty.BackgroundBountyManager
import assortment_of_things.backgrounds.bounty.BountyFleetIntel
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig
import lunalib.lunaUtil.LunaCommons
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.getOfficerSalary
import org.magiclib.kotlin.isAutomated
import org.magiclib.kotlin.isDecentralized

class NeuralShardBackground : BaseCharacterBackground() {


    override fun shouldShowInSelection(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return RATSettings.backgroundsEnabled!!
    }


    override fun getLongDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        return "You can take control of all ships within your fleet, but the mental exhaustion from the chip makes it hard to work with others."
    }

    fun getTooltip(tooltip: TooltipMakerAPI) {

        tooltip.addSpacer(10f)

        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        var key = Keyboard.getKeyName(RATSettings.backgroundsAbilityKeybind!!)
        if (RATSettings.backgroundsAbilityKeybind == 0) key = "right click"

        var label = tooltip!!.addPara(
                "All non-automated ships in your fleet without an officer receive a shard of yourself as their pilot. " +
                        "Those shards share two to three of your own combat skills. Those skills are assigned at random in every combat encounter. Every shard, and yourself, has aggressive behaviour. \n\n" +
                        "" +
                        "You can switch to any ship with a shard immediately by pressing $key while hovering over the ship. The ship currently controlled has access to all of your skills. \n\n" +
                        "" +
                        "The maximum number of officers able to join your fleet is halved, and you have a harder time finding anyone wanting to join you.", 0f)


        label.setHighlight("receive a shard of yourself as their pilot", "two to three", "aggressive", "switch", "$key", "all of your skills.", "halved")
        label.setHighlightColors(hc, hc, hc, hc, hc, hc, nc)

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




        Global.getSector().addScript(NeuralShardOfficerScript())
    }
}

class NeuralShardOfficerScript : EveryFrameScript {

    var interval = IntervalUtil(1f, 1f)

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        interval.advance(amount)

        for (member in Global.getSector().playerFleet.fleetData.membersListCopy) {
            if (member.captain == null || member.captain?.isDefault == true)
            {
                if (!member.isAutomated()) {
                    addShardOfficer(member)
                }
            }

            if (member.captain?.hasTag("rat_neuro_shard") == true) {
                for (skill in member.captain.stats.skillsCopy) {
                    member.captain.stats.setSkillLevel(skill.skill.id, 0f)
                }
            }
        }

        if (interval.intervalElapsed()) {

            var stats = Global.getSector().characterData.person.stats
            stats.officerNumber.modifyMult("rat_neural_shard", 0.5f)

            for (market in Global.getSector().economy.marketsCopy) {
                market.stats.dynamic.getMod(Stats.OFFICER_PROB_MOD).modifyMult("rat_army_mod", 0.5f)
                market.stats.dynamic.getMod(Stats.OFFICER_ADDITIONAL_PROB_MULT_MOD).modifyMult("rat_army_mod", 0.5f)
            }
        }
    }

    fun addShardOfficer(member: FleetMemberAPI) {

        var person = Global.getFactory().createPerson()
        person.setPersonality(Personalities.AGGRESSIVE)

        var player = Global.getSector().playerPerson

        person.getOfficerSalary()
        person.name = FullName("${player.nameString}", "", player.gender)
        person.gender = player.gender
        //person.portraitSprite = player.portraitSprite
        person.portraitSprite = "graphics/portraits/cores/rat_neural_shard.png"
        person.rankId = "rat_shard"

        var level = MathUtils.getRandomNumberInRange(2, 3)
        person.stats.level = level

       /* var skills = player.stats.skillsCopy.filter { it.skill.isCombatOfficerSkill && !it.skill.hasTag("npc_only") && it.level >= 0.5f }.toMutableList()
        for (i in 0 until  level) {
            var skill = skills.randomOrNull() ?: continue

            skills.remove(skill)
            person.stats.setSkillLevel(skill.skill.id, skill.level)
        }*/

        person.addTag("rat_neuro_shard")

        member.captain = person
        //ship.setCustomData("rat_neuro_shard", person)
    }

}