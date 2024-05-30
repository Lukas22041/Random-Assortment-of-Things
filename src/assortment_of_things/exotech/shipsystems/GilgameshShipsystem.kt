package assortment_of_things.exotech.shipsystems

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import com.fs.starfarer.api.loading.WeaponGroupType
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*
import kotlin.collections.ArrayList

class GilgameshShipsystem : BaseShipSystemScript(), CombatLayeredRenderingPlugin {

    var activated = false
    var ship: ShipAPI? = null
    val color = ExoUtils.color2

    var drones = ArrayList<ShipAPI>()
    var target: ShipAPI? = null

    var addedRenderer = false

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)
        ship = stats!!.entity as ShipAPI? ?: return

        if (!addedRenderer) {
            addedRenderer = true
            Global.getCombatEngine().addLayeredRenderingPlugin(this)
        }


        var system = ship!!.system

        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false
            target = null
        }

        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            target = ship!!.shipTarget

            var left = ship!!.allWeapons.find { it.slot.id == "WS0004" }
            var right = ship!!.allWeapons.find { it.slot.id == "WS0005" }

            if (left != null) {
                var drone = spawnDrone(left.spec.weaponId)
                drones.add(drone)
            }
            if (right != null) {
                var drone = spawnDrone(right.spec.weaponId)
                drones.add(drone)
            }
        }




        controlWingPosition(effectLevel)
    }

    override fun advance(amount: Float) {
        if (target != null) {
            for (drone in drones) {

                var weapon = drone.allWeapons.first()
                weapon.ensureClonedSpec()

                var spec =  weapon.spec
                var projectileSpec = spec.projectileSpec
                if (projectileSpec is ProjectileSpecAPI) {
                    if (projectileSpec.collisionClass == CollisionClass.PROJECTILE_FF) {
                        projectileSpec.collisionClass = CollisionClass.PROJECTILE_NO_FF
                    }

                    if (projectileSpec.collisionClass == CollisionClass.RAY) {
                        projectileSpec.collisionClass = CollisionClass.RAY_FIGHTER
                    }
                }

                if (spec is BeamWeaponSpecAPI) {
                    if (spec.collisionClass == CollisionClass.RAY) {
                        spec.collisionClass = CollisionClass.RAY_FIGHTER
                    }
                }


                val angle = Misc.getAngleInDegrees(drone.location, target!!.location)

                var isInArc = Misc.isInArc(drone.facing, weapon.arc, angle)
                var isInRange = MathUtils.getDistance(weapon.location, target!!.location) <= weapon.range

                if (isInArc && isInRange) {
                    drone.giveCommand(ShipCommand.FIRE, target!!.location, 0)
                }

                turnTowardsPointV2(drone, target!!.location, 0f)
            }
        }
    }


    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

    }


    fun turnTowardsPointV2(drone: ShipAPI, point: Vector2f?, angVel: Float): Boolean {
        val desiredFacing = Misc.getAngleInDegrees(drone.location, point)
        return turnTowardsFacingV2(drone, desiredFacing, angVel)
    }

    fun turnTowardsFacingV2(drone: ShipAPI, desiredFacing: Float, relativeAngVel: Float): Boolean {
        val turnVel = drone.angularVelocity - relativeAngVel
        val absTurnVel = Math.abs(turnVel)
        val turnDecel = drone.engineController.turnDeceleration
        // v t - 0.5 a t t = dist
        // dv = a t;  t = v / a
        val decelTime = absTurnVel / turnDecel
        val decelDistance = absTurnVel * decelTime - 0.5f * turnDecel * decelTime * decelTime
        val facingAfterNaturalDecel = drone.facing + Math.signum(turnVel) * decelDistance
        val diffWithEventualFacing = Misc.getAngleDiff(facingAfterNaturalDecel, desiredFacing)
        val diffWithCurrFacing = Misc.getAngleDiff(drone.facing, desiredFacing)
        if (diffWithEventualFacing > 1f) {
            var turnDir = Misc.getClosestTurnDirection(drone.facing, desiredFacing)
            if (Math.signum(turnVel) == Math.signum(turnDir)) {
                if (decelDistance > diffWithCurrFacing) {
                    turnDir = -turnDir
                }
            }
            if (turnDir < 0) {
                drone.giveCommand(ShipCommand.TURN_RIGHT, null, 0)
            } else if (turnDir >= 0) {
                drone.giveCommand(ShipCommand.TURN_LEFT, null, 0)
            } else {
                return false
            }
        }
        return false
    }

    fun spawnDrone(weaponID: String) : ShipAPI {
        val spec = Global.getSettings().getHullSpec("rat_gilgamesh_drone")
        val v = Global.getSettings().createEmptyVariant("rat_gilgamesh_drone", spec)

        v.addWeapon("WS 000", weaponID)
        var g = WeaponGroupSpec(WeaponGroupType.LINKED)
        g.addSlot("WS 000")
        v.addWeaponGroup(g)

        var drone = Global.getCombatEngine().createFXDrone(v)


        drone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER)
        drone.setOwner(ship!!.originalOwner)

        drone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f) // so it's non-targetable

        drone.setDrone(true)
        drone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, ship)
        drone.getMutableStats().getEnergyWeaponDamageMult().applyMods(ship!!.mutableStats.missileWeaponDamageMult)
        drone.getMutableStats().getMissileWeaponDamageMult().applyMods(ship!!.mutableStats.missileWeaponDamageMult)
        drone.getMutableStats().getBallisticWeaponDamageMult().applyMods(ship!!.mutableStats.missileWeaponDamageMult)

        drone.getMutableStats().damageToFrigates.applyMods(ship!!.mutableStats.damageToFrigates)
        drone.getMutableStats().damageToDestroyers.applyMods(ship!!.mutableStats.damageToDestroyers)
        drone.getMutableStats().damageToCruisers.applyMods(ship!!.mutableStats.damageToCruisers)
        drone.getMutableStats().damageToCapital.applyMods(ship!!.mutableStats.damageToCapital)

        //Damage reduction
        drone.getMutableStats().getEnergyWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.333f)
        drone.getMutableStats().getMissileWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.333f)
        drone.getMutableStats().getBallisticWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.333f)

        drone.setCollisionClass(CollisionClass.NONE)
        drone.giveCommand(ShipCommand.SELECT_GROUP, null, 0)
        drone.facing = ship!!.facing

        drone.location.set(ship!!.location)

        Global.getCombatEngine().addEntity(drone)

        return drone
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }

    fun controlWingPosition(effectLevel: Float) {
        var effectLevel = easeInOutSine(effectLevel)

        var angle = -25f
        var lWing = ship!!.allWeapons.find { it.spec.weaponId == "rat_gilgamesh_wing_left" } ?: return
        var rWing = ship!!.allWeapons.find { it.spec.weaponId == "rat_gilgamesh_wing_right" } ?: return

        angle += 40f * effectLevel

        lWing.currAngle = ship!!.facing - angle
        rWing.currAngle = ship!!.facing + angle
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        return ship!!.shipTarget != null
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String? {

        if (ship!!.shipTarget == null) return "No Target selected"

        return super.getInfoText(system, ship)
    }



    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun getRenderRadius(): Float {
        return 1000000f
    }


    override fun init(entity: CombatEntityAPI?) {

    }

    override fun cleanup() {

    }

    override fun isExpired(): Boolean {
        return false
    }


}