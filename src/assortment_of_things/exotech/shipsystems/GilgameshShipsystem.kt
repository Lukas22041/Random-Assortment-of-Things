package assortment_of_things.exotech.shipsystems

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import com.fs.starfarer.api.loading.WeaponGroupType
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class GilgameshShipsystem : BaseShipSystemScript(), CombatLayeredRenderingPlugin {

    var activated = false
    var ship: ShipAPI? = null
    val color = ExoUtils.color2

    var drones = ArrayList<ShipAPI>()
    var target: ShipAPI? = null

    var addedRenderer = false

    var gate = Global.getSettings().getAndLoadSprite("graphics/ships/exo/rat_gilgamesh_gate.png")

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

            for (drone in ArrayList(drones)) {
                Global.getCombatEngine().removeEntity(drone)

                drones.remove(drone)

            }

        }

        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            target = ship!!.shipTarget

            var slotId = "WS0004"

            for (i in 0 until 6) {
                var slot = ship!!.allWeapons.find { it.slot.id == slotId }
                if (slot != null) {
                    var drone = spawnDrone(slot.spec.weaponId)
                    drones.add(drone)
                }

                if (slotId == "WS0004") {
                    slotId = "WS0005"
                }
                else {
                    slotId = "WS0004"
                }
            }

           /* var left = ship!!.allWeapons.find { it.slot.id == "WS0004" }
            var right = ship!!.allWeapons.find { it.slot.id == "WS0005" }

            if (left != null) {
                var drone = spawnDrone(left.spec.weaponId)
                drones.add(drone)
            }
            if (right != null) {
                var drone = spawnDrone(right.spec.weaponId)
                drones.add(drone)
            }*/
        }




        controlWingPosition(effectLevel)
    }

    override fun advance(amount: Float) {

        var positions = ArrayList<Vector2f>()
        var posIndex = 0

        var facing = ship!!.facing - 180f
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 150f, facing - 30))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 150f, facing + 30))

        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 160f, facing - 60))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 160f, facing + 60))

        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 170f, facing - 90))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 170f, facing + 90))

        if (target != null) {
            for (drone in drones) {

                var pos = positions.getOrNull(posIndex) ?: break
                posIndex += 1

                drone.location.set(pos)

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

               // var isInArc = Misc.isInArc(weapon.currAngle, weapon.arc + 10, angle)
                var isInRange = MathUtils.getDistance(weapon.location, target!!.location) <= weapon.range

                if (/*isInArc && */isInRange && ship!!.system.effectLevel >= 0.7f && ship!!.system.state != ShipSystemAPI.SystemState.OUT) {
                    drone.giveCommand(ShipCommand.FIRE, target!!.location, 0)
                }

                if (ship!!.system.state == ShipSystemAPI.SystemState.OUT && ship!!.system.effectLevel <= 0.8f) {
                    for (weapon in drone.allWeapons) {
                        weapon.stopFiring()
                    }
                }

                turnTowardsPointV2(drone, target!!.location, 0f)
            }
        }
    }


    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        var system = ship!!.system
        var effectLevel = system.effectLevel
        for (drone in drones) {

            var jitterLoc = drone.customData.get("rat_gilgamesh_jitter") as ArrayList<Vector2f>?
            if (jitterLoc == null) {
                jitterLoc = ArrayList<Vector2f>()
                drone.setCustomData("rat_gilgamesh_jitter", jitterLoc)
            }

            var weapon = drone.allWeapons.first()

            /*weapon.sprite?.color = Color(237, 120, 74)*/
            doJitter(weapon.location, weapon.sprite, effectLevel * 0.25f, jitterLoc, 5, 6f)
            weapon.sprite?.setNormalBlend()


            drone.alphaMult = 0.25f * effectLevel

            gate.angle = drone.facing - 90
            gate.alphaMult = effectLevel * 0.5f
            gate.setNormalBlend()
            gate.renderAtCenter(drone.location.x, drone.location.y)

            doJitter(Vector2f(drone.location.x, drone.location.y), gate, effectLevel * 0.33f, jitterLoc, 5, 12f)


        }

    }

    fun doJitter(loc: Vector2f, sprite: SpriteAPI, level: Float, lastLocations: java.util.ArrayList<Vector2f>, jitterCount: Int, jitterMaxRange: Float) {

        var paused = Global.getCombatEngine().isPaused
        var jitterAlpha = 0.2f


        if (!paused) {
            lastLocations.clear()
        }

        for (i in 0 until jitterCount) {

            var jitterLoc = Vector2f()

            if (!paused) {
                var x = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)
                var y = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)

                jitterLoc = Vector2f(x, y)
                lastLocations.add(jitterLoc)
            }
            else {
                jitterLoc = lastLocations.getOrElse(i) {
                    Vector2f()
                }
            }

            sprite.setAdditiveBlend()
            sprite.alphaMult = level * jitterAlpha
            sprite.renderAtCenter(loc.x + jitterLoc.x, loc.y + jitterLoc.y)
        }
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


        drone.setLayer(CombatEngineLayers.ABOVE_SHIPS_LAYER)
        drone.setOwner(ship!!.originalOwner)

        drone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f) // so it's non-targetable

        drone.setDrone(true)
        drone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, ship)
        drone.getMutableStats().getEnergyWeaponDamageMult().applyMods(ship!!.mutableStats.missileWeaponDamageMult)
        drone.getMutableStats().getMissileWeaponDamageMult().applyMods(ship!!.mutableStats.missileWeaponDamageMult)
        drone.getMutableStats().getBallisticWeaponDamageMult().applyMods(ship!!.mutableStats.missileWeaponDamageMult)

        drone.getMutableStats().ballisticWeaponRangeBonus.applyMods(ship!!.mutableStats.ballisticWeaponRangeBonus)
        drone.getMutableStats().energyWeaponRangeBonus.applyMods(ship!!.mutableStats.energyWeaponRangeBonus)
        drone.getMutableStats().missileWeaponRangeBonus.applyMods(ship!!.mutableStats.missileWeaponRangeBonus)

        drone.getMutableStats().ballisticRoFMult.applyMods(ship!!.mutableStats.ballisticRoFMult)
        drone.getMutableStats().energyRoFMult.applyMods(ship!!.mutableStats.energyRoFMult)
        drone.getMutableStats().missileRoFMult.applyMods(ship!!.mutableStats.missileRoFMult)

        drone.mutableStats.timeMult.applyMods(ship!!.mutableStats.timeMult)

        drone.mutableStats.ballisticAmmoBonus.applyMods(ship!!.mutableStats.ballisticAmmoBonus)
        drone.mutableStats.energyAmmoBonus.applyMods(ship!!.mutableStats.energyAmmoBonus)
        drone.mutableStats.missileAmmoBonus.applyMods(ship!!.mutableStats.missileAmmoBonus)

        drone.mutableStats.ballisticAmmoRegenMult.applyMods(ship!!.mutableStats.ballisticAmmoRegenMult)
        drone.mutableStats.energyAmmoRegenMult.applyMods(ship!!.mutableStats.energyAmmoRegenMult)
        drone.mutableStats.missileAmmoRegenMult.applyMods(ship!!.mutableStats.missileAmmoRegenMult)

        drone.getMutableStats().damageToFrigates.applyMods(ship!!.mutableStats.damageToFrigates)
        drone.getMutableStats().damageToDestroyers.applyMods(ship!!.mutableStats.damageToDestroyers)
        drone.getMutableStats().damageToCruisers.applyMods(ship!!.mutableStats.damageToCruisers)
        drone.getMutableStats().damageToCapital.applyMods(ship!!.mutableStats.damageToCapital)

        //Damage reduction
        drone.getMutableStats().getEnergyWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.50f)
        drone.getMutableStats().getMissileWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.50f)
        drone.getMutableStats().getBallisticWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.50f)


        drone.getMutableStats().ballisticWeaponRangeBonus.modifyFlat("rat_gilgamesh_drone", 200f)
        drone.getMutableStats().energyWeaponRangeBonus.modifyFlat("rat_gilgamesh_drone", 200f)
        drone.getMutableStats().missileWeaponRangeBonus.modifyFlat("rat_gilgamesh_drone", 200f)


        drone.setCollisionClass(CollisionClass.NONE)
        drone.giveCommand(ShipCommand.SELECT_GROUP, null, 0)
        drone.facing = ship!!.facing

        drone.alphaMult = 0f

        drone.location.set(ship!!.location)

        Global.getCombatEngine().addEntity(drone)

        return drone
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }

    fun controlWingPosition(effectLevel: Float) {
        var effectLevel = effectLevel * 2f
        effectLevel = MathUtils.clamp(effectLevel, 0f, 1f)
        effectLevel = easeInOutSine(effectLevel)

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
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER)
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