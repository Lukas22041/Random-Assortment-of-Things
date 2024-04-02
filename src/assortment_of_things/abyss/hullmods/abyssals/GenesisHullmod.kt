package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.activators.PerseveranceActivator
import assortment_of_things.abyss.boss.GenesisBossScript
import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import assortment_of_things.abyss.shipsystem.activators.PrimordialSeaActivator
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha

class GenesisHullmod : BaseHullMod() {

    var color = AbyssUtils.GENESIS_COLOR
    var afterimageInterval = IntervalUtil(0.1f, 0.1f)

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        if (!stats!!.variant.hasHullMod("rat_abyssal_conversion") && !stats!!.variant.hasHullMod("rat_chronos_conversion") && !stats!!.variant.hasHullMod("rat_cosmos_conversion") && !stats!!.variant.hasHullMod("rat_seraph_conversion")  && !stats.variant.hasHullMod(
                HullMods.AUTOMATED)) {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
        }

        if (stats.fleetMember?.fleetData?.fleet?.faction?.id == "rat_abyssals_primordials") {
            stats.crewLossMult.modifyMult("test", 0f)
            stats.crLossPerSecondPercent.modifyMult("test", 0f)
        } else {
            if (Global.getSector()?.characterData?.person != null) {
                if (Global.getSector().characterData.person!!.stats.hasSkill(Skills.AUTOMATED_SHIPS)
                    || stats!!.variant.hasHullMod("rat_abyssal_conversion") ||
                    stats!!.variant.hasHullMod("rat_chronos_conversion") || stats!!.variant.hasHullMod("rat_cosmos_conversion") || stats!!.variant.hasHullMod("rat_seraph_conversion")) {
                    if (!stats.variant.hasTag("rat_really_not_recoverable")) {
                        stats!!.variant.removeTag(Tags.VARIANT_UNBOARDABLE)
                    }
                }
                else {
                    stats!!.variant.addTag(Tags.VARIANT_UNBOARDABLE)
                }
            }
        }


        /*stats!!.dynamic.getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -2f)
        stats!!.dynamic.getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -4f)
        stats!!.dynamic.getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -6f)*/

        stats.energyWeaponDamageMult.modifyMult(id, 1.2f)
        stats.energyRoFMult.modifyMult(id, 1.2f)
        stats!!.energyWeaponFluxCostMod.modifyMult(id, 0.8f)

        stats!!.energyWeaponRangeBonus.modifyFlat(id, 200f)
        //stats!!.beamWeaponRangeBonus.modifyMult(id, 100f)

        stats.empDamageTakenMult.modifyMult(id, 0.5f)

        stats!!.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 0f);
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship.mutableStats?.fleetMember?.fleetData?.fleet?.faction?.id == "rat_abyssals_primordials") {

            Global.getCombatEngine().addLayeredRenderingPlugin(GenesisBossScript(ship))
        }

        if (ship.shield != null) {
            ship.shield.setRadius(ship.shieldRadiusEvenIfNoShield, "graphics/fx/rat_primordial_shields256.png", "graphics/fx/rat_primordial_shields256ring.png")
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

   /* override fun affectsOPCosts(): Boolean {
        return true
    }*/

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
        if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused && ship!!.isAlive)
        {
            AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(50), color.setAlpha(0), 3.5f, 0f, Vector2f().plus(ship!!.location))
        }

       // ship!!.setJitter(this, color.setAlpha(25), 1f, 3, 0f, 0f)
        //ship!!.setJitterUnder(this, color.setAlpha(75), 1f, 25, 0f, 14f)
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  width: Float, isForModSpec: Boolean) {

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight, color.brighter())
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("A ship thats warping the space around it just by its sheer existance.\n\n" +
                "Energy weapons operate at 20%% increased damage, fire rate, and lower flux cost. They also have an increased base range of 200 units." +
                "\n\n" +
                "The ships distorted grid takes 50%% less emp damage and has full immunity against abyssal storms and similar hazards." +
                "",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "\"Primordial Sea\"","Energy weapons", "20%", "200", "2", "4", "6", "50%", "abyssal storms")



        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
        }

    }




    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }
}