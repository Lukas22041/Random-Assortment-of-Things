package assortment_of_things.campaign.items.cores

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.rpg.Person
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.*



class AzureProcessorCore : AICoreOfficerPlugin {

    var automatedPointsMult = 2f

    override fun createPerson(aiCoreId: String?, factionId: String?, random: Random?): PersonAPI? {
        var core =AICoreUtil.createCorePerson(aiCoreId, factionId)
        core.stats.level = 3
        core.setPersonality(Personalities.CAUTIOUS)
        core.setRankId(Ranks.SPACE_CAPTAIN)

        core.stats.setSkillLevel(Skills.FIELD_MODULATION, 2F)
        core.stats.setSkillLevel(Skills.DAMAGE_CONTROL, 2F)
        core.stats.setSkillLevel(Skills.POINT_DEFENSE, 2F)

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
        img.addPara("The " + spec.name + " is compareable in computational power to a gamma-core, but it also causes chiral ships to become more defensive.", 0f)
        tooltip.addImageWithText(0f)
        tooltip.addSectionHeading("Personality Config", text, bg, Alignment.MID, 20f)
        tooltip.addPara("The " + spec.name + " has no personality of it's own, instead it can emulate a set of personalities. " +
                "Those can switched through by pressing the Middle Mouse Button.\n\n" +
                "Personalites: Timid, Cautious, Steady",
            opad, Misc.getHighlightColor(), "Middle Mouse Button", "Personalites:")

        if (Mouse.getEventButton() == 2)
        {
            if (person.personalityAPI.id == Personalities.TIMID) person.setPersonality(Personalities.CAUTIOUS)
            else if (person.personalityAPI.id == Personalities.CAUTIOUS) person.setPersonality(Personalities.STEADY)
            else if (person.personalityAPI.id == Personalities.STEADY) person.setPersonality(Personalities.TIMID)
        }

        AICoreUtil.addPersonalityTooltip(person, tooltip)
    }

}