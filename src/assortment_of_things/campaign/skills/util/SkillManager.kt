package assortment_of_things.campaign.skills.util

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

object SkillManager {

    fun update()
    {
        updateSkills()
        updateAptitude()
    }

    fun updateAptitude()
    {
        var specs = Global.getSettings().skillIds.map { Global.getSettings().getSkillSpec(it) }.filter { it.hasTag("rat_learnable") }

        var hasAny = false

        for (spec in specs)
        {
            if (Global.getSector().playerPerson.memoryWithoutUpdate.get("\$rat_learned_${spec.id}") != null)
            {
                hasAny = true
            }
        }

        var apt = Global.getSettings().getSkillSpec("rat_aptitude_learned")
        if (apt != null)
        {
            if (hasAny && Global.getCurrentState() != GameState.TITLE)
            {
                apt.tags.remove("npc_only")
            }
            else
            {
                apt.addTag("npc_only")
            }
        }
    }

    fun updateSkills()
    {
        var specs = Global.getSettings().skillIds.map { Global.getSettings().getSkillSpec(it) }.filter { it.hasTag("rat_learnable") }

        for (spec in specs)
        {

           // spec.addTag("npc_only")

            var learned = Global.getSector().playerPerson.memoryWithoutUpdate.get("\$rat_learned_${spec!!.id}")
            if (learned == null)
            //if (skill.level > 0.1f)
            {
                spec.reqPoints = 10000
                spec.addTag("npc_only")
            }
            else
            {
                spec.tags.remove("npc_only")
            }
        }
    }

    fun addUnavailableTooltip(tooltip: TooltipMakerAPI, skillId: String) : Boolean
    {
        //&& Global.getSector().campaignUI.currentCoreTab != CoreUITabId.CARGO


        var skill = Global.getSettings().skillIds.map { Global.getSettings().getSkillSpec(it) }.find { it.id == skillId }
       // var skill: SkillLevelAPI? = Global.getSector().playerPerson.stats.skillsCopy.find { it.skill.id == skillId  }


        //Always 10 in the menu for some reason, need to check if the player character just has the skill i guess, wait, not sure if that works since the player already has the skill i think
        if (skill!!.hasTag("npc_only") && Global.getSector().campaignUI.currentCoreTab == CoreUITabId.CHARACTER)
        {

            tooltip.addPara("This skill can only be unlocked by finding the relevant training data within the sector. No further data is available.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
            tooltip.addSpacer(10f)
            return true
        }
        else
        {
            return false
        }
    }
}