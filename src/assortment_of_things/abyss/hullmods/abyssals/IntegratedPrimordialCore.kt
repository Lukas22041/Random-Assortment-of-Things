package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.abyss.skills.PrimordialCoreSkill
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class IntegratedPrimordialCore : BaseAlteration() {


    var minCrew = mapOf(
        HullSize.FRIGATE to 10f,
        HullSize.DESTROYER to 15f,
        HullSize.CRUISER to 25f,
        HullSize.CAPITAL_SHIP to 40f)

    var maxCrew = mapOf(
        HullSize.FRIGATE to 20f,
        HullSize.DESTROYER to 30f,
        HullSize.CRUISER to 50f,
        HullSize.CAPITAL_SHIP to 80f)

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)


        if (stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats!!.variant.removePermaMod(HullMods.AUTOMATED)
        }

        stats.minCrewMod.modifyFlat("rat_core_conversion", minCrew.get(hullSize)!!)
        stats.maxCrewMod.modifyFlat("rat_core_conversion", maxCrew.get(hullSize)!!)

        PrimordialCoreSkill().apply(stats, stats.variant.hullSize, id, 2f)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
        PrimordialCoreSkill().apply(ship!!.mutableStats, ship.hullSize, id, 2f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Replaces the ships AI-Components with a miniature bridge and integrates a primordial core in to the ships subsystem. " +
                "This allows humans to crew the ship and provides the cores skill to the ship.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "humans to crew the ship", "skill")

        var seraphSkill = Global.getSettings().getSkillSpec("rat_core_primordial")
        tooltip.addSpacer(10f)
        var primordialSkillImg = tooltip.beginImageWithText(seraphSkill.spriteName, 48f)
        PrimordialCoreSkill().createCustomDescription(null, null, primordialSkillImg, tooltip.widthSoFar)
        tooltip.addImageWithText(0f)

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return variant!!.hasHullMod("rat_abyssal_core") && (member!!.captain == null || member!!.captain.nameString == "")
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?,  member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
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

    override fun cannotUninstallAlterationTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?,width: Float) {
        tooltip!!.addPara("Can not be removed while a core is integrated.", 0f,
            Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }

    override fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {
        variant!!.addPermaMod(HullMods.AUTOMATED)
    }
}

