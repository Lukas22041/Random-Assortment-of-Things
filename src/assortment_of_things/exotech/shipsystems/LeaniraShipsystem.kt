package assortment_of_things.exotech.shipsystems

import assortment_of_things.misc.ReflectionUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.MineStrikeStats
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.starfarer.combat.entities.Ship
import org.lwjgl.util.vector.Vector2f
import java.awt.Color


class LeaniraShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null
    val color = Color(248,172,44, 255)

    var variant: ShipVariantAPI? = null

    var platform: ShipAPI? = null

    var activated = false

    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {
        ship = stats.entity as ShipAPI? ?: return
        var system = ship!!.system

        if (platform != null) {
            var level = (platform!!.hitpoints - 0f) / (platform!!.maxHitpoints - 0f)
            level = 1 - level
            var mult = 1 + (2 * level)

            ship!!.system.cooldown = ship!!.system.specAPI.getCooldown(ship!!.mutableStats) * mult
        }


        for (module in ship!!.childModulesCopy) {
            if (!module.isAlive) continue
            variant = module.variant
            Global.getCombatEngine().removeEntity(module)
            platform = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, 1f, Vector2f(), ship!!.facing)
            platform!!.captain = ship!!.captain
            platform!!.setCustomData("rat_apheidas_parent", ship)
            Global.getCombatEngine().removeEntity(platform)
        }

        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false

            Global.getCombatEngine().removeEntity(platform)
        }

        if (!activated && system.isActive && ship!!.mouseTarget != null && variant != null) {
            spawnPlatform()
            activated = true
        }

        if (platform != null && state != ShipSystemStatsScript.State.ACTIVE) {
            platform!!.isHoldFireOneFrame = true
        }

        if (platform != null && platform!!.isAlive) {

            if (state == ShipSystemStatsScript.State.IN) {
                var level = 1 - (1 * effectLevel)
                platform!!.alphaMult = effectLevel
                platform!!.setJitterUnder(this, color, level, 20, 1f, 30f)
                platform!!.isHoldFireOneFrame = true
            }
            else if (state == ShipSystemStatsScript.State.ACTIVE) {
                platform!!.alphaMult = 1f
            }
            else if (state == ShipSystemStatsScript.State.OUT) {
                var level = 1 - (1 * effectLevel)
                platform!!.alphaMult = effectLevel
                platform!!.setJitterUnder(this, color, level, 20, 1f, 30f)
                platform!!.isHoldFireOneFrame = true
            }
        }


        val target = ship!!.mouseTarget
        var test = ""
    }

    fun spawnPlatform() {

        var target = ship!!.mouseTarget

        val dist = Misc.getDistance(ship!!.location, target)
        val max = getMaxRange(ship) + ship!!.collisionRadius
        if (dist > max) {
            val dir = Misc.getAngleInDegrees(ship!!.location, target)
            target = Misc.getUnitVectorAtDegreeAngle(dir)
            target.scale(max)
            Vector2f.add(target, ship!!.location, target)
        }

        target = findClearLocation(ship!!, target)

        platform!!.isHoldFireOneFrame = true

        Global.getCombatEngine().addEntity(platform)

        for (weapon in platform!!.allWeapons) {
            if (weapon.isDisabled) {
                weapon.repair()
            }
        }

        platform!!.location.set(target)
        platform!!.hitpoints = platform!!.maxHitpoints


        platform!!.addListener(LeaniraShipsystemDamageListener(ship!!))
    }

    fun spawnShipOrWingDirectly(variant: ShipVariantAPI?, type: FleetMemberType?, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI? {
        val member = Global.getFactory().createFleetMember(type, variant)
        member.owner = owner
        member.crewComposition.addCrew(member.neededCrew)
        member.captain = ship!!.captain

        val platform = Global.getCombatEngine().getFleetManager(owner).spawnFleetMember(member, location, facing, 0f)
        platform.crAtDeployment = combatReadiness
        platform.currentCR = combatReadiness
        platform.owner = owner
        platform.shipAI.forceCircumstanceEvaluation()
        return platform
    }

    override fun unapply(stats: MutableShipStatsAPI, id: String?) {

    }

    protected fun getMaxRange(ship: ShipAPI?): Float {
        return 600f
    }


    override fun getInfoText(system: ShipSystemAPI, ship: ShipAPI): String? {
        if (system.isOutOfAmmo) return null
        if (system.state != SystemState.IDLE) return null
        val target = ship.mouseTarget
        if (target != null) {
            val dist = Misc.getDistance(ship.location, target)
            val max = getMaxRange(ship) + ship.collisionRadius
            return if (dist > max) {
                "OUT OF RANGE"
            } else {
                "READY"
            }
        }
        return null
    }


    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI): Boolean {
        return ship.mouseTarget != null
    }


    private fun findClearLocation(ship: ShipAPI, dest: Vector2f): Vector2f? {
        if (isLocationClear(dest)) return dest
        val incr = 50f
        val tested = WeightedRandomPicker<Vector2f>()
        var distIndex = 1f
        while (distIndex <= 32f) {
            val start = Math.random().toFloat() * 360f
            var angle = start
            while (angle < start + 360) {
                val loc = Misc.getUnitVectorAtDegreeAngle(angle)
                loc.scale(incr * distIndex)
                Vector2f.add(dest, loc, loc)
                tested.add(loc)
                if (isLocationClear(loc)) {
                    return loc
                }
                angle += 60f
            }
            distIndex *= 2f
        }
        return if (tested.isEmpty) dest else tested.pick() // shouldn't happen
    }

    private fun isLocationClear(loc: Vector2f): Boolean {
        for (other in Global.getCombatEngine().ships) {
            if (other.isShuttlePod) continue
            if (other.isFighter) continue

            var otherLoc = other.shieldCenterEvenIfNoShield
            var otherR = other.shieldRadiusEvenIfNoShield
            if (other.isPiece) {
                otherLoc = other.location
                otherR = other.collisionRadius
            }


            val dist = Misc.getDistance(loc, otherLoc)
            val r = otherR
            var checkDist = platform!!.shieldRadiusEvenIfNoShield
            if (dist < r + checkDist) {
                return false
            }
        }
        for (other in Global.getCombatEngine().asteroids) {
            val dist = Misc.getDistance(loc, other.location)
            if (dist < other.collisionRadius + MineStrikeStats.MIN_SPAWN_DIST) {
                return false
            }
        }
        return true
    }


    fun getFuseTime(): Float {
        return 3f
    }



}

class LeaniraShipsystemDamageListener(var parent: ShipAPI) : HullDamageAboutToBeTakenListener {

    override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?, point: Vector2f?, damageAmount: Float): Boolean {
        if (ship!!.isAlive && ship.hitpoints - damageAmount <= 0) {
            if (parent.system.isActive) {
                parent.useSystem()
            }
            ship.hitpoints = 1f
            return true
        }

        return false
    }
}

