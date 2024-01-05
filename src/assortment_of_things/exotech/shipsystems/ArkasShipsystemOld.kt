package assortment_of_things.exotech.shipsystems

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ArkasShipsystemOld : BaseShipSystemScript() {

    var SPEED_BONUS = 75f
    var TURN_BONUS = 10f

    private val color = ExoUtils.color2
    var interval = IntervalUtil(0.5f, 0.8f)


    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {

        stats.fluxDissipation.modifyMult(id, 1 + (0.33f * effectLevel))

        stats.maxSpeed.modifyFlat(id, SPEED_BONUS * effectLevel)
        stats.acceleration.modifyPercent(id, SPEED_BONUS * 3f * effectLevel)
        stats.deceleration.modifyPercent(id, SPEED_BONUS * 3f * effectLevel)
        stats.turnAcceleration.modifyFlat(id, TURN_BONUS * effectLevel)
        stats.turnAcceleration.modifyPercent(id, TURN_BONUS * 5f * effectLevel)
        stats.maxTurnRate.modifyFlat(id, 15f * effectLevel)
        stats.maxTurnRate.modifyPercent(id, 100f * effectLevel)
        if (stats.entity is ShipAPI) {
            val ship = stats.entity as ShipAPI
            ship.engineController.fadeToOtherColor(this, color, Color(0, 0, 0, 0), effectLevel, 0.67f)
            //ship.getEngineController().fadeToOtherColor(this, Color.white, new Color(0,0,0,0), effectLevel, 0.67f);
            ship.engineController.extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel)

            if (ship.isPhased) {
                if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
                    ship.system.forceState(ShipSystemAPI.SystemState.OUT, 0f)
                }
            }

            interval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (interval.intervalElapsed() && !Global.getCombatEngine().isPaused && !ship.isPhased) {

                var coilLocationDeco = ship.allWeapons.find { it.spec.weaponId == "rat_exo_coil_location" }

                var from = Vector2f(coilLocationDeco!!.location)

                var entities = Global.getCombatEngine().allObjectGrid.getCheckIterator(coilLocationDeco.location, 600f, 600f)
                for (entity in entities) {
                    if (entity is MissileAPI) {
                        if (entity.owner == ship.owner) continue
                        if (entity.maxHitpoints != 0f) {

                            val emp: Float = 75f
                            val dam: Float = 150f

                            Global.getCombatEngine().spawnEmpArc(ship,
                                coilLocationDeco!!.location,
                                ship,
                                entity,
                                DamageType.ENERGY,
                                dam,
                                emp,  // emp
                                100000f,  // max range
                                "tachyon_lance_emp_impact",
                                10f,  // thickness
                                color,
                                color)

                            break
                        }
                    }
                }

            }


        }


    }

    override fun unapply(stats: MutableShipStatsAPI, id: String?) {
    /*    stats.maxSpeed.unmodify(id)
        stats.maxTurnRate.unmodify(id)
        stats.turnAcceleration.unmodify(id)
        stats.acceleration.unmodify(id)
        stats.deceleration.unmodify(id)*/
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float): StatusData? {
        if (index == 0) {
            return StatusData("improved maneuverability", false)
        } else if (index == 1) {
            return StatusData("+" + SPEED_BONUS.toInt() + " top speed", false)
        }
        return null
    }

}