package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
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

class IntegratedChronosCore : BaseAlteration() {


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
            if (stats.fleetMember.captain != null)
            {
                if (stats.fleetMember.captain.isAICore)
                {
                    Global.getSector().playerFleet.cargo.addCommodity(stats.fleetMember.captain.aiCoreId, 1f)
                }
                stats.fleetMember.captain = null
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
        tooltip!!.addPara("Replaces the ships AI-Components with a miniature bridge and integrates a chronos core in to the ships subsystem. " +
                "This allows humans to crew the ship, while enabling the chronos-core related shipsystem.", 0f)

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return member!!.baseOrModSpec().hasTag("rat_abyssals") && (member!!.captain == null || member!!.captain.nameString == "")
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?,  member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        if (!member!!.baseOrModSpec().hasTag("rat_abyssals")) {
            tooltip!!.addPara("Can only be installed in to abyssal hulls.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else {
            tooltip!!.addPara("Can only be installed when no AI core is assigned to the ship.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
    }

    override fun canUninstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {
        return member!!.captain == null || member!!.captain.nameString == ""
    }

    override fun cannotUninstallAlterationTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?,width: Float) {
        tooltip!!.addPara("Can only be removed if no Officer is assigned to the ship.", 0f,
            Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
    }

    override fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {
        variant!!.addPermaMod(HullMods.AUTOMATED)
    }
}

