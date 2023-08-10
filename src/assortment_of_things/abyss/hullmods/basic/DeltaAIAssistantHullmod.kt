package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.campaign.ui.DeltaAIRefitButton
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.sun.org.apache.regexp.internal.RE

class DeltaAIAssistantHullmod : BaseAlteration() {

    var modID = "rat_delta_ai"

    companion object {
        fun getCurrentSkill(ship: ShipAPI) : SkillSpecAPI? {
            return getCurrentSkill(ship.variant)
        }

        fun getCurrentSkill(variant: ShipVariantAPI) : SkillSpecAPI? {
            var tag = variant.tags.find { it.contains("rat_deltaskill_") }
            if (tag != null) {
                var id = tag!!.replace("rat_deltaskill_", "")
                var skill = Global.getSettings().getSkillSpec(id)
                return skill
            }
            return null
        }
    }

    fun applySkillStats(stats: MutableShipStatsAPI, variant: ShipVariantAPI) {
        var skill: SkillSpecAPI = getCurrentSkill(variant) ?: return
        var lambda = DeltaAIRefitButton.deltaSkills.get(skill.id) ?: return

        lambda(stats, variant.hullSize, "rat_delta_skill", 2f)
    }


    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)
        tooltip.addPara("Integrates an additional delta-level AI in to the ship, which can assist the ship with a single elite skill. " +
                "The skill is only applied if the officer does not already have it.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
        "delta-level AI", "elite skill")

        tooltip.addSpacer(20f)


        var path = "graphics/icons/mission_marker.png"
        var name = "No skill set."

        if (ship != null) {

            var skill = getCurrentSkill(ship)
            if (skill != null) {
                name = skill.name
                path = skill.spriteName
            }
            Global.getSettings().loadTexture(path)

            var img = tooltip.beginImageWithText(path, 32f)

            img.addPara("Selected Skill:", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
            img.addPara(name, 0f)

            tooltip.addImageWithText(0f)

            tooltip.addSpacer(20f)
        }

        tooltip!!.addPara("The skill can be choosen from the \"Additional Options\" button at the top of the refit screen if this alteration is installed.",0f,
            Misc.getGrayColor(), Misc.getGrayColor(), "")


    }


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        var captain = stats!!.fleetMember?.captain
        var skill = getCurrentSkill(stats.variant) ?: return

        if (captain != null) {
            if (!captain.stats.hasSkill(skill.id)) {
                applySkillStats(stats, stats.variant)
            }
        }
        else {
            applySkillStats(stats, stats.variant)
        }
    }


    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        var captain = ship!!.captain
        var skill = getCurrentSkill(ship) ?: return

        if (captain != null) {
            if (!captain.stats.hasSkill(skill.id)) {
                applySkillStats(ship.mutableStats, ship.variant)
            }
        }
        else {
            applySkillStats(ship.mutableStats, ship.variant)
        }
    }
}