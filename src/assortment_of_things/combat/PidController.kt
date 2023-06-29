package assortment_of_things.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f

//Code from Ruddygreat, which is based off on code from wikipedia and used tomatopastes code as reference.
/**
 * @param Kp movement proportional. higher value increases overshoot.
 * @param Kd movement derivative. higher value dampens oscillation.
 * @param Rp rotational proportional. higher value increases overshoot.
 * @param Rd rotational derivative. higher value dampens oscillation.
 */
class PidController(Kp: Float, Kd: Float, Rp: Float, Rd: Float) {

    //error for x, y & rotation
    var lastErrorX = 0f
    var lastErrorY = 0f
    var lastErrorR = 0f

    //strafe accel is this % of forward accel, used as a mult for the y factors
    val strafeRatio = 0.5f

    //proporional values
    var KpX = 0f
    var KpY = 0f
    var KpR = 0f

    //derivative values
    var KdX = 0f
    var KdY = 0f
    var KdR = 0f


    init {
        KpX = Kp
        KpY = Kp * strafeRatio
        KpR = Rp
        KdX = Kd
        KdY = Kd * strafeRatio
        KdR = Rd
    }

    fun move(dest: Vector2f?, drone: ShipAPI) {
        val diff = Vector2f.sub(dest, drone.location, Vector2f())
        //this one line is from tomato
        //rotate the vector for ??? reasons
        VectorUtils.rotate(diff, 90f - drone.facing)
        val errorX = diff.x
        val derivativeX = (errorX - lastErrorX) / Global.getCombatEngine().elapsedInLastFrame
        val outputX = KpX * errorX + KdX * derivativeX
        val commandX = if (outputX > 0f) ShipCommand.STRAFE_RIGHT else ShipCommand.STRAFE_LEFT
        drone.giveCommand(commandX, null, 0)
        lastErrorX = errorX
        val errorY = diff.y
        val derivativeY = (errorY - lastErrorY) / Global.getCombatEngine().elapsedInLastFrame
        val outputY = KpY * errorY + KdY * derivativeY
        val commandY = if (outputY > 0f) ShipCommand.ACCELERATE else ShipCommand.ACCELERATE_BACKWARDS
        drone.giveCommand(commandY, null, 0)
        lastErrorY = errorY
    }

    fun rotate(destFacing: Float, drone: ShipAPI) {
        val rotationError = MathUtils.getShortestRotation(drone.facing, destFacing)
        val derivativeR = (rotationError - lastErrorR) / Global.getCombatEngine().elapsedInLastFrame
        val outputR = KpR * rotationError + KdR * derivativeR
        val commandR = if (outputR > 0f) ShipCommand.TURN_LEFT else ShipCommand.TURN_RIGHT
        drone.giveCommand(commandR, null, 0)
        lastErrorR = rotationError
    }
}