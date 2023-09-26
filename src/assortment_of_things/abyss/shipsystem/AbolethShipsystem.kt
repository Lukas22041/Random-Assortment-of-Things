package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.misc.baseOrModSpec
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
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import org.magiclib.plugins.MagicTrailPlugin
import java.awt.Color

class AbolethShipsystem : BaseShipSystemScript() {

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

    var ship: ShipAPI? = null


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

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) return true
        if (AbyssalsCoreHullmod.isChronosCore(ship!!)) return true
        return false
    }

    override fun apply(stats: MutableShipStatsAPI?,  id: String?,state: ShipSystemStatsScript.State?, effectLevel: Float) {
        ship = stats!!.entity as ShipAPI

        if (state == ShipSystemStatsScript.State.ACTIVE)
        {
            AbyssalsCoreHullmod.getRenderer(ship!!).enableBlink()
        }
        else
        {
            AbyssalsCoreHullmod.getRenderer(ship!!).disableBlink()
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            applyCosmos(stats, id!!, state!!, effectLevel)
        }
        else if ( AbyssalsCoreHullmod.isChronosCore(ship!!))
        {
            applyChronos(stats, id!!, state!!, effectLevel)
        }
    }

    override fun getDisplayNameOverride(state: ShipSystemStatsScript.State?, effectLevel: Float): String {
        if (ship == null) return "Inactive Shipsystem"
        if (AbyssalsCoreHullmod.isChronosCore(ship!!))
        {
            return "Temporal Grid"
        }
        else if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            return "Phase Dive"
        }
        return "Inactive Shipsystem"
    }


    fun applyChronos(stats: MutableShipStatsAPI, id: String, state: ShipSystemStatsScript.State, effectLevel: Float)
    {
        var ship = stats.entity as ShipAPI
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        var jitterColor = AbyssalsCoreHullmod.getColorForCore(ship).setAlpha(55)
        var jitterUnderColor = AbyssalsCoreHullmod.getColorForCore(ship).setAlpha(150)
        var jitterLevel = effectLevel
        var jitterRangeBonus = 0f
        val maxRangeBonus = 10f
        var mult = 3f

        ship!!.setJitter(this, jitterColor, jitterLevel, 3, 0f, 0 + jitterRangeBonus)
        ship!!.setJitterUnder(this, jitterUnderColor, jitterLevel, 25, 0f, 7f + jitterRangeBonus)

        val shipTimeMult = 1f + (mult - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        if (ship.system.isActive) {
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(75), Color(150, 0 ,255).setAlpha(75), 2f, 2f, Vector2f().plus(ship!!.location))
            }
        }

        ship!!.engineController.extendFlame(this, 0.25f * effectLevel, 0.25f * effectLevel, 0.25f * effectLevel)
    }

    var trailID = MagicTrailPlugin.getUniqueID()
    var interval = IntervalUtil(0.05f, 0.05f)
    fun applyCosmos(stats: MutableShipStatsAPI, id: String, state: ShipSystemStatsScript.State, effectLevel: Float) {
        var id = id
        var ship: ShipAPI? = null
        var player = false
        if (stats.entity is ShipAPI) {
            ship = stats.entity as ShipAPI
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
        var cloak = ship!!.phaseCloak
        if (cloak == null) cloak = ship.system
        if (cloak == null) return

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


            var points = ArrayList<Vector2f>()

            if (ship.baseOrModSpec().hullId == "rat_aboleth") {
                points.add(ship.location.plus(Vector2f(100f, 20f).rotate(ship.facing - 90)))
                points.add(ship.location.plus(Vector2f(-100f, 20f).rotate(ship.facing - 90)))
            }

            // points.add(ship.location.plus(Vector2f(85f, -30f).rotate(ship.facing - 90)))
            //points.add(ship.location.plus(Vector2f(-85f, -30f).rotate(ship.facing - 90)))

            var thrusterID = 1000
            interval.advance(Global.getCombatEngine().elapsedInLastFrame)
            for (point in points)
            {
                if (!ship.isAlive) continue
                if (ship.isHulk) continue
                thrusterID += 1000
                var color = AbyssalsCoreHullmod.getColorForCore(ship).setAlpha(50)

                if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
                {
                    MagicTrailPlugin.addTrailMemberSimple(ship, trailID + thrusterID, Global.getSettings().getSprite("fx", "base_trail_aura"),
                        Vector2f(point.x, point.y) , 50f, ship.facing - 180, 1f, 1f, color, 1f, 0.2f, 0.25f, 0.3f, true )

                }
            }
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

        if (ship.system.isActive) {
            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(15), Color(150, 0 ,255).setAlpha(15), 2f, 2f, Vector2f().plus(ship!!.location))
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

        AbyssalsCoreHullmod.getRenderer(ship).disableBlink()
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State, effectLevel: Float): StatusData? {
        return null
    }


}