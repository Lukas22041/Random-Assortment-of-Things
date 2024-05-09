package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssalChargeShipsystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.1f, 0.1f)
    var empInterval = IntervalUtil(0.2f, 0.2f)

    var activated = false

    var distance = 1500f

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        var ship = stats!!.entity as ShipAPI
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        var color = AbyssalsAdaptabilityHullmod.getColorForCore(ship!!)
        var secondaryColor = AbyssalsAdaptabilityHullmod.getSecondaryColorForCore(ship!!)

        if (ship!!.system.isActive)
        {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.enableBlink()
        }
        else
        {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.disableBlink()
        }

        var emitters = ship.allWeapons.filter { it.spec?.weaponId == "rat_morkoth_coils_location" }

        if (!activated && state == ShipSystemStatsScript.State.OUT) {
            activated = true


            var allies = AIUtils.getNearbyAllies(ship, distance)
            for (ally in allies) {
                if (!ally.isAlive) continue
                ally.removeListenerOfClass(AbyssalChargeBuffListener::class.java)
                ally.addListener(AbyssalChargeBuffListener(ally, color, secondaryColor))
                Global.getSoundPlayer().playSound("system_emp_emitter_impact", 0.9f, 1.1f, ally.location, ally.velocity)
            }

            Global.getSoundPlayer().playSound("disabled_large_crit", 0.9f, 0.9f, ship.location, ship.velocity)

        }



        if (state == ShipSystemStatsScript.State.IDLE || state == ShipSystemStatsScript.State.COOLDOWN) {
            activated = false
        }

        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            //ship!!.giveCommand(ShipCommand.ACCELERATE, null, 0)
        }

        if (ship!!.system.isActive) {
            ship!!.engineController.extendFlame(this, 1f * effectLevel, 0.2f * effectLevel, 0.5f * effectLevel)
            ship!!.setJitter(this, color.setAlpha(55), 1f * effectLevel, 3, 0f, 0f)
            ship!!.setJitterUnder(this, color.setAlpha(100), 1f * effectLevel, 25, 0f, 14f)
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(100), secondaryColor.setAlpha(100), 2f, 2f, Vector2f().plus(ship!!.location))
            }

            empInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (empInterval.intervalElapsed() && !Global.getCombatEngine().isPaused) {
                ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                var from = Vector2f(ship!!.exactBounds.segments.random().p1)
                var to = Vector2f(ship!!.exactBounds.segments.random().p1)

                var empColor = Misc.interpolateColor(color, secondaryColor, Random().nextFloat())
            //    Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, empColor, empColor)

                if (effectLevel >= 0.5f && state == ShipSystemStatsScript.State.OUT) {
                    for (ally in AIUtils.getNearbyAllies(ship, distance + 100) ) {
                        if (ally.hasListenerOfClass(AbyssalChargeBuffListener::class.java)) {
                            if (!ally.isAlive) continue

                            ally?.exactBounds?.update(ally.location, ally.facing) ?: continue
                            to = ally?.exactBounds?.segments?.random()?.p1 ?: continue

                            empColor = Misc.interpolateColor(color.setAlpha(255), secondaryColor.setAlpha(255), Random().nextFloat() * 0.8f)
                            var closest = emitters.minByOrNull { MathUtils.getDistance(ally, it.location) } ?: continue
                            Global.getCombatEngine().spawnEmpArcVisual(closest.location, ship, to, SimpleEntity(to), 10f, empColor.setAlpha(75), empColor.setAlpha(75))
                        }
                    }
                }
            }
        }



        var mult = 1.5f
        val shipTimeMult = 1f + (mult - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        stats.maxSpeed.modifyFlat(id, 225f * effectLevel)
        stats.acceleration.modifyFlat(id, 100 * effectLevel)
        stats.acceleration.modifyMult(id, 1 + (10f * effectLevel))
        stats.deceleration.modifyMult(id, 1 + (10f * effectLevel))
        stats.turnAcceleration.modifyMult(id, 1 - (0.6f * effectLevel))
        stats.maxTurnRate.modifyMult(id, 1 - (0.6f * effectLevel))

    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        super.unapply(stats, id)

        var ship = stats!!.entity as ShipAPI

        if (ship != null) {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.disableBlink()
        }

        activated = false
    }

}

class AbyssalChargeBuffListener(var ship: ShipAPI, var color: Color, var secondaryColor: Color) : AdvanceableListener {

    var maxTime = 7f
    var outTime = 2f
    var timer = maxTime



    var afterimageInterval = IntervalUtil(0.2f, 0.2f)

    override fun advance(amount: Float) {
        timer -= 1 * amount

        var level = timer / outTime
        level = MathUtils.clamp(level, 0f, 1f)

        ship!!.setJitter(this, color.setAlpha(55), 1f * level, 3, 0f, 0f)
        ship!!.setJitterUnder(this, color.setAlpha(125), 1f * level, 15, 2f, 10f)

        var stats = ship.mutableStats
        stats.timeMult.modifyMult("rat_abyssal_charge", 1 + (0.1f * level))
        stats.maxSpeed.modifyMult("rat_abyssal_charge", 1 + (0.1f * level))
        stats.acceleration.modifyMult("rat_abyssal_charge", 1 + (0.25f * level))
        stats.deceleration.modifyMult("rat_abyssal_charge", 1 + (0.25f * level))

        stats.fluxDissipation.modifyMult("rat_abyssal_charge", 1 + (0.25f * level))

        stats.energyWeaponFluxCostMod.modifyMult("rat_abyssal_charge", 1 - (0.1f * level))
        stats.ballisticWeaponFluxCostMod.modifyMult("rat_abyssal_charge", 1 - (0.1f * level))

        afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
        if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
        {
            AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(100), secondaryColor.setAlpha(100), 1f, 2f, Vector2f().plus(ship!!.location))
        }

        if (ship.system != null) {

            if (ship.system.state == ShipSystemAPI.SystemState.COOLDOWN) {
                ship.system.cooldownRemaining -= 1f * amount * level
            }
            ship.system.ammoReloadProgress += ship.system.ammoPerSecond * amount * level
        }

        if (timer <= 0) {
            ship.removeListener(this)
        }
    }

}