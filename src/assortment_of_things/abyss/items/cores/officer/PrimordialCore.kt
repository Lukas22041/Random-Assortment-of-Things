package assortment_of_things.abyss.items.cores.officer

import assortment_of_things.abyss.items.cores.AICoreUtil
import assortment_of_things.abyss.skills.PrimordialCoreSkill
import assortment_of_things.abyss.skills.TimeCoreSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.util.*



class PrimordialCore : AICoreOfficerPlugin {

    var automatedPointsMult = 4f

    override fun createPerson(aiCoreId: String?, factionId: String?, random: Random?): PersonAPI? {
        var core = AICoreUtil.createCorePerson(aiCoreId, factionId)
        core.stats.level = 8
        core.setPersonality(Personalities.RECKLESS)
        core.setRankId(Ranks.SPACE_CAPTAIN)

        var portrait = "graphics/portraits/cores/rat_primordial_core.png"
        Global.getSettings().loadTexture(portrait)
        core.setPortraitSprite(portrait)

        core.stats.setSkillLevel(Skills.HELMSMANSHIP, 2f)
        core.stats.setSkillLevel(Skills.TARGET_ANALYSIS, 2f)
        core.stats.setSkillLevel(Skills.IMPACT_MITIGATION, 2f)
        core.stats.setSkillLevel(Skills.FIELD_MODULATION, 2f)
        core.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
        core.stats.setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2f)
        core.stats.setSkillLevel(Skills.DAMAGE_CONTROL, 2f)
        core.stats.setSkillLevel("rat_core_primordial", 1f)

        core.memoryWithoutUpdate.set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, automatedPointsMult)
        return core
    }

    override fun createPersonalitySection(person: PersonAPI, tooltip: TooltipMakerAPI) {
        val opad = 10f
        val text = person!!.faction.baseUIColor
        val bg = person.faction.darkUIColor
        val spec = Global.getSettings().getCommoditySpec(person.aiCoreId)
        var desc = Global.getSettings().getDescription(spec.id, Description.Type.RESOURCE)

        var skill = Global.getSettings().getSkillSpec("rat_core_primordial")

        tooltip.addSpacer(10f)
        var img = tooltip.beginImageWithText(spec.iconName, 64f)
        img.addPara(desc.text1, 0f)

        img.addSpacer(5f)

        img.addPara("Automated Points Multiplier: ${(person.memoryWithoutUpdate.get(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT) as Float).toInt()}x", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Automated Points Multiplier")

        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        tooltip.addSectionHeading("Signature Skill: ${skill.name}", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        var skillImg = tooltip.beginImageWithText(skill.spriteName, 48f)
        PrimordialCoreSkill().createCustomDescription(null, null, skillImg, tooltip.widthSoFar)

        tooltip.addImageWithText(0f)

        AICoreUtil.addPersonalityTooltip(person, tooltip)
    }

}