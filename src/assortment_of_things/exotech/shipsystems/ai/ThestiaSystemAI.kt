package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class ThestiaSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        if (ship!!.system.isActive) return

        var wings = ship!!.allWings
        for (wing in wings) {
            if (target == null) continue
            if (wing.returning.isNotEmpty()) continue
            if (wing.isDestroyed) continue

            var anyInRange = false
            for (fighter in wing.wingMembers) {
                var range = getFurthestWeaponRange(fighter)
                if (MathUtils.getDistance(fighter.location, target.location) <= range + 300) {
                    anyInRange = true
                }
            }
            if (!anyInRange) continue

            ship!!.useSystem()

            break
        }
    }

    fun getFurthestWeaponRange(fighter: ShipAPI) : Float {
        var range = 0f
        for (weapon in fighter.allWeapons) {
            if (weapon.range > range)
            {
                range = weapon.range
            }
        }
        return range
    }

}