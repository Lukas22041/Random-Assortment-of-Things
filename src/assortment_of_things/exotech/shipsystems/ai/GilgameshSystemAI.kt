package assortment_of_things.exotech.shipsystems.ai

import assortment_of_things.exotech.hullmods.PhaseshiftShield
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
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

        var phaseshiftShieldListener = ship!!.getListeners(PhaseshiftShield.PhaseshiftShieldListener::class.java).firstOrNull()

        //Force in to phase to regen shield outside of combat encounters
        if (phaseshiftShieldListener != null && target == null || MathUtils.getDistance(ship, target) >= 2000) {

            if (phaseshiftShieldListener!!.shieldHP / PhaseshiftShield.PhaseshiftShieldListener.maxShieldHP < 1f && ship!!.fluxLevel <= 0.3f) {

                if (!ship!!.phaseCloak.isActive && !ship!!.system.isCoolingDown && !ship!!.fluxTracker.isOverloadedOrVenting) {
                    ship!!.phaseCloak.forceState(ShipSystemAPI.SystemState.IN, 0f)
                }

                if (ship!!.phaseCloak.isActive) {
                    ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.STAY_PHASED, 0.5f)
                    ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_VENT, 3f)
                }
            }
        }

        //Stay phased for longer if it has the flux to spare to regen shield
        if (phaseshiftShieldListener != null) {
            if (phaseshiftShieldListener!!.shieldHP / PhaseshiftShield.PhaseshiftShieldListener.maxShieldHP < 1f && ship!!.fluxLevel <= 0.25f) {
                if (ship?.phaseCloak!!.isActive) {
                    ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.STAY_PHASED, 0.25f)
                }
            }
        }

        var shieldLevel = 0f
        if (phaseshiftShieldListener != null) shieldLevel = MathUtils.clamp(phaseshiftShieldListener.shieldHP / PhaseshiftShield.PhaseshiftShieldListener.maxShieldHP, 0f, 1f)
        var shieldCapable = shieldLevel >= 0.5 && ship!!.fluxLevel <= 0.6f

        //Allow phase use if the ship would otherwise get it while the system is active
        if (!shieldCapable && hasNearbyDanger(amount, missileDangerDir, collisionDangerDir, target, 100f)) {
            ship!!.setCustomData("rat_dont_allow_phase", -1f)
            return
        }

        if (ship!!.system.isActive) return
        if (ship!!.phaseCloak.isActive) return

        if (target == null) return

        if (!shieldCapable && hasNearbyDanger(amount, missileDangerDir, collisionDangerDir, target, 250f)) {
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