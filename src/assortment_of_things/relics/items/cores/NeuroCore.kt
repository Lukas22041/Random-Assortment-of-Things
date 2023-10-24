package assortment_of_things.relics.items.cores

import assortment_of_things.abyss.items.cores.AICoreUtil
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.util.*


class NeuroCore : AICoreOfficerPlugin {

    var automatedPointsMult = 4f

    companion object {
        fun getCore() : PersonAPI {
            var core = Global.getSector().memoryWithoutUpdate.get("\$rat_neuro_core") as PersonAPI?
            if (core == null) {
                core = NeuroCore().createPerson("rat_neuro_core", Factions.PLAYER, Random())
            }
            return core
        }
    }

    override fun createPerson(aiCoreId: String?, factionId: String?, random: Random?): PersonAPI {

        var existingCore = Global.getSector().memoryWithoutUpdate.get("\$rat_neuro_core") as PersonAPI?
        if (existingCore != null) {
            return existingCore
        }

        var core = AICoreUtil.createCorePerson(aiCoreId, factionId)
        Global.getSector().memoryWithoutUpdate.set("\$rat_neuro_core", core)
        core.stats.level = 7
        core.setPersonality(Personalities.RECKLESS)
        core.setRankId(Ranks.SPACE_CAPTAIN)

        var portrait = "graphics/portraits/cores/rat_neural_core.png"
        core.setPortraitSprite(portrait)

        core.stats.setSkillLevel(Skills.HELMSMANSHIP, 2f)
        core.stats.setSkillLevel(Skills.TARGET_ANALYSIS, 2f)
        core.stats.setSkillLevel(Skills.IMPACT_MITIGATION, 2f)
        core.stats.setSkillLevel(Skills.FIELD_MODULATION, 2f)
        core.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
        core.stats.setSkillLevel(Skills.COMBAT_ENDURANCE, 2f)
        core.stats.setSkillLevel("rat_hyperlink", 2f)

        core.memoryWithoutUpdate.set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, automatedPointsMult)
        return core
    }

    override fun createPersonalitySection(person: PersonAPI, tooltip: TooltipMakerAPI) {
        val opad = 10f
        val text = person!!.faction.baseUIColor
        val bg = person.faction.darkUIColor

        tooltip.addSpacer(10f)

        tooltip.addPara("This experimental alpha-core is able to communicate with a human targets brainwaves directly, this allows it near instant communication in combat.", 0f)

        tooltip.addSpacer(5f)

        tooltip.addPara("Automated Points Multiplier: ${(person.memoryWithoutUpdate.get(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT) as Float).toInt()}x", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Automated Points Multiplier")


        AICoreUtil.addPersonalityTooltip(person, tooltip)
    }

}