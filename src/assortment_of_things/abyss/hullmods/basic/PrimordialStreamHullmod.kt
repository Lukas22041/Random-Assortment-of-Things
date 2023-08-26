package assortment_of_things.abyss.hullmods.basic

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.abyss.skills.SpaceCoreSkill
import assortment_of_things.abyss.skills.TimeCoreSkill
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class PrimordialStreamHullmod : BaseAlteration() {

    var modID = "rat_primordial_stream"


    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)

        tooltip!!.addPara("Applies the \"Deity of Time\" and \"Deity of Space\" skills for the ship in combat. Each skill is only applied if the officer doesnt already have it.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(),
        "Deity of Time", "Deity of Space")

        var timeSkill = Global.getSettings().getSkillSpec("rat_core_time")
        tooltip.addSpacer(10f)
        var timeSkillImage = tooltip.beginImageWithText(timeSkill.spriteName, 48f)
        TimeCoreSkill().createCustomDescription(null, null, timeSkillImage, tooltip.widthSoFar)
        tooltip.addImageWithText(0f)

        var spaceSkill = Global.getSettings().getSkillSpec("rat_core_space")
        tooltip.addSpacer(10f)
        var spaceSkillImage = tooltip.beginImageWithText(spaceSkill.spriteName, 48f)
        SpaceCoreSkill().createCustomDescription(null, null, spaceSkillImage, tooltip.widthSoFar)
        tooltip.addImageWithText(0f)

    }


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        var captain = stats!!.fleetMember?.captain

        if (captain != null) {
            if (!captain.stats.hasSkill("rat_core_time")) {
                TimeCoreSkill().apply(stats, stats.variant.hullSize, modID, 2f)
            }
            if (!captain.stats.hasSkill("rat_core_space")) {
                SpaceCoreSkill().apply(stats, stats.variant.hullSize, modID, 2f)
            }
        }
        else {
            TimeCoreSkill().apply(stats, stats.variant.hullSize, modID, 2f)
            SpaceCoreSkill().apply(stats, stats.variant.hullSize, modID, 2f)
        }
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        var captain = ship!!.captain

        if (captain != null) {
            if (!captain.stats.hasSkill("rat_core_time")) {
                TimeCoreSkill().apply(ship.mutableStats, ship.hullSize, modID, 2f)
            }
            if (!captain.stats.hasSkill("rat_core_space")) {
                SpaceCoreSkill().apply(ship.mutableStats, ship.hullSize, modID, 2f)
            }
        }
        else {
            TimeCoreSkill().apply(ship.mutableStats, ship.hullSize, modID, 2f)
            SpaceCoreSkill().apply(ship.mutableStats, ship.hullSize, modID, 2f)
        }
    }

}