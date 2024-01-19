package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.abyss.skills.SpaceCoreSkill
import assortment_of_things.abyss.skills.TimeCoreSkill
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class IntegratedCosmosCore : BaseAlteration() {

    var minCrew = mapOf(
        ShipAPI.HullSize.FRIGATE to 10f,
        ShipAPI.HullSize.DESTROYER to 15f,
        ShipAPI.HullSize.CRUISER to 25f,
        ShipAPI.HullSize.CAPITAL_SHIP to 40f)

    var maxCrew = mapOf(
        ShipAPI.HullSize.FRIGATE to 25f,
        ShipAPI.HullSize.DESTROYER to 35f,
        ShipAPI.HullSize.CRUISER to 60f,
        ShipAPI.HullSize.CAPITAL_SHIP to 85f)

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats!!.variant.removePermaMod(HullMods.AUTOMATED)
        }

        stats.minCrewMod.modifyFlat("rat_core_conversion", minCrew.get(hullSize)!!)
        stats.maxCrewMod.modifyFlat("rat_core_conversion", maxCrew.get(hullSize)!!)

        SpaceCoreSkill().apply(stats, stats.variant.hullSize, id, 2f)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
        SpaceCoreSkill().apply(ship!!.mutableStats, ship.hullSize, id, 2f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addPara("Replaces the ships AI-Components with a miniature bridge and integrates a cosmos core in to the ships subsystem. " +
                "This allows humans to crew the ship. It enables the bonuses from the \"Abyssal Adaptability\" hullmod and provides the cores skill to the ship.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "humans to crew the ship", "\"Abyssal Adaptability\"", "skill")

        var spaceSkill = Global.getSettings().getSkillSpec("rat_core_space")
        tooltip.addSpacer(10f)
        var spaceSkillImage = tooltip.beginImageWithText(spaceSkill.spriteName, 48f)
        SpaceCoreSkill().createCustomDescription(null, null, spaceSkillImage, tooltip.widthSoFar)
        tooltip.addImageWithText(0f)
    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return variant!!.hasHullMod("rat_abyssal_core") && (member!!.captain == null || member!!.captain.nameString == "")
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        if (!member!!.baseOrModSpec().hasTag("rat_abyssals")) {
            tooltip!!.addPara("Can only be installed on hulls with the \"Abyssal Core\" hullmod.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else {
            tooltip!!.addPara("Can not be installed while an AI core is assigned to the ship.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }

    override fun canUninstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return false
    }

    override fun cannotUninstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        tooltip!!.addPara("Can not be removed while a core is integrated.", 0f,
            Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }

    override fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {
        variant!!.addPermaMod(HullMods.AUTOMATED)
    }
}

