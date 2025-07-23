package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.skills.PrimordialCoreSkill
import assortment_of_things.abyss.skills.SeraphCoreSkill
import assortment_of_things.abyss.skills.SpaceCoreSkill
import assortment_of_things.abyss.skills.TimeCoreSkill
import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.misc.SkillWidgetElement
import assortment_of_things.misc.addPara
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.magiclib.kotlin.isAutomated

class AbyssalCrewConversion : BaseAlteration() {

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

    //Null if its the base conversion
    fun getCoreSkill() : RATBaseShipSkill? {
        return when (spec?.id) {
            "rat_chronos_conversion" -> TimeCoreSkill()
            "rat_cosmos_conversion" -> SpaceCoreSkill()
            "rat_seraph_conversion" -> SeraphCoreSkill()
            "rat_primordial_conversion" -> PrimordialCoreSkill()
            else -> null
        }
    }

    fun getCoreSkillSpec() : SkillSpecAPI? {
        return when (spec?.id) {
            "rat_chronos_conversion" -> Global.getSettings().getSkillSpec("rat_core_time")
            "rat_cosmos_conversion" -> Global.getSettings().getSkillSpec("rat_core_space")
            "rat_seraph_conversion" -> Global.getSettings().getSkillSpec("rat_core_seraph")
            "rat_primordial_conversion" -> Global.getSettings().getSkillSpec("rat_core_primordial")
            else -> null
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

       /* if (stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats!!.variant.removePermaMod(HullMods.AUTOMATED)

            for (moduleString in stats.variant.moduleSlots) {
                var module = stats.variant.getModuleVariant(moduleString)
                module.removePermaMod(HullMods.AUTOMATED)
            }
        }*/

       /* stats.minCrewMod.modifyFlat("rat_core_conversion", minCrew.get(hullSize)!!)
        stats.maxCrewMod.modifyFlat("rat_core_conversion", maxCrew.get(hullSize)!!)*/

        var member = stats!!.fleetMember
        if (member != null) {
            //Apply Auto Points Mult
            var captain = member.captain
            if (captain != null && !captain.isDefault) {

                var mult = 2f
                if (getCoreSkill() != null) mult = 3f

                captain.memoryWithoutUpdate.set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, mult)
            }
        }

        if (!Misc.getAllowedRecoveryTags().contains(Tags.AUTOMATED_RECOVERABLE)) {
            stats.maxCombatReadiness.modifyFlat(id, 0.7f, "Abyssal Crew Conversion")
        }

        //Apply Core Skill
        getCoreSkill()?.apply(stats, stats.variant.hullSize, id, 2f)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        //Apply Core Skill
        getCoreSkill()?.apply(ship!!.mutableStats, ship.hullSize, id, 2f)
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Makes place for a miniature bridge in automated hulls, enabling humans to crew the ship. \n\n" +
                "Additionaly, a sub-interface enables the integration of an Abyssal AI Core from within the \"Additional Options\" menu. " +
                "" +
                "Integrated cores provide the effects of their signature skill and trigger the effects of ships with the \"Abyssal Adaptability\" hullmod. \n\n" +
                "" +
                "Officers assigned to the ship count as a 2${Strings.X} multiplier to the automated ship points cost. Integrating a core increases this to a 3${Strings.X} multiplier. " +
                "Increases maximum combat readiness by 70%% if you are not capable of fielding automated ships.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),
            "enabling humans to crew the ship", "Abyssal AI Core", "Additional Options", "signature skill", "\"Abyssal Adaptability\"", "2${Strings.X}", "3${Strings.X}", "70%")

        var skillSpec = getCoreSkillSpec()
        var skillPlugin = getCoreSkill()

        if (skillSpec != null && skillPlugin != null) {

            tooltip.addSpacer(10f)
            tooltip.addSectionHeading("Integrated Core", Alignment.MID, 0f)
            tooltip.addSpacer(10f)

            var img = skillSpec.spriteName
            var color = skillSpec.governingAptitudeColor

            var widget = SkillWidgetElement(true, true, img, color, tooltip, 48f, 48f)
            //widget.elementPanel.position.setXAlignOffset(10f)
            var height = tooltip.heightSoFar

            var anchor = tooltip.addLunaElement(0f, 0f)
            anchor.position.rightOfTop(widget.elementPanel, 10f)
            var title = tooltip.addTitle("Integrated Signature Skill", Misc.getBasePlayerColor())
            title.position.belowLeft(anchor.elementPanel, 8f)
            tooltip.addPara("${skillSpec.name}", 0f)

            tooltip.heightSoFar = height
            tooltip.addPara("").position.inTL(5f, height-5)
            //tooltip.addPara("Test")
            skillPlugin.createCustomDescription(null, null, tooltip, tooltip.widthSoFar)

            widget.position.setXAlignOffset(tooltip.widthSoFar/2-widget.width/2-title.computeTextWidth(title.text)/2-5)

        }

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return member?.isAutomated() == true && (member!!.captain == null || member!!.captain.nameString == "" || member.captain?.hasTag("rat_neuro_shard") == true)
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?,  member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        if (member?.isAutomated() != true) {
            tooltip!!.addPara("Can only be installed on automated ships.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else {
            tooltip!!.addPara("Can not be installed while an AI core is assigned to the ship.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }

    override fun canUninstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return spec?.id == "rat_abyssal_conversion" && (member!!.captain == null || member!!.captain.nameString == "" || member.captain?.hasTag("rat_neuro_shard") == true)
    }

    override fun cannotUninstallAlterationTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?,width: Float) {

        if (spec?.id != "rat_abyssal_conversion") {
            tooltip!!.addPara("Can not be removed while a core is integrated.", 0f,
                Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        } else {
            tooltip!!.addPara("Can not be removed while an officer is assigned to the ship.", 0f,
                Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }


    }


    override fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {

    }
}

