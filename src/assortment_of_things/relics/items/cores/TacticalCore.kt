package assortment_of_things.relics.items.cores

import assortment_of_things.abyss.items.cores.AICoreUtil
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
import java.util.*



class TacticalCore : AICoreOfficerPlugin {

    var automatedPointsMult = 3f

    override fun createPerson(aiCoreId: String?, factionId: String?, random: Random?): PersonAPI? {
        var core = AICoreUtil.createCorePerson(aiCoreId, factionId)
        core.stats.level = 5
        core.setPersonality(Personalities.STEADY)
        core.setRankId(Ranks.SPACE_CAPTAIN)

        var portrait = "graphics/portraits/cores/rat_tactical_core.png"
        Global.getSettings().loadTexture(portrait)
        core.setPortraitSprite(portrait)

        core.stats.setSkillLevel(Skills.HELMSMANSHIP, 2F)
        core.stats.setSkillLevel(Skills.COMBAT_ENDURANCE, 2F)
        core.stats.setSkillLevel(Skills.FIELD_MODULATION, 2F)
        core.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2F)
        core.stats.setSkillLevel("rat_hyperlink", 2f)

        core.memoryWithoutUpdate.set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, automatedPointsMult)
        return core
    }

    override fun createPersonalitySection(person: PersonAPI, tooltip: TooltipMakerAPI) {
        val opad = 10f
        val text = person!!.faction.baseUIColor
        val bg = person.faction.darkUIColor

        AICoreUtil.addPersonalityTooltip(person, tooltip)
    }

}