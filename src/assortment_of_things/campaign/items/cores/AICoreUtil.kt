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

object AICoreUtil
{

    fun createCorePerson(aiCoreId: String?, factionId: String?) : PersonAPI
    {
        val spec = Global.getSettings().getCommoditySpec(aiCoreId)
        var core = Global.getFactory().createPerson()

        core.id = Misc.genUID()
        core.setFaction(factionId)
        core.setAICoreId(aiCoreId)
        core.setName(FullName(spec.getName(), "", FullName.Gender.ANY))

        core.setPortraitSprite(spec.iconName)

        core.stats.isSkipRefresh = false

        return core
    }

    fun addPersonalityTooltip(person: PersonAPI?, tooltip: TooltipMakerAPI?)
    {
        val opad = 10f
        val text = person!!.faction.baseUIColor
        val bg = person.faction.darkUIColor
        val spec = Global.getSettings().getCommoditySpec(person.aiCoreId)

        if (person.personalityAPI.id == Personalities.TIMID)
        {
            tooltip!!.addSectionHeading("Personality: Timid", text, bg, Alignment.MID, 20f)
            tooltip.addPara("In combat, the " + spec.name + " will attempt to avoid direct engagements if at all possible, even if commanding a combat vessel.",
                opad)
        }
        if (person.personalityAPI.id == Personalities.CAUTIOUS)
        {
            tooltip!!.addSectionHeading("Personality: Cautious", text, bg, Alignment.MID, 20f)
            tooltip.addPara("In combat, the " + spec.name + " will prefer to stay out of enemy range, only occasionally moving in if out-ranged by the enemy.",
                opad)
        }
        if (person.personalityAPI.id == Personalities.STEADY)
        {
            tooltip!!.addSectionHeading("Personality: Steady", text, bg, Alignment.MID, 20f)
            tooltip.addPara("In combat, the " + spec.name + " will favor a balanced approach with tactics matching the current situation.",
                opad)
        }
        if (person.personalityAPI.id == Personalities.AGGRESSIVE)
        {
            tooltip!!.addSectionHeading("Personality: Aggressive", text, bg, Alignment.MID, 20f)
            tooltip.addPara("In combat, the " + spec.name + " will prefer to engage at a range that allows the use of all of their ship's weapons, will pilot civilian ships as if they were combat ships, and will employ any fighters under their command aggressively.",
                opad)
        }
        if (person.personalityAPI.id == Personalities.RECKLESS)
        {
            tooltip!!.addSectionHeading("Personality: Fearless", text, bg, Alignment.MID, 20f)
            tooltip.addPara("In combat, the " + spec.name + " is aggressive to a fault, often disregarding the safety of their ship entirely in an effort to engage the enemy.",
                opad)
        }
    }
}