package assortment_of_things.exotech.shipsystems.ai

import assortment_of_things.exotech.shipsystems.LeaniraShipsystem
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*

class LeaniraSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null
    var deployedInterval = IntervalUtil(45f, 45f)

    var currentAngleMod = 0f
    var angleModInterval = IntervalUtil(3f, 3f)

    var outOfRangeInterval = IntervalUtil(7f, 7f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship!!.mouseTarget == null) return
        var module = ship!!.customData.get("rat_leanira_children") as ShipAPI? ?: return

        var system = ship!!.system

        var weaponRange = getFurthestWeaponRange(module)

        angleModInterval.advance(amount)
        if (angleModInterval.intervalElapsed()) {

            currentAngleMod =  MathUtils.getRandomNumberInRange(25f, 65f)
            if (Random().nextFloat() >= 0.5f) {
                currentAngleMod =  MathUtils.getRandomNumberInRange(-25f, -65f)
            }
        }

        if (!system.isActive && target != null) {

            var useSystem = false

            //Checks that atleast one weapon is in range

            //-200 as the platform is usually set in front anyways
            if (target.owner == ship!!.owner) return
            if (ship!!.fluxLevel >= 0.8f) return


            var potentialLocation = lookForSystemLocation(ship!!, module, target)

            if (MathUtils.getDistance(potentialLocation, target.location) < weaponRange * 0.8f) useSystem = true
            //if (ship!!.hitpoints <= ship!!.maxHitpoints * 0.5f) useSystem = true

            if (useSystem) {

                deployedInterval = IntervalUtil(45f, 45f)
                ship!!.useSystem()
            }


        }
        else if (system.isActive){
            var despawn = false
            if (module.hitpoints <= module.maxHitpoints * 0.45f) despawn = true

            if (target != null && MathUtils.getDistance(target, module) >= weaponRange * 1.05f) {
                outOfRangeInterval.advance(amount)
                if (outOfRangeInterval.intervalElapsed()) {
                    despawn = true
                }
            }
            else {
                outOfRangeInterval = IntervalUtil(7f, 7f)
            }

            if (target == null || !target.isAlive) despawn = true

            deployedInterval.advance(amount)
            if (deployedInterval.intervalElapsed()) {
                despawn = true
            }

            if (despawn) {
                ship!!.useSystem()
            }
        }
    }

    fun getFurthestWeaponRange(module: ShipAPI) : Float {
        var range = 0f
        for (weapon in module.allWeapons) {
            if (weapon.range > range)
            {
                range = weapon.range
            }
        }
        return range
    }

    fun lookForSystemLocation(ship: ShipAPI, module: ShipAPI, target: ShipAPI) : Vector2f {

        var weaponRange = getFurthestWeaponRange(module)
        var angleToTarget = Misc.getAngleInDegrees(ship.location, target.location)

        var range = LeaniraShipsystem.range + 100f


        var position = MathUtils.getPointOnCircumference(ship.location, range, angleToTarget + currentAngleMod)
        ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS, 1f, position)

        return position
    }

}