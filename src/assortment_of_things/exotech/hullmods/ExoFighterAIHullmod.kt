package assortment_of_things.exotech.hullmods

import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils

class ExoFighterAIHullmod : BaseHullMod() {

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        ship.addListener(ExoFighterAIScript(ship))
    }
}

class ExoFighterAIScript(var ship: ShipAPI) : AdvanceableListener {

    //var minDistance = 600f
    var maxDistance = 600f

    var decelerateInterval = IntervalUtil(0.2f, 0.4f)
    var canDecelerate = true
    var strafeInterval = IntervalUtil(2f, 5f)
    var strafeState = "none"

    init {
        decideWeaponRange()
    }

    fun decideWeaponRange() {
        var shortest = 1200f
        for (weapon in ship.allWeapons) {
            if (weapon.range <= shortest) {
                shortest = weapon.range
            }
        }

        shortest -= MathUtils.getRandomNumberInRange(150f, 250f)
        shortest = MathUtils.clamp(shortest, 200f, 1200f)
        maxDistance = shortest
    }

    override fun advance(amount: Float) {

        strafeInterval.advance(amount)
        if (strafeInterval.intervalElapsed() || strafeState == "none") {
            var states = listOf("left", "right")
            strafeState = states.random()
        }


        if (ship.baseOrModSpec().hullId == "rat_nightblade") {
            if (ship.usableWeapons.all { it.ammo == 0 && !it.isFiring} ) {
                var wing = ship.wing
                if (wing != null && !wing.isReturning(ship)) {
                    wing.orderReturn(ship)
                }
            }
        }

        var target = ship.shipTarget
        if (target != null && (ship.wing != null && !ship.wing.isReturning(ship) )) {

            var distance = MathUtils.getDistance(ship, target)

            if (distance <= maxDistance + 50f) {

                if (strafeState == "left") {
                    ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0)
                }

                if (strafeState == "right") {
                    ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0)
                }
            }

            if (distance <= maxDistance) {
                decelerateInterval.advance(amount)
                if (decelerateInterval.intervalElapsed()) {
                    canDecelerate = true
                    decideWeaponRange()
                }


                if (canDecelerate) {
                    ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0)
                }

            }
            else {
                canDecelerate = false
            }
        }
    }
}