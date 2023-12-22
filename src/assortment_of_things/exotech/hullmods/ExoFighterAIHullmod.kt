package assortment_of_things.exotech.hullmods

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
    var maxDistance = MathUtils.getRandomNumberInRange(380f, 420f)

    var decelerateInterval = IntervalUtil(0.2f, 0.4f)
    var canDecelerate = true
    var strafeInterval = IntervalUtil(2f, 5f)
    var strafeState = "none"

    override fun advance(amount: Float) {

        strafeInterval.advance(amount)
        if (strafeInterval.intervalElapsed() || strafeState == "none") {
            var states = listOf("left", "right")
            strafeState = states.random()
        }

        var target = ship.shipTarget
        if (target != null) {

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