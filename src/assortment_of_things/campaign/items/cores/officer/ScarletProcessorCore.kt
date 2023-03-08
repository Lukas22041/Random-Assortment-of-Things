package assortment_of_things.campaign.items.cores.officer

import assortment_of_things.campaign.items.cores.AICoreUtil
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Mouse
import java.util.*



class ScarletProcessorCore : AICoreOfficerPlugin {

    var automatedPointsMult = 2f

    override fun createPerson(aiCoreId: String?, factionId: String?, random: Random?): PersonAPI? {
        var core = AICoreUtil.createCorePerson(aiCoreId, factionId)
        core.stats.level = 3
        core.setPersonality(Personalities.RECKLESS)
        core.setRankId(Ranks.SPACE_CAPTAIN)

        core.stats.setSkillLevel(Skills.HELMSMANSHIP, 2F)
        core.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2F)
        core.stats.setSkillLevel(Skills.TARGET_ANALYSIS, 2F)

        core.memoryWithoutUpdate.set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, automatedPointsMult)
        return core
    }

    override fun createPersonalitySection(person: PersonAPI, tooltip: TooltipMakerAPI) {
        val opad = 10f
        val text = person!!.faction.baseUIColor
        val bg = person.faction.darkUIColor
        val spec = Global.getSettings().getCommoditySpec(person.aiCoreId)

        tooltip.addSpacer(10f)
        var img = tooltip.beginImageWithText(person.portraitSprite, 64f)
        img.addPara("The " + spec.name + " is compareable in computational power to a gamma-core, but it also causes chiral ships to become more aggresive.", 0f)
        tooltip.addImageWithText(0f)
        tooltip.addSectionHeading("Personality Config", text, bg, Alignment.MID, 20f)
        tooltip.addPara("The " + spec.name + " has no personality of it's own, instead it can emulate a set of personalities. " +
                "Those can switched through by pressing the Middle Mouse Button.\n\n" +
                "Personalites: Aggressive, Fearless",
            opad, Misc.getHighlightColor(), "Middle Mouse Button", "Personalites:")

        if (Mouse.getEventButton() == 2)
        {
            if (person.personalityAPI.id == Personalities.AGGRESSIVE) person.setPersonality(Personalities.RECKLESS)
            else if (person.personalityAPI.id == Personalities.RECKLESS) person.setPersonality(Personalities.AGGRESSIVE)
        }

        AICoreUtil.addPersonalityTooltip(person, tooltip)
    }

}