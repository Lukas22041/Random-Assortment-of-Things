package assortment_of_things.relics.hullmods.alterations

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.addNegativePara
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class UpscaleProtocolHullmod : BaseAlteration() {

    var modID = "rat_upscale_protocol"

    private var dpFlat = 5f
    private var dpMult = 0.3f

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        var baseDp = stats!!.variant.baseOrModSpec().suppliesToRecover

        var increase = dpFlat

        if (baseDp + (baseDp * dpMult) >= baseDp + dpFlat) {
            increase = baseDp * dpMult
        }

        stats.suppliesToRecover.modifyFlat(modID, increase);
        stats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(modID, increase);



        stats.energyWeaponDamageMult.modifyMult(modID, 1.1f)
        stats.ballisticWeaponDamageMult.modifyMult(modID, 1.1f)
        stats.missileWeaponDamageMult.modifyMult(modID, 1.1f)

        stats.energyRoFMult.modifyMult(modID, 1.2f)
        stats.ballisticRoFMult.modifyMult(modID, 1.2f)
        stats.missileRoFMult.modifyMult(modID, 1.2f)

        stats.energyWeaponRangeBonus.modifyMult(modID, 1.15f)
        stats.ballisticWeaponRangeBonus.modifyMult(modID, 1.15f)
        stats.missileWeaponRangeBonus.modifyMult(modID, 1.15f)

        stats.armorBonus.modifyMult(modID, 1.25f)
        stats.hullBonus.modifyMult(modID, 1.25f)

        stats.fluxCapacity.modifyMult(modID, 1.25f)
        stats.fluxDissipation.modifyMult(modID, 1.25f)

        stats.maxSpeed.modifyMult(modID, 0.85f)
        stats.acceleration.modifyMult(modID, 0.85f)
        stats.deceleration.modifyMult(modID, 0.85f)
        stats.turnAcceleration.modifyMult(modID, 0.85f)
        stats.maxTurnRate.modifyMult(modID, 0.85f)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        var hc = Misc.getHighlightColor()
        var nc = Misc.getNegativeHighlightColor()

        tooltip!!.addSpacer(5f)

        var label = tooltip.addPara("Increases the ships deployment cost by ${dpFlat.toInt()} or 30%, based on which one is larger. This is calculated with the hulls base deployment cost.", 0f)
        label.setHighlight("deployment cost", "${dpFlat.toInt()}", "30%", "larger")
        label.setHighlightColors(hc, nc, nc, hc)

        tooltip.addSpacer(10f)

        var image = tooltip.beginImageWithText(Global.getSettings().getSpriteName("ui", "icon_tactical_cr_bonus"), 48f)
        image.addPara("10%% weapon damage", 0f, Misc.getTextColor(), Misc.getPositiveHighlightColor(), "10%")
        image.addPara("20%% weapon firerate", 0f, Misc.getTextColor(), Misc.getPositiveHighlightColor(), "20%")
        image.addPara("15%% increased weapon range", 0f, Misc.getTextColor(), Misc.getPositiveHighlightColor(), "15%")
        image.addPara("25%% increased armor and hitpoints", 0f, Misc.getTextColor(), Misc.getPositiveHighlightColor(), "25%")
        image.addPara("25%% increased flux dissipation & capacity", 0f, Misc.getTextColor(), Misc.getPositiveHighlightColor(), "25%")
        image.addPara("15%% decreased max speed", 0f, Misc.getTextColor(), Misc.getNegativeHighlightColor(), "15%")
        image.addPara("15%% decreased maneuverability", 0f, Misc.getTextColor(), Misc.getNegativeHighlightColor(), "15%")
        tooltip.addImageWithText(0f)

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?,  marketAPI: MarketAPI?): Boolean {
        return member!!.isFrigate || member.isDestroyer
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?,variant: ShipVariantAPI?,width: Float) {
        tooltip!!.addNegativePara("Can only be installed on frigates or destroyers.")
    }
}