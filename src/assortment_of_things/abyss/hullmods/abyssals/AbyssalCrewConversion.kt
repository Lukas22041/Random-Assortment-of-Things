package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

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

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)


        if (stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats!!.variant.removePermaMod(HullMods.AUTOMATED)

            for (moduleString in stats.variant.moduleSlots) {
                var module = stats.variant.getModuleVariant(moduleString)
                module.removePermaMod(HullMods.AUTOMATED)
            }
        }
        stats.minCrewMod.modifyFlat("rat_core_conversion", minCrew.get(hullSize)!!)
        stats.maxCrewMod.modifyFlat("rat_core_conversion", maxCrew.get(hullSize)!!)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("Replaces the AI-Components in abyssal hulls with a miniature bridge and leaves a subterminal for the integration of either a chronos, cosmos or seraph core, allowing humans to crew the ship. \n\n" +
                "The subterminal allows an integrated core to activate the additional effects on ships with the \"Abyssal Adaptability\" hullmod.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "subterminal", "chronos", "cosmos", "seraph", "humans to crew the ship", "subterminal", "\"Abyssal Adaptability\"")

        tooltip.addSpacer(10f)

        tooltip!!.addPara("Cores can be integrated from the \"Additional Options\" button at the top of the refit screen if this alteration is installed.",0f,
            Misc.getGrayColor(), Misc.getGrayColor(), "")

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return (variant!!.hasHullMod("rat_abyssal_core") || variant!!.hasHullMod("rat_seraphs_grace") ||  variant!!.hasHullMod("rat_genesis_hullmod") || variant!!.hasHullMod("rat_genesis_serpent_hullmod")) && (member!!.captain == null || member!!.captain.nameString == "")
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?,  member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        if (!member!!.baseOrModSpec().hasTag("rat_abyssals")) {
            tooltip!!.addPara("Can only be installed on abyssal hulls.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else {
            tooltip!!.addPara("Can not be installed while an AI core is assigned to the ship.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }

    override fun canUninstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return member!!.captain == null || member!!.captain.nameString == ""
    }

    override fun cannotUninstallAlterationTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?,width: Float) {
        tooltip!!.addPara("Can not be removed while an officer is assigned to the ship.", 0f,
            Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }


    override fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {
        variant!!.addPermaMod(HullMods.AUTOMATED)

        for (moduleString in variant.moduleSlots) {
            var module = variant.getModuleVariant(moduleString)
            module.addPermaMod(HullMods.AUTOMATED)
        }
    }
}

