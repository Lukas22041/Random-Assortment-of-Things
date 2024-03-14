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
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
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
import java.util.*

class GenesisConstructHullmod : BaseHullMod() {



    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        stats!!.weaponTurnRateBonus.modifyMult(id, 1.5f)

        stats!!.energyWeaponDamageMult.modifyMult(id, 1.2f)
        stats.energyRoFMult.modifyMult(id, 1.2f)
        stats!!.energyWeaponFluxCostMod.modifyMult(id, 0.8f)

        stats!!.energyWeaponRangeBonus.modifyFlat(id, 200f)

        stats.empDamageTakenMult.modifyMult(id, 0.5f)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship.shield != null) {
            ship.shield.setRadius(ship.shieldRadiusEvenIfNoShield, "graphics/fx/rat_primordial_shields256.png", "graphics/fx/rat_primordial_shields256ring.png")
        }
        ship.addListener(GenesisAfterImageRenderer(ship))
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight, AbyssUtils.GENESIS_COLOR.brighter())
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("" +
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

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }

    class GenesisAfterImageRenderer(var ship: ShipAPI) : AdvanceableListener {

        var color = AbyssUtils.GENESIS_COLOR
        var afterimageInterval = IntervalUtil(0.1f, 0.1f)

        override fun advance(amount: Float) {
            /*afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused && ship!!.isAlive)
            {
                var image = AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(50), color.setAlpha(0), 1f, 0f, Vector2f().plus(ship!!.location))
            }*/
        }

    }
}