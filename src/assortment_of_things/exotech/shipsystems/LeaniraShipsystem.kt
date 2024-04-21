package assortment_of_things.exotech.shipsystems

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.MineStrikeStats
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color


class LeaniraShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null
    val color = ExoUtils.color2.setAlpha(150)

    var variant: ShipVariantAPI? = null

    var platform: ShipAPI? = null

    var activated = false

    var moduleDespawnInterval = IntervalUtil(0.1f, 0.1f)

    companion object {
        var range = 600f
    }

    var despawned = false

    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {
        ship = stats.entity as ShipAPI? ?: return
        var system = ship!!.system


        if (platform != null) {
            var level = (platform!!.hitpoints - 0f) / (platform!!.maxHitpoints - 0f)
            level = 1 - level
            var mult = 1 + (1 * level)

            ship!!.system.cooldown = ship!!.system.specAPI.getCooldown(ship!!.mutableStats) * mult

            //weird thing thats required to prevent platforms from delaying combat from ending
            if (!ship!!.isAlive && !despawned) {
                if (!platform!!.isAlive) {
                    Global.getCombatEngine().addEntity(platform!!)
                    platform!!.location.set(Vector2f(1000000f, 0f))
                    /*despawned = true
                    var spec = DamagingExplosionSpec.explosionSpecForShip(platform!!)
                    Global.getCombatEngine().spawnDamagingExplosion(spec, platform!!, Vector2f(platform!!.location))*/
                }
                /*platform!!.hitpoints = 1f*/
                Global.getCombatEngine().applyDamage(platform, platform!!.location, 100000f, DamageType.ENERGY, 1000f, true, false, true )
               // platform!!.splitShip()
                despawned = true
            }

        }



        for (module in ship!!.childModulesCopy) {

            module.alphaMult = 0f
            module.collisionClass = CollisionClass.NONE

            module.shipAI = null
            module.location.set(Vector2f(100000f + ship!!.location.x, 100000f + ship!!.location.y))
            module.extraAlphaMult = 0f
            module.extraAlphaMult2 = 0f
            module.spriteAPI.color = Color(0, 0, 0,0)
            module.mutableStats.hullDamageTakenMult.modifyMult("rat_module_to_be_despawned", 0f)
            module.mutableStats.armorDamageTakenMult.modifyMult("rat_module_to_be_despawned", 0f)

            module.isPhased = true
            module.isHoldFireOneFrame = true

            for (weapon in module.allWeapons) {
                weapon.sprite?.color = Color(0, 0, 0, 0)
                weapon.barrelSpriteAPI?.color = Color(0, 0, 0, 0)
                weapon.glowSpriteAPI?.color = Color(0, 0, 0, 0)
                weapon.underSpriteAPI?.color = Color(0, 0, 0, 0)
            }

            for (engine in module.engineController.shipEngines) {
                engine.engineSlot.color = Color(0, 0, 0, 0)
                engine.engineSlot.contrailColor = Color(0, 0, 0, 0)
                engine.engineSlot.glowAlternateColor = Color(0, 0, 0, 0)
            }

            if (!Global.getCombatEngine().combatUI.isShowingCommandUI) {
                moduleDespawnInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            }

            if (module.hasTag("copied_variant")) continue
            if (!moduleDespawnInterval.intervalElapsed()) continue

            module.addTag("copied_variant")

            module.addListener(object: AdvanceableListener {
                override fun advance(amount: Float) {
                    for (weapon in module.allWeapons) {
                        weapon.setRemainingCooldownTo(999f)
                        //module.location.set(ship!!.location)
                    }
                }
            })

            variant = module.variant
            /*Global.getCombatEngine().removeEntity(module)
            module.hitpoints = 0f*/
            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true
            platform = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, 1f, Vector2f(), ship!!.facing)
            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false
            platform!!.captain = ship!!.captain
            platform!!.setCustomData("rat_apheidas_parent", ship)
            ship!!.setCustomData("rat_leanira_children", platform)

            Global.getCombatEngine().getFleetManager(ship!!.owner).removeDeployed(platform, true)
            //Global.getCombatEngine().removeEntity(platform)
           // Global.getCombatEngine().getFleetManager(ship!!.owner).addToReserves(platform!!.fleetMember)

        }

        if (ship!!.isAlive && activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false

            Global.getCombatEngine().removeEntity(platform)
            //Global.getCombatEngine().getFleetManager(ship!!.owner).addToReserves(platform!!.fleetMember)
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

       /* if (platform != null && (!platform!!.isAlive || !Global.getCombatEngine().isInPlay(platform))) {
          //  platform!!.location.set(ship!!.location)
            platform!!.hitpoints = 0f
        }*/
    }

    fun spawnPlatform() {

        var target = ship!!.mouseTarget
        if (ship!!.shipAI != null && ship!!.aiFlags.hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
            target =  ship!!.aiFlags.getCustom(AIFlags.SYSTEM_TARGET_COORDS) as Vector2f
        }

        val dist = Misc.getDistance(ship!!.location, target)
        val max = range + ship!!.collisionRadius
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

            if (weapon.spec.mountType == WeaponAPI.WeaponType.MISSILE || weapon.spec.mountType == WeaponAPI.WeaponType.SYNERGY) {
                if (weapon.usesAmmo()) {
                    var restore = (weapon.maxAmmo * 0.2f).toInt()
                    restore = MathUtils.clamp(restore, 1, weapon.maxAmmo)
                    weapon.ammo = MathUtils.clamp(weapon.ammo + restore, 1, weapon.maxAmmo)
                }
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



    override fun getInfoText(system: ShipSystemAPI, ship: ShipAPI): String? {
        if (system.isOutOfAmmo) return null
        if (system.state != SystemState.IDLE) return null
        val target = ship.mouseTarget
        if (target != null) {
            val dist = Misc.getDistance(ship.location, target)
            val max = range + ship.collisionRadius
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
        if (parent.isAlive && ship!!.isAlive && ship.hitpoints - damageAmount <= 0) {
            if (parent.system.isActive) {
                parent.useSystem()
            }
            ship.hitpoints = 1f

            return true
        }

        return false
    }
}

