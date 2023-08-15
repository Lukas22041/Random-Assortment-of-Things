package assortment_of_things.abyss.shipsystem.charybdis

import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class CharybdisHeadSystemOld : BaseShipSystemScript() {

    protected var STATUSKEY1 = Any()
    protected var STATUSKEY2 = Any()
    protected var STATUSKEY3 = Any()
    protected var STATUSKEY4 = Any()
    protected fun isDisruptable(cloak: ShipSystemAPI): Boolean {
        return cloak.specAPI.hasTag(Tags.DISRUPTABLE)
    }

    var afterimageInterval = IntervalUtil(0.2f, 0.2f)

    companion object {
        var JITTER_COLOR = Color(255, 175, 255, 255)
        var JITTER_FADE_TIME = 0.5f
        var SHIP_ALPHA_MULT = 0.25f

        //public static float VULNERABLE_FRACTION = 0.875f;
        var VULNERABLE_FRACTION = 0f
        var INCOMING_DAMAGE_MULT = 0.25f
        var MAX_TIME_MULT = 3f

    }

   // var ship: ShipAPI? = null


    fun maintainStatus(playerShip: ShipAPI?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        val f = VULNERABLE_FRACTION
        var cloak = playerShip!!.phaseCloak
        if (cloak == null) cloak = playerShip.system
        if (cloak == null) return
        if (effectLevel > f) {
            Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
                cloak.specAPI.iconSpriteName,
                cloak.displayName,
                "time flow altered",
                false)
        } else {
        }
    }

    override fun apply(stats: MutableShipStatsAPI?,  id: String?,state: ShipSystemStatsScript.State?, effectLevel: Float) {
        var ship = stats!!.entity as ShipAPI
        if (ship !is ShipAPI) return


       /* stats.maxSpeed.modifyMult(id, 1 + (2f * effectLevel))

        stats.deceleration.modifyMult(id, 1 - (0.5f * effectLevel))
        stats.maxTurnRate.modifyMult(id, 1 - (0.5f * effectLevel))
        stats.turnAcceleration.modifyMult(id, 1 - (0.5f * effectLevel))*/



        if (ship.system.isActive) {
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)


        }

        applyPhase(ship!!, stats, id!!, state!!, effectLevel)

        var children = ship!!.childModulesCopy

        for (child in children) {
            applyPhase(child!!, child.mutableStats, id!!, state!!, effectLevel)
        }
    }



    fun applyPhase(ship: ShipAPI, stats: MutableShipStatsAPI, id: String, state: ShipSystemStatsScript.State, effectLevel: Float) {
        var id = id
        var player = false
        if (stats.entity is ShipAPI) {
            player = ship === Global.getCombatEngine().playerShip
            id = id + "_" + ship!!.id
        } else {
            return
        }
        if (player) {
            maintainStatus(ship, state, effectLevel)
        }
        if (Global.getCombatEngine().isPaused) {
            return
        }

        if (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE) {
            unapply(stats, id)
            return
        }

        val jitterLevel = 0f
        val jitterRangeBonus = 0f
        var levelForAlpha = effectLevel
        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            ship.isPhased = true

            levelForAlpha = effectLevel


        } else if (state == ShipSystemStatsScript.State.OUT) {
            ship.isPhased = effectLevel > 0.5f
            levelForAlpha = effectLevel
        }
        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.ACTIVE)
        {
            ship.isHoldFireOneFrame = true
            ship.blockCommandForOneFrame(ShipCommand.FIRE)
        }
        ship.extraAlphaMult = 1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha
        ship.setApplyExtraAlphaToEngines(true)
        val extra = 0f
        val shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * levelForAlpha * (1f - extra)
        stats.timeMult.modifyMult(id, shipTimeMult)
        stats.fluxDissipation.modifyMult(id, 0.5f)
        stats.hardFluxDissipationFraction.modifyMult(id, 0f)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.ACTIVE) {
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                var afterImageColor =  Color(150, 0 ,255)
                AfterImageRenderer.addAfterimage(ship!!, afterImageColor.setAlpha(30), afterImageColor.setAlpha(15), 2f, 2f, Vector2f().plus(ship!!.location))
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI, id: String) {
        var ship: ShipAPI? = null
        ship = if (stats.entity is ShipAPI) {
            stats.entity as ShipAPI
        } else {
            return
        }
        Global.getCombatEngine().timeMult.unmodify(id)
        stats.timeMult.unmodify(id)
        stats.fluxDissipation.unmodify(id)
        stats.hardFluxDissipationFraction.unmodify(id)

        ship!!.isPhased = false
        ship.extraAlphaMult = 1f

    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State, effectLevel: Float): StatusData? {
        return null
    }


}