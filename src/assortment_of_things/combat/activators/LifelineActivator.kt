package assortment_of_things.combat.activators

import activators.CombatActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color


class LifelineActivator(ship: ShipAPI) : CombatActivator(ship), DamageTakenModifier {

    var linkedShip: ShipAPI? = null
    var color = Color(0, 255, 100)
    var range = 1200f
    var shipsInRange = false

    override fun initialized() {
        super.initialized()
        ship.addListener(this)
    }

    override fun getBaseActiveDuration(): Float {
        return 10f
    }

    override fun getBaseCooldownDuration(): Float {
        return 20f
    }

    override fun canActivate(): Boolean {
        var ships = CombatUtils.getShipsWithinRange(ship.location, range).filter { it != ship && it.owner == ship.owner }
        return shipsInRange
    }


    override fun shouldActivateAI(p0: Float): Boolean {
        var activateFlags = listOf(AIFlags.MANEUVER_TARGET, AIFlags.PURSUING)
        var flags = ship.aiFlags

        if (ship.currFlux < ship.maxFlux * 0.1f) return  false
        if (!shipsInRange) return false

        for (flag in activateFlags)
        {
            if (flags.hasFlag(flag))
            {
                return true
            }
        }

        return false
    }

    override fun isReady(): Boolean {
        return shipsInRange
    }

    override fun getDisplayText(): String {
        if (shipsInRange)
        {
            return "Lifeline"
        }
        else
        {
            return "Lifeline (No nearby ships)"
        }
    }

    override fun isToggle(): Boolean {
        return false
    }

    fun findClosestAlliedShip(ship: ShipAPI, locFromForSorting: Vector2f?, smallestToNote: HullSize, maxRange: Float, considerShipRadius: Boolean): ShipAPI? {
        val engine = Global.getCombatEngine()
        val ships = engine.ships
        var minDist = Float.MAX_VALUE
        var closest: ShipAPI? = null
        for (other in ships) {
            if (other.hullSize.ordinal < smallestToNote.ordinal) continue
            if (other.isShuttlePod) continue
            if (other.isHulk) continue
            if (ship == other) continue
            if (ship.owner != other.owner) continue
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

    override fun onActivate() {
        var player = ship == Global.getCombatEngine().getPlayerShip();

        if (player)
        {
             linkedShip = findClosestAlliedShip(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, range, true);
        }
        else
        {
            var ships = CombatUtils.getShipsWithinRange(ship.location, range).filter { it != ship && it.owner == ship.owner }
            if (ships.isNotEmpty())
            {
                linkedShip = ships.random()
            }
        }

        if (linkedShip == null)
        {
            setActiveDuration(0f, true)
            setCooldownDuration(0f, true)
        }
    }

    override fun onFinished() {
        linkedShip = null
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        var ships = CombatUtils.getShipsWithinRange(ship.location, range).filter { it != ship && it.owner == ship.owner }
        shipsInRange = ships.isNotEmpty()

        if (linkedShip != null)
        {

            ship!!.setJitterUnder(this, color, 1f, 10, 4f, 6f);
            linkedShip!!.setJitterUnder(this, color, 1f, 10, 4f, 6f);
        }
    }

    override fun getBaseInDuration(): Float {
        return 0.1f
    }

    override fun getHUDColor(): Color {
        return color
    }

    override fun modifyDamageTaken(param: Any?, target: CombatEntityAPI?, damage: DamageAPI?, point: Vector2f?, shieldHit: Boolean): String? {
        if (linkedShip == null) return null

        if ( shieldHit && damage != null )
        {
            var engine = Global.getCombatEngine()
            var fluxDamage = damage.computeFluxDealt(damage.damage * 0.5f)

            damage.modifier.modifyMult("lifeline_mod", 0.5f)
            linkedShip!!.fluxTracker.hardFlux = linkedShip!!.fluxTracker.hardFlux + fluxDamage

            //engine.applyDamage(linkedShip, point, damage.damage * 0.5f, damage.type, 0f, false, damage.isSoftFlux, ship)
            return "lifeline_mod"
        }
        else
        {
            return null
        }
    }

}