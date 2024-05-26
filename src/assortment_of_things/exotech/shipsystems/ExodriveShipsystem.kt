package assortment_of_things.exotech.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData

class ExodriveShipsystem : BaseShipSystemScript() {


    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {
        var ship = stats.entity as ShipAPI? ?: return
        var isPhase = ship.phaseCloak != null
        var player = Global.getCombatEngine().playerShip == ship

        if (isPhase) {
            var levelForAlpha = effectLevel

            if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
                ship.isPhased = true
                levelForAlpha = effectLevel
            } else if (state == ShipSystemStatsScript.State.OUT) {
                ship.isPhased = effectLevel > 0.5f
                levelForAlpha = effectLevel
            }

            levelForAlpha *= 2f
            levelForAlpha = levelForAlpha.coerceIn(0f, 1f)

            ship.extraAlphaMult = 1f - (1f - ExophaseShipsystem.SHIP_ALPHA_MULT) * levelForAlpha
            ship.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small

            ship.engineController.fadeToOtherColor(this,
                ExophaseShipsystem.ENGINE_COLOR,
                ExophaseShipsystem.ENGINE_COLOR, 1f * effectLevel, 1f)
            ship.engineController.extendFlame(this, -0.25f * effectLevel, -0.25f * effectLevel, 0f)

            val shipTimeMult = 1 + 0.1f * levelForAlpha
            stats.timeMult.modifyMult(id, shipTimeMult)
            if (player) {
                Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
            } else {
                Global.getCombatEngine().timeMult.unmodify(id)
            }
        }

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.maxSpeed.unmodify(id) // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.maxSpeed.modifyFlat(id, 600f * effectLevel)
            stats.acceleration.modifyFlat(id, 600f * effectLevel)
            //stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
        }
    }

    override fun unapply(stats: MutableShipStatsAPI, id: String?) {
        stats.maxSpeed.unmodify(id)
        stats.maxTurnRate.unmodify(id)
        stats.turnAcceleration.unmodify(id)
        stats.acceleration.unmodify(id)
        stats.deceleration.unmodify(id)
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float): StatusData? {
        if (index == 0) {
            return StatusData("increased engine power", false)
        }
        else {
            return null
        }
    }

}