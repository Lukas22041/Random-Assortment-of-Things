package assortment_of_things.abyss.shipsystem.charybdis.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f

class CharybdisHeadSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    var performanceInterval = IntervalUtil(0.1f, 0.1f)

    var secondaryToggle = false
    var secondaryReasonInterval = IntervalUtil(6f, 6f)



    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return

        var flags = ship!!.aiFlags
        var system = ship!!.phaseCloak


        if (system.state == ShipSystemAPI.SystemState.IN || system.state == ShipSystemAPI.SystemState.OUT || system!!.state == ShipSystemAPI.SystemState.COOLDOWN) {
            return
        }

        if (system.isActive) {
            flags.setFlag(AIFlags.DO_NOT_VENT, 0.2f)
        }

        if (secondaryToggle) {
            secondaryReasonInterval.advance(amount)
            if (secondaryReasonInterval.intervalElapsed()) {
                secondaryToggle = false
            }
        }

        performanceInterval.advance(amount)
        if (performanceInterval.intervalElapsed()) {
            if (system.isActive) {
                shoulDeactivate(amount, missileDangerDir, collisionDangerDir, target)
                return
            }
            else if (!system.isActive) {
                shouldActivate(amount, missileDangerDir, collisionDangerDir, target)
                return
            }
        }
    }


    fun shouldActivate(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        var system = ship!!.phaseCloak
        var flags = ship!!.aiFlags
        var modules = ship!!.childModulesCopy + ship

        var activate = false

        if (!secondaryToggle && flags.hasFlag(AIFlags.PURSUING)) {
            secondaryToggle = true
            activate = true
        }
       /* if (!secondaryToggle && !flags.hasFlag(AIFlags.SAFE_FROM_DANGER_TIME)) {
            secondaryToggle = true
            activate = true
        }*/
        if (!secondaryToggle && flags.hasFlag(AIFlags.MANEUVER_TARGET) && ship!!.fluxLevel < 0.2f)  {
            secondaryToggle = true
            activate = true
        }
        if (hasNearbyDanger(amount, missileDangerDir, collisionDangerDir, target, 75f))  activate = true

        if (ship!!.fluxLevel > 0.8f)  {
            secondaryReasonInterval = IntervalUtil(6f, 6f)
            secondaryToggle = true
            activate = false
        }

         if (activate) {
             system.forceState(ShipSystemAPI.SystemState.IN, 0f)
             flags.setFlag(AIFlags.DO_NOT_VENT, 0.5f)
         }
    }

    fun shoulDeactivate(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {

        var system = ship!!.phaseCloak
        var flags = ship!!.aiFlags
        var modules = ship!!.childModulesCopy + ship

        var deactivate = false

        if (!secondaryToggle && !hasNearbyDanger(amount, missileDangerDir, collisionDangerDir, target, 350f))  deactivate = true
        if (ship!!.fluxLevel > 0.9f) deactivate = true

        if (deactivate) {
            system.forceState(ShipSystemAPI.SystemState.OUT, 0f)
            secondaryReasonInterval = IntervalUtil(6f, 6f)
            secondaryToggle = true
        }

    }

    fun hasNearbyDanger(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?, range: Float) : Boolean {

        var modules = ship!!.childModulesCopy + ship

        var incomingMissileDamage = 0f
        var consideredProjectiles = ArrayList<DamagingProjectileAPI>()
        for (module in modules) {

            var projectiles = Global.getCombatEngine().allObjectGrid.getCheckIterator(module!!.location, range, range)
            for (projectile in projectiles) {
                if (consideredProjectiles.contains(projectile))  continue

                if (projectile is DamagingProjectileAPI) {
                    consideredProjectiles.add(projectile)
                    if (projectile.isExpired) continue
                    incomingMissileDamage += projectile.baseDamageAmount
                    continue
                }

                if (projectile is MissileAPI) {
                    consideredProjectiles.add(projectile)
                    if (projectile.isExpired) continue
                    incomingMissileDamage += projectile.baseDamageAmount
                    continue
                }


            }
        }

        if (incomingMissileDamage > 2000) {
            return true
        }

        return false
    }

}