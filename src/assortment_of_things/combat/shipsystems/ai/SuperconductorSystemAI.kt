package assortment_of_things.combat.shipsystems.ai

import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class SuperconductorSystemAI : ShipSystemAIScript {


    var flags: ShipwideAIFlags? = null
    var ship: ShipAPI? = null


    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.flags = flags
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {

        var targets = CombatUtils.getShipsWithinRange(ship!!.location, 800f).filter { it.owner != ship!!.owner }
        if (flags!!.hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) && targets.isNotEmpty())
        {
            ship!!.useSystem()
        }
    }
}