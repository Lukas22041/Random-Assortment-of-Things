package assortment_of_things.abyss.activators

import activators.ActivatorManager
import activators.CombatActivator
import assortment_of_things.combat.PidController
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import java.awt.Color
import kotlin.math.atan

class EmergencySupportActivator(ship: ShipAPI?) : CombatActivator(ship) {

    var color = Color(0, 255, 150, 200)

    var activeWings: MutableMap<ShipAPI, PidController> = HashMap()

    var currentRotation = MathUtils.getRandomNumberInRange(30f, 90f)

    override fun getBaseActiveDuration(): Float {
       return  20f
    }

    override fun getBaseCooldownDuration(): Float {
        return  20f
    }

    override fun shouldActivateAI(p0: Float): Boolean {
        var activateFlags = listOf(AIFlags.PURSUING, AIFlags.MOVEMENT_DEST, AIFlags.RUN_QUICKLY)
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

    override fun getBaseInDuration(): Float {
        return 0f
    }

    override fun getOutDuration(): Float {
        return 1f
    }

    override fun onActivate() {
        super.onActivate()


        var num = when(ship.hullSize)
        {
            ShipAPI.HullSize.FRIGATE -> 3
            ShipAPI.HullSize.DESTROYER -> 4
            ShipAPI.HullSize.CRUISER -> 6
            ShipAPI.HullSize.CAPITAL_SHIP -> 8
            else -> 3
        }
        Global.getCombatEngine().getFleetManager(ship.owner).isSuppressDeploymentMessages = true
        for (i in 0 until num)
        {
            var fighter = CombatUtils.spawnShipOrWingDirectly("rat_shield_drone_wing", FleetMemberType.FIGHTER_WING, FleetSide.PLAYER, 0.7f, ship.location, ship.facing)

            var controller = PidController(2f, 2f, 6f, 0.5f)
            activeWings.put(fighter, controller)
            fighter.weaponGroupsCopy.forEach {group ->
                if (!group.isAutofiring)
                {
                    fighter.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, 0);
                }
            }
            fighter.shipAI = null
            fighter.giveCommand(ShipCommand.SELECT_GROUP, null, 99);
        }
        Global.getCombatEngine().getFleetManager(ship.owner).isSuppressDeploymentMessages = false
    }

    override fun advance(amount: Float) {

        activeWings = activeWings.filter { it.key.isAlive && !it.key.isHulk }.toMutableMap()

        if (activeWings.isEmpty()) return

        var angleIncrease = 360 / activeWings.size
        var angle = 0f

        currentRotation += 0.2f
        angle += currentRotation

        for ((drone, controller) in activeWings)
        {

            drone.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
            drone.blockCommandForOneFrame(ShipCommand.VENT_FLUX)

            if (state == State.ACTIVE)
            {
                if (!drone.shield.isOn) {
                    drone.shield.toggleOn()
                }
            }

            if (state == State.OUT)
            {
                if (drone.shield.isOn) {
                    drone.shield.toggleOff()
                }
            }

            if (drone.shield != null)
            {
                drone.shield.ringColor = Color(0, 255, 150, 200)
                drone.shield.innerColor = Color(0, 150, 0, 150)
            }

            var iter = Global.getCombatEngine().shipGrid.getCheckIterator(drone.location, 1000f, 1000f)

            var target: ShipAPI? = null
            var distance = 100000f
            for (it in iter)
            {
                if (it is ShipAPI)
                {
                    if (it.isFighter) continue
                    if (Global.getCombatEngine().getFleetManager(it.owner).owner == Global.getCombatEngine().getFleetManager(drone.owner).owner) continue
                    if (it.isHulk) continue
                    var distanceBetween = MathUtils.getDistance(it, ship)
                    if (distance > distanceBetween)
                    {
                        distance = distanceBetween
                        target = it
                    }
                }
            }

            var shipLoc = ship.location

            var point = MathUtils.getPointOnCircumference(shipLoc, ship.collisionRadius * 1.5f, angle)
            controller.move(point, drone)

            if (target != null)
            {
                var targetLoc = target
                var facing = atan((drone.location.y - target.location.y) / (drone.location.x - target.location.x))
                controller.rotate(Misc.getAngleInDegrees(drone.location, target.location)  , drone)

            }
            else
            {
                controller.rotate(ship.facing + MathUtils.getRandomNumberInRange(-10f, 10f), drone)
            }

            angle += angleIncrease

        }
    }

    override fun onFinished() {
        super.onFinished()

        activeWings = activeWings.filter { it.key.isAlive && !it.key.isHulk }.toMutableMap()

        if (activeWings.isEmpty()) return
        for (wing in activeWings)
        {
            Global.getCombatEngine().spawnExplosion(wing.key.location, wing.key.velocity, Color(0, 255, 150, 200), 50f, 1f)
            Global.getSoundPlayer().playSound("explosion_ship", 1f, 0.3f, wing.key.location, wing.key.velocity)
            wing.key.hitpoints = 0f
        }

        currentRotation = MathUtils.getRandomNumberInRange(30f, 90f)
    }

    override fun getDisplayText(): String {
        return "Emergency Support"
    }

    override fun getHUDColor(): Color {
        return color
    }
}