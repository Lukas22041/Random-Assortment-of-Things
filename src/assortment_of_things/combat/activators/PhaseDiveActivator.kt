package assortment_of_things.combat.activators

import activators.CombatActivator
import assortment_of_things.campaign.procgen.customThemes.ChiralThemeGenerator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreAdminPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color


class PhaseDiveActivator(ship: ShipAPI?) : CombatActivator(ship) {

    var color = Color(155, 0, 255, 255)

    override fun getBaseActiveDuration(): Float {
        return 1f
    }

    override fun getBaseCooldownDuration(): Float {
        return 15f
    }

    override fun getInDuration(): Float {
        return 0.5f
    }

    override fun getBaseOutDuration(): Float {
        return 0.5f
    }

    override fun shouldActivateAI(p0: Float): Boolean {
        var activateFlags = listOf(AIFlags.PURSUING, AIFlags.BACK_OFF, AIFlags.MANEUVER_TARGET)
        var flags = ship.aiFlags

        for (flag in activateFlags)
        {
            if (flags.hasFlag(flag))
            {
                return true
            }
        }
        
        return false
    }


    var alphaMult = 1f
    var crPenalty = 0f

    override fun advance(amount: Float) {
        super.advance(amount)

        var id = "rat_phase_hop"
        var player = ship == Global.getCombatEngine().playerShip

        if (state == State.IN)
        {
            alphaMult = 1 - effectLevel
            alphaMult = MathUtils.clamp(alphaMult, 0.1f, 1f)

            ship.alphaMult = alphaMult
            ship.isPhased = true

            ship.setJitter(id, color, effectLevel * 0.5f, 10, 2f, 2f)
        }
        else if (state == State.ACTIVE)
        {
            ship.isPhased = true

            ship.setJitter(id, color, effectLevel * 0.5f, 10, 2f, 2f)
            crPenalty += 3 * Global.getCombatEngine().elapsedInLastFrame * ship.mutableStats.timeMult.mult

            if (isKeyDown)
            {
                setActiveDuration(0f, true)
            }
        }
        else if (state == State.OUT )
        {
            ship.isPhased = true
            alphaMult = 1 - effectLevel

            alphaMult = MathUtils.clamp(alphaMult, 0.1f, 1f)
            ship.alphaMult = alphaMult

            ship.setJitter(id, color, effectLevel * 0.5f, 10, 2f, 2f)
            var closestShip = findClosestShip(ship, ship.location, ShipAPI.HullSize.FRIGATE, 100f + ship.collisionRadius, true);
            if (closestShip != null && MathUtils.getDistance(ship.location, closestShip.location) <= ship.collisionRadius + closestShip.collisionRadius)
            {
                setOutDuration(0.2f, false)
            }

        }
        else if (state == State.COOLDOWN)
        {

            setActiveDuration(1f, true)

            ship.isPhased = false
            alphaMult = 1f

            ship.alphaMult = alphaMult
        }
        else
        {

        }

        val shipTimeMult = 1f + (4f * effectLevel)
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1 / shipTimeMult)
        }
        else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        ship.mutableStats.peakCRDuration.modifyFlat(id, -crPenalty)


    }

    override fun getDisplayText(): String {
        return "Phase Dive"
    }

    override fun getHUDColor(): Color {
        return color
    }

    fun findClosestShip(ship: ShipAPI, locFromForSorting: Vector2f?, smallestToNote: ShipAPI.HullSize, maxRange: Float, considerShipRadius: Boolean): ShipAPI? {
        val engine = Global.getCombatEngine()
        val ships = engine.ships
        var minDist = Float.MAX_VALUE
        var closest: ShipAPI? = null
        for (other in ships) {
            if (other.hullSize.ordinal < smallestToNote.ordinal) continue
            if (other.isShuttlePod) continue
            if (other.isHulk) continue
            if (ship == other) continue
            val dist = Misc.getDistance(ship.location, other.location)
            val distSort = Misc.getDistance(locFromForSorting, other.location)
            var radSum = ship.collisionRadius + other.collisionRadius
            if (!considerShipRadius) radSum = 0f
            if (dist > maxRange + radSum) continue
            if (distSort < minDist) {
                closest = other
                minDist = distSort
            }
        }
        return closest
    }
}