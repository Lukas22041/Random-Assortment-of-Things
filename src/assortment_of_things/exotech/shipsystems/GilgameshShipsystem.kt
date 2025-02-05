package assortment_of_things.exotech.shipsystems

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.hullmods.PhaseriftShield
import assortment_of_things.misc.baseOrModSpec
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
import com.fs.starfarer.api.util.IntervalUtil
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

    var gate = Global.getSettings().getAndLoadSprite("graphics/ships/exo/rat_gilgamesh_gate.png")
    var nebulaInterval = IntervalUtil(0.05f, 0.1f)

    var delaysList = listOf<Float>(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.25f, 2.5f, 2.75f, 3f).shuffled()
    var delaysIndex = 0

    var noLongerTeleportDrones = false

    var addedListener = false


    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)
        ship = stats!!.entity as ShipAPI? ?: return

        if (!ship!!.hasListenerOfClass(CollissionSpecConverter::class.java)) {
            ship!!.addListener(CollissionSpecConverter())
        }

        if (!addedListener) {
            addedListener = true
            /*ship!!.addListener(GilgameshDamageConverter())
            ship!!.addListener(GilgameshDamageModifier())*/
        }

        if (ship!!.fleetMember?.fleetData?.fleet?.isPlayerFleet == false) {
            Global.getCombatEngine().getCustomData().set("phaseAnchor_canDive", false)
        }

        if (!addedRenderer) {
            addedRenderer = true
            Global.getCombatEngine().addLayeredRenderingPlugin(this)
        }


        var system = ship!!.system

        if (system.state == ShipSystemAPI.SystemState.ACTIVE && ship!!.phaseCloak.isActive) {
            ship!!.system.forceState(ShipSystemAPI.SystemState.OUT, 0f)
            controlWingPosition(effectLevel)
            noLongerTeleportDrones = true
            return
        }

        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false
            target = null
            delaysList = delaysList.shuffled()

            for (drone in ArrayList(drones)) {
                Global.getCombatEngine().removeEntity(drone)

                drones.remove(drone)

            }

            noLongerTeleportDrones = false

        }

        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            var phaseriftShieldListener = ship!!.getListeners(PhaseriftShield.PhaseriftShieldListener::class.java).firstOrNull()
            if (phaseriftShieldListener != null) {
                phaseriftShieldListener.shieldHP += phaseriftShieldListener.maxShieldHP * phaseriftShieldListener.regenPerSystemUse
                phaseriftShieldListener.shieldHP = MathUtils.clamp(phaseriftShieldListener.shieldHP, 0f, phaseriftShieldListener.maxShieldHP)
            }


            target = findTarget()

            if (ship!!.shipAI != null) {
                ship!!.setCustomData("rat_dont_allow_phase", 7f)
            }

            var slotId = "WS0004"

            for (i in 0 until 12) {
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

            drones.shuffle()

        }




        controlWingPosition(effectLevel)
    }

    override fun advance(amount: Float) {

        //Dont let it phase immediately after system activation
        var disallowPhaseTimer = ship!!.customData.get("rat_dont_allow_phase") as Float?
        if (disallowPhaseTimer != null) {
            if (disallowPhaseTimer > 0f) {
                disallowPhaseTimer -= 1f * amount
                ship!!.setCustomData("rat_dont_allow_phase", disallowPhaseTimer)
            }
        }

        var positions = ArrayList<Vector2f>()
        var posIndex = 0

        var facing = ship!!.facing - 180f
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 150f, facing - 30))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 150f, facing + 30))

        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 160f, facing - 60))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 160f, facing + 60))

        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 240f, facing - 80))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 240f, facing + 80))

        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 220f, facing - 45))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 220f, facing + 45))

        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 240f, facing - 65))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 240f, facing + 65))

        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 200f, facing - 18))
        positions.add(MathUtils.getPointOnCircumference(ship!!.location, 200f, facing + 18))


        nebulaInterval.advance(amount)
        var intervalElapsed = nebulaInterval.intervalElapsed()
        if (target != null) {
            for (drone in drones) {

                drone.mutableStats.timeMult.applyMods(ship!!.mutableStats.timeMult) //Match Drones Timemult to ship, otherwise increases their time flow when you activate it when exiting phase

                updateDroneEntryLevel(drone)
                var droneLevel = getDroneLevel(drone)


                var pos = positions.getOrNull(posIndex) ?: break
                posIndex += 1

                if (!noLongerTeleportDrones) {
                    drone.location.set(pos)
                }


                var weapon = drone.allWeapons.first()
                weapon.ensureClonedSpec()

                var spec =  weapon.spec
                var projectileSpec = spec.projectileSpec
                var projSpeed = 600f
                if (projectileSpec is ProjectileSpecAPI) {
                    if (projectileSpec.collisionClass == CollisionClass.PROJECTILE_FF) {
                        projectileSpec.collisionClass = CollisionClass.PROJECTILE_NO_FF
                    }

                    if (projectileSpec.collisionClass == CollisionClass.RAY) {
                        projectileSpec.collisionClass = CollisionClass.RAY_FIGHTER
                    }

                    projSpeed = projectileSpec.getMoveSpeed(drone.mutableStats, weapon)
                }

                if (spec is BeamWeaponSpecAPI) {
                    if (spec.collisionClass == CollisionClass.RAY) {
                        spec.collisionClass = CollisionClass.RAY_FIGHTER
                    }

                    projSpeed = spec.beamSpeed
                }





                val angle = Misc.getAngleInDegrees(drone.location, target!!.location)

                var isInArc = Misc.isInArc(weapon.currAngle, weapon.arc, angle)
                var isInRange = MathUtils.getDistance(weapon.location, target!!.location) <= weapon.range

                if (/*isInArc && */isInRange && droneLevel >= 0.7f && ship!!.system.state != ShipSystemAPI.SystemState.OUT) {
                    drone.giveCommand(ShipCommand.FIRE, target!!.location, 0)
                }

                if (ship!!.system.state == ShipSystemAPI.SystemState.OUT && droneLevel <= 0.8f) {
                    for (weapon in drone.allWeapons) {
                        weapon.stopFiring()
                    }
                }

                drone.shipTarget = target

                val shipAngleToTarget = Misc.getAngleInDegrees(drone.location, target!!.location)
                var isEnemyInShipArc = Misc.isInArc(ship!!.facing, 40f, shipAngleToTarget)


                //This hopefully fixes some aiming issues, i think since the drones dont have velocity, its not considered in the innitial speed boost for weapons
              /*  var currentSpeed = Vector2f(drone.velocity)
                drone.velocity.set(Vector2f(ship!!.velocity))*/

                //drone.velocity.set(Vector2f(ship!!.velocity)) //May potentialy fix issues where weapons are misaimed, i think before it got messed up since the drones have no velocity, which ofc isnt true when firing.

                var predictedPoint = Global.getCombatEngine().getAimPointWithLeadForAutofire(drone, 1f, target, projSpeed)
                turnTowardsPointV2(drone, predictedPoint, 0f)
                //turnTowardsPointV2(drone, target!!.location, 0f)


               /* if (droneLevel >= 0.9 && intervalElapsed) {

                    Global.getCombatEngine().addNegativeNebulaParticle(drone.location, drone.velocity,
                        60f, 0.1f, 1f, 1f, 1f,
                       Color(20, 20, 20))

                    Global.getCombatEngine().addNebulaParticle(drone.location, drone.velocity,
                        60f, 0.1f, 1f, 1f, 1f,
                        Misc.interpolateColor(ExoUtils.color1, ExoUtils.color2, MathUtils.getRandomNumberInRange(0f, 1f)).setAlpha((45 * droneLevel).toInt()))
                }*/


            }
        }
    }

    fun updateDroneEntryLevel(drone: ShipAPI) : Float {
        var delay = drone.customData.get("rat_gilgamesh_drone_increase") as Float?
        var level = drone.customData.get("rat_gilgamesh_drone_level") as Float?
        if (delay == null || level == null) {

            var timing = delaysList.get(delaysIndex)

            delaysIndex += 1
            if (delaysIndex >= delaysList.size) {
                delaysIndex = 0
            }

            delay = timing + MathUtils.getRandomNumberInRange(0f, 0.1f)
            level = 0f
        }

        delay -= 1.5f * Global.getCombatEngine().elapsedInLastFrame

        if (delay <= 0 && (ship!!.system.state == ShipSystemAPI.SystemState.IN || ship!!.system.state == ShipSystemAPI.SystemState.ACTIVE)) {
            level += 0.6f /** ship!!.system.effectLevel*/ * Global.getCombatEngine().elapsedInLastFrame * ship!!.mutableStats.timeMult.modifiedValue
        }

        if (ship!!.system.state == ShipSystemAPI.SystemState.OUT) {
            //level -= increase * Global.getCombatEngine().elapsedInLastFrame * ship!!.mutableStats.timeMult.modifiedValue
            var oldLevel = level
            level = ship!!.system.effectLevel /** ship!!.system.effectLevel*/
            level = MathUtils.clamp(level, 0f, oldLevel)
        }

        level = MathUtils.clamp(level, 0f, 1f)

        drone.setCustomData("rat_gilgamesh_drone_increase", delay)
        drone.setCustomData("rat_gilgamesh_drone_level", level)


        return level
    }

    fun getDroneLevel(drone: ShipAPI) : Float {
        var level = drone.customData.get("rat_gilgamesh_drone_level") as Float?

        if (level == null) {
            level = updateDroneEntryLevel(drone)
        }

        level = easeInOutSine(level)
        return level
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        var system = ship!!.system
        var effectLevel = system.effectLevel
        for (drone in drones) {

            var droneLevel = getDroneLevel(drone)

            var jitterLoc = drone.customData.get("rat_gilgamesh_jitter") as ArrayList<Vector2f>?
            if (jitterLoc == null) {
                jitterLoc = ArrayList<Vector2f>()
                drone.setCustomData("rat_gilgamesh_jitter", jitterLoc)
            }

            var weapon = drone.allWeapons.first()

            /*weapon.sprite?.color = Color(237, 120, 74)*/
            doJitter(weapon.location, weapon.sprite, droneLevel * 0.25f, jitterLoc, 5, 6f)
            weapon.sprite?.setNormalBlend()


            drone.alphaMult = 0.25f * droneLevel

            var gateAlpha = effectLevel * 0.5f

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
        drone.getMutableStats().getEnergyWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.4f)
        drone.getMutableStats().getBallisticWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.4f)
        drone.getMutableStats().getMissileWeaponDamageMult().modifyMult("rat_gilgamesh_drone", 0.3f)

        drone.mutableStats.missileWeaponRangeBonus.modifyMult("rat_gilgamesh_drone", 0.75f)

        drone.getMutableStats().ballisticWeaponRangeBonus.modifyFlat("rat_gilgamesh_drone", 300f)
        drone.getMutableStats().energyWeaponRangeBonus.modifyFlat("rat_gilgamesh_drone", 300f)
        drone.getMutableStats().missileWeaponRangeBonus.modifyFlat("rat_gilgamesh_drone", 300f)


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

    fun findTarget() : ShipAPI? {
        if (ship == null) return null
        var target = ship!!.shipTarget

        var flags = ship!!.aiFlags
        if (flags.hasFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)) {
            target = flags.getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM) as ShipAPI?
        }

        return target
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        return findTarget() != null
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

class CollissionSpecConverter() : AdvanceableListener {
    override fun advance(amount: Float) {
        var engine = Global.getCombatEngine() ?: return

        for (missile in engine.missiles) {
            if (missile.weapon?.ship?.baseOrModSpec()?.baseHullId != "rat_gilgamesh_drone") continue
            if (missile.collisionClass == CollisionClass.MISSILE_FF) {
                missile.collisionClass = CollisionClass.MISSILE_NO_FF
            }
        }
    }

}