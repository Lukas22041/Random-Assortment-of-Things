package assortment_of_things.abyss.shipsystem.ai

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class AbyssalCurrentSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        if (target == null) return

        var flags = ship!!.aiFlags
        var system = ship!!.system

        if (system.isCoolingDown) return
        if (system.isActive)  return

        var weapon = ship!!.allWeapons.find { it.spec?.weaponId == "rat_merrow_beam" } ?: return

        var distance = MathUtils.getDistance(ship, target)
        if (distance <= weapon.range) {
            ship!!.useSystem()
        }
    }
}