package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class GilgameshSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null
    var maxCountdown = 1.5f
    var countdown = maxCountdown

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return

        //Allow phase use if the ship would otherwise get it while the system is active
        if (hasNearbyDanger(amount, missileDangerDir, collisionDangerDir, target, 100f)) {
            ship!!.setCustomData("rat_dont_allow_phase", -1f)
            return
        }

        if (ship!!.system.isActive) return
        if (ship!!.phaseCloak.isActive) return

        if (target == null) return

        if (hasNearbyDanger(amount, missileDangerDir, collisionDangerDir, target, 250f)) {
            countdown = maxCountdown
            return
        }

        var range = getFurthestWeaponRange(ship!!)
        var anyInRange = false
        if (MathUtils.getDistance(ship!!.location, target!!.location) <= range + 50) {
            anyInRange = true
        }

        if (!anyInRange) {
            countdown = maxCountdown
            return
        }
        if (ship!!.fluxTracker.fluxLevel >= 0.8f) {
            countdown = maxCountdown
            return
        }
        if (ship!!.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.STAY_PHASED))  {
            countdown = maxCountdown
            return
        }

        countdown -= 1 * amount
        if (countdown <= 0f) {
            ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.TARGET_FOR_SHIP_SYSTEM, 0.3f, target)
            ship!!.useSystem()

            countdown = 2f
        }


    }

    fun getFurthestWeaponRange(ship: ShipAPI) : Float {
        var range = 0f
        for (weapon in ship.allWeapons.filter { it.slot.id == "WS0004" || it.slot.id == "WS0005"} ) {
            var weaponRange = weapon.range * 1.10f
            if (weapon.type == WeaponAPI.WeaponType.MISSILE) weaponRange *= 0.9f //Adjust for reduction of missile ranges

            if (weaponRange > range)
            {
                range = weaponRange
            }
        }
        return range
    }

    fun hasNearbyDanger(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?, range: Float) : Boolean {

        var incomingMissileDamage = 0f
        var consideredProjectiles = ArrayList<DamagingProjectileAPI>()

        var projectiles = Global.getCombatEngine().allObjectGrid.getCheckIterator(ship!!.location, range, range)
        for (projectile in projectiles) {
            if (consideredProjectiles.contains(projectile))  continue

            if (projectile is DamagingProjectileAPI) {
                if (projectile.owner == ship!!.owner) continue
                consideredProjectiles.add(projectile)
                if (projectile.isExpired) continue
                incomingMissileDamage += projectile.baseDamageAmount
                continue
            }

            if (projectile is MissileAPI) {
                if (projectile.owner == ship!!.owner) continue
                consideredProjectiles.add(projectile)
                if (projectile.isExpired) continue
                incomingMissileDamage += projectile.baseDamageAmount
                continue
            }


        }

        if (incomingMissileDamage > 2000) {
            return true
        }

        return false
    }

}