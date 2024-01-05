package assortment_of_things.exotech.shipsystems.ai

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class ArkasSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null
    var interval = IntervalUtil(4f, 5f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        if (ship!!.system.isActive)  {
            return
        }

        if (target == null) return

        var wantsToActivate = false

        if (isInRange(ship!!, target)) wantsToActivate = true

        if (ship!!.fluxLevel >= 0.65f) wantsToActivate = false
        if (ship!!.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.STAY_PHASED)) wantsToActivate = false
        if (!wantsToActivate) {
            interval = IntervalUtil(4f, 5f)
            return
        }

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            ship!!.useSystem()
        }
    }

    fun isInRange(ship: ShipAPI, target: ShipAPI) : Boolean {
        var range = 300f
        for (weapon in ship.allWeapons) {
            if (weapon.spec.weaponId == "rat_arkas_launcher_left" || weapon.spec.weaponId == "rat_arkas_launcher_right") continue
            var wRange = weapon.range
            if (weapon.type == WeaponAPI.WeaponType.MISSILE) {
                wRange = MathUtils.clamp(wRange, 0f, 1200f)
            }
            if (weapon.range > range)
            {
                range = wRange
            }
        }
        var distance = MathUtils.getDistance(ship, target)
        var inRange = distance <= range - 100 //-100 so it doesnt activate to early
        return inRange
    }

}