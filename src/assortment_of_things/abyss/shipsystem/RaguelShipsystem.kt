package assortment_of_things.abyss.shipsystem

import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class RaguelShipsystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.15f, 0.15f)

    override fun apply(stats: MutableShipStatsAPI?, id: String,  state: ShipSystemStatsScript.State?,  effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        var ship = stats!!.entity
        if (ship !is ShipAPI) return
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        if (!ship.hasListenerOfClass(RaguelDamagerListener::class.java)) {
            ship.addListener(RaguelDamagerListener(ship))
        }

        var system = ship.system

        var color = Color(196, 20, 35, 255)

        if (system.isActive) {

            ship!!.setJitterUnder(this, color, 1f, 25, 0f, 6 * effectLevel)

            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(75), color.setAlpha(75), 2f, 2f, Vector2f().plus(ship!!.location))

                ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                var from = Vector2f(ship!!.exactBounds.segments.random().p1)
                var to = Vector2f(ship!!.exactBounds.segments.random().p1)

                Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, color.setAlpha(75), color.setAlpha(75))
            }
        }

        stats.maxSpeed.modifyFlat(id, 50f * effectLevel)

        val shipTimeMult = 1f + (4f - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }


    }

    class RaguelDamagerListener(var ship: ShipAPI) : DamageDealtModifier {

        var requiredDamage = 5000
        var damageSoFar = 0f

        override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?,  damage: DamageAPI?, point: Vector2f?, shieldHit: Boolean): String? {


            if (ship.system.isActive) return null

            if (target is ShipAPI && !target.isAlive) return null

            if (param is BeamAPI) {
                damageSoFar +=  damage!!.damage * damage.dpsDuration
            }
            else {
                damageSoFar +=  damage!!.damage
            }


            if (damageSoFar > requiredDamage) {
                damageSoFar = 0f

                ship.system.ammo = MathUtils.clamp(ship.system.ammo + 1, 0, ship.system.maxAmmo)
            }

            return null
        }
    }
}