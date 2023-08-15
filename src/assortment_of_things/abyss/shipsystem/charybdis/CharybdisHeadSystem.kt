package assortment_of_things.abyss.shipsystem.charybdis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class CharybdisHeadSystem : BaseShipSystemScript() {

    var JITTER_COLOR = Color(255, 175, 255, 255)
    var JITTER_FADE_TIME = 0.5f
    var SHIP_ALPHA_MULT = 0.25f

    var VULNERABLE_FRACTION = 0f
    var INCOMING_DAMAGE_MULT = 0.25f
    var MAX_TIME_MULT = 2f

    var FLUX_LEVEL_AFFECTS_SPEED = false
    var MIN_SPEED_MULT = 0.33f
    var BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f

    fun getMaxTimeMult(stats: MutableShipStatsAPI): Float {
        return 1f + (MAX_TIME_MULT - 1f) * stats.dynamic.getValue(Stats.PHASE_TIME_BONUS_MULT)
    }

    protected fun getDisruptionLevel(ship: ShipAPI?): Float {

        if (FLUX_LEVEL_AFFECTS_SPEED) {
            val threshold = ship!!.mutableStats.dynamic.getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD)
                .computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED)
            if (threshold <= 0) return 1f
            var level = ship.hardFluxLevel / threshold
            if (level > 1f) level = 1f
            return level
        }

        return 0f
    }



    fun getSpeedMult(ship: ShipAPI?, effectLevel: Float): Float {
        return if (getDisruptionLevel(ship) <= 0f) 1f else MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(
            ship) * effectLevel)
    }

    fun isActive(state: ShipSystemStatsScript.State) = state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.ACTIVE


    override fun apply(stats: MutableShipStatsAPI?,id: String?, state: ShipSystemStatsScript.State,  effectLevel: Float) {

        var ship = stats!!.entity
        if (ship !is ShipAPI) return

      /*  disableWeapons(ship, state)
        applyForAll(ship, stats, id!!, state, effectLevel)*/

        if (state == State.OUT) {
            var isColliding = false
            for (module in ship.childModulesCopy + ship) {

                var iter = Global.getCombatEngine().allObjectGrid.getCheckIterator(module.location, module.collisionRadius, module.collisionRadius)
                for (it in iter) {
                    if (ship.childModulesCopy.contains(it)) continue
                    if (it == ship) continue
                    if (it !is ShipAPI) continue
                    if (it.isFighter) continue

                    var colRadius = (it.collisionRadius / 2 + module.collisionRadius / 2) * 1.1f

                    if (MathUtils.getDistance(it.location, module.location) < colRadius)  {
                        isColliding = true
                    }

                }
            }

            if (isColliding) {
                ship.phaseCloak.forceState(ShipSystemAPI.SystemState.ACTIVE, 0.5f)
            }
        }


        var segments = ship.childModulesCopy + ship
        for (module in segments) {

            disableWeapons(module, state)
            applyForAll(module, module.mutableStats, id!!, state!!, effectLevel)

            if (isActive(state)) {
                module.mutableStats.fluxDissipation.modifyMult("rat_charybdisphase_flux", 0f)
            }
            else
            {
                module.mutableStats.fluxDissipation.modifyMult("rat_charybdisphase_flux", 1f)
            }
        }
    }


    fun disableWeapons(ship: ShipAPI, state: State) {

        if (isActive(state)) {
            ship.isHoldFireOneFrame = true
            ship.blockCommandForOneFrame(ShipCommand.FIRE)

            for (weapon in ship.allWeapons) {

            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {

    }

    fun applyForAll(ship: ShipAPI, stats: MutableShipStatsAPI, id: String, state: ShipSystemStatsScript.State, effectLevel: Float) {
        var id = id
        var player = false
        player = ship === Global.getCombatEngine().playerShip
        id = id + "_" + ship!!.id

        if (player) {
           // maintainStatus(ship, state, effectLevel)
        }
        if (Global.getCombatEngine().isPaused) {
            return
        }


       /* if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (state == ShipSystemStatsScript.State.ACTIVE || state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.IN) {
                val mult = getSpeedMult(ship, effectLevel)
                if (mult < 1f) {
                    stats.maxSpeed.modifyMult(id + "_2", mult)
                } else {
                    stats.maxSpeed.unmodifyMult(id + "_2")
                }
            }
        }*/
        if (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE) {
            unapplyForAll(ship, stats, id)
            return
        }

        val speedPercentMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f)
        val accelPercentMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f)
        stats.maxSpeed.modifyPercent(id, speedPercentMod * effectLevel)
        stats.acceleration.modifyPercent(id, accelPercentMod * effectLevel)
        stats.deceleration.modifyPercent(id, accelPercentMod * effectLevel)
        val speedMultMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult()
        val accelMultMod = stats.dynamic.getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult()
        stats.maxSpeed.modifyMult(id, speedMultMod * effectLevel)
        stats.acceleration.modifyMult(id, accelMultMod * effectLevel)
        stats.deceleration.modifyMult(id, accelMultMod * effectLevel)
        //float f = VULNERABLE_FRACTION;
        val jitterLevel = 0f
        val jitterRangeBonus = 0f
        var levelForAlpha = effectLevel

        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            ship.isPhased = true
            levelForAlpha = effectLevel
        } else if (state == ShipSystemStatsScript.State.OUT) {
            if (effectLevel > 0.5f) {
                ship.isPhased = true
            } else {
                ship.isPhased = false
            }
            levelForAlpha = effectLevel

        }

        ship.extraAlphaMult = 1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha
        ship.setApplyExtraAlphaToEngines(true)

        val extra = 0f

        val shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha * (1f - extra)
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)

        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

    }

    fun unapplyForAll(ship: ShipAPI, stats: MutableShipStatsAPI, id: String) {

        Global.getCombatEngine().timeMult.unmodify(id)
        stats.timeMult.unmodify(id)
        stats.maxSpeed.unmodify(id)
        stats.maxSpeed.unmodifyMult(id + "_2")
        stats.acceleration.unmodify(id)
        stats.deceleration.unmodify(id)
        ship!!.isPhased = false
        ship.extraAlphaMult = 1f
        var cloak = ship.phaseCloak
        if (cloak == null) cloak = ship.system
        if (cloak != null) {
            (cloak as PhaseCloakSystemAPI).minCoilJitterLevel = 0f
        }

    }


    companion object {

    }
}