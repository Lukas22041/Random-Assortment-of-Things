package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f

class AbyssalChargeSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null
    var wasActivated = false
    var interval = IntervalUtil(7f, 9f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system

        if (system.isCoolingDown) return
        if (system.isActive)  return

        ship!!.useSystem()
    }
}