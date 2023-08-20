package assortment_of_things.abyss.shipsystem.ai

import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class SarielSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        var flags = ship!!.aiFlags
        var system = ship!!.system


        if (target == null) return

        var distance = MathUtils.getDistance(ship, target)
        if (distance < 1000 && flags.hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET)) ship!!.useSystem()
        if (distance < 1000 && (flags.hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF) || flags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF))) ship!!.useSystem()
        if (distance < 1400 && ship!!.fluxLevel > 0.6f) ship!!.useSystem()

    }

}