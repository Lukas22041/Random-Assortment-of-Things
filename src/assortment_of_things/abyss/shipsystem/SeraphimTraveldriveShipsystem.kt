package assortment_of_things.abyss.shipsystem

import assortment_of_things.misc.GraphicLibEffects
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class SeraphimTraveldriveShipsystem : BaseShipSystemScript() {

    var activated = false

    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {
        var ship = stats.entity as ShipAPI? ?: return
        var isPhase = ship.phaseCloak != null
        var player = Global.getCombatEngine().playerShip == ship

        var levelForAlpha = effectLevel

        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            ship.isPhased = true
            levelForAlpha = effectLevel
        } else if (state == ShipSystemStatsScript.State.OUT) {
            ship.isPhased = effectLevel > 0.5f
            levelForAlpha = effectLevel
        }

        var alpha = SeraphimDriveShipsystem.SHIP_ALPHA_MULT
        var inLevel = (effectLevel - 0.8f) / (0.5f - 0.8f)
      /*  inLevel = 1-inLevel*/
        inLevel = inLevel.coerceIn(0f, 1f)

        alpha *= inLevel

        if (alpha > 0 && state == ShipSystemStatsScript.State.OUT && !activated) {
            activated = true

           /* GraphicLibEffects.CustomRippleDistortion(Vector2f(ship.location), Vector2f(ship.velocity.x * 0.5f, ship.velocity.y * 0.5f), 500f, 100f, true, ship.facing, 360f, 1f
                ,0.1f, 1f, 1f, 1f, 1f)*/

            GraphicLibEffects.CustomBubbleDistortion(Vector2f(ship.location), Vector2f(), 300f + ship.collisionRadius, 10f, true, ship.facing, 360f, 1f
                ,0.1f, 0.1f, 1f, 0.3f, 1f)
        }

        levelForAlpha *= 2f
        levelForAlpha = levelForAlpha.coerceIn(0f, 1f)

        ship.extraAlphaMult = 1f - (1f - alpha) * levelForAlpha

        if (inLevel < 0.1f) {
            ship.setApplyExtraAlphaToEngines(true) //Disable to make engines not get way to small
        }
        else {
            ship.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small
        }

        ship.setCustomData("rat_burndrive_level_overwrite_in", inLevel)

        var color = Color(242, 48, 65, 150)

        ship.engineController.fadeToOtherColor(this,
            color.setAlpha(alpha.toInt()),
            color.setAlpha(alpha.toInt()), 1f * effectLevel, 1f)
        ship.engineController.extendFlame(this, -0.25f * effectLevel, -0.25f * effectLevel, 0f)

        val shipTimeMult = 1 + 0.1f * levelForAlpha
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.maxSpeed.unmodify(id) // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.maxSpeed.modifyFlat(id, 600f * effectLevel)
            stats.acceleration.modifyFlat(id, 600f * effectLevel)
            //stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
        }

        var fighters = ship.allWings.flatMap { it.wingMembers }.toMutableList()
        fighters.addAll(ship.allWings.flatMap { it.returning.map { it.fighter } })

        for (fighter in fighters) {
            fighter.extraAlphaMult = 1f - (1f - alpha) * levelForAlpha
            fighter.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small

            fighter.engineController.fadeToOtherColor(this,
                SeraphimDriveShipsystem.ENGINE_COLOR,
                SeraphimDriveShipsystem.ENGINE_COLOR, 1f * effectLevel, 1f)
            fighter.engineController.extendFlame(this, -0.1f * effectLevel, -0.1f * effectLevel, 0f)
        }
    }

    override fun unapply(stats: MutableShipStatsAPI, id: String?) {
        stats.maxSpeed.unmodify(id)
        stats.maxTurnRate.unmodify(id)
        stats.turnAcceleration.unmodify(id)
        stats.acceleration.unmodify(id)
        stats.deceleration.unmodify(id)

        activated = false
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