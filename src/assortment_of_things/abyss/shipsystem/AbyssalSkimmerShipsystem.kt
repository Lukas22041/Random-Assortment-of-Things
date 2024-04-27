package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssalSkimmerShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null

    var activated = false
    var init = false

    var afterimageInterval = IntervalUtil(0.020f, 0.020f)
    var empInterval = IntervalUtil(0.1f, 0.2f)

    var startLocation = Vector2f()
    var targetLocation = Vector2f()
    var startDistance = 0f

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        ship = stats!!.entity as ShipAPI

        if (state == ShipSystemStatsScript.State.IN && !init) {
            onInit(ship!!)

            init = true
        }
        if (state == ShipSystemStatsScript.State.IDLE) {
            init = false
        }

        /*if (state == ShipSystemStatsScript.State.ACTIVE && !activated) {
            ship!!.location.set(targetLocation)
            activated = true
        }
        if (state == ShipSystemStatsScript.State.IDLE) {
            activated = false
        }*/

        if (ship!!.system.isActive) {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.enableBlink()
        } else
        {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.disableBlink()
        }

        var color = AbyssalsAdaptabilityHullmod.getColorForCore(ship!!).setAlpha(100)
        var secondaryColor = AbyssalsAdaptabilityHullmod.getSecondaryColorForCore(ship!!).setAlpha(75)

        if (state == ShipSystemStatsScript.State.IN) {
            var angle =  Misc.getAngleInDegrees(startLocation, targetLocation)
            var currentPoint = MathUtils.getPointOnCircumference(startLocation, startDistance * effectLevel, angle)
            ship!!.location.set(currentPoint)
        }

        if (state == ShipSystemStatsScript.State.IN) {
            ship!!.isPhased = true
        }
        if (state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.IDLE) {
            ship!!.isPhased = false
        }


        if (ship!!.system.isActive) {

            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                if (state == ShipSystemStatsScript.State.IN) {
                    AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(125), secondaryColor.setAlpha(75), 0.65f, 25f, Vector2f(ship!!.location))
                }
            }

            empInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (empInterval.intervalElapsed() && !Global.getCombatEngine().isPaused) {
                ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                var from = Vector2f(ship!!.exactBounds.segments.random().p1)
                var angle = Misc.getAngleInDegrees(ship!!.location, from)
                var to = Vector2f(ship!!.exactBounds.segments.random().p1)

                var empColor = Misc.interpolateColor(color, secondaryColor, Random().nextFloat() * 0.75f)
                Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, empColor.setAlpha(75), empColor.setAlpha(75))
            }

            ship!!.setJitter(this, color, 1f * effectLevel , 3, 0f, 15f)
            ship!!.setJitterUnder(this, color, 1f * effectLevel, 25, 0f, 25f)

        }
        val shipTimeMult = 1f + (6f - 1f) * effectLevel
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (Global.getCombatEngine().playerShip == ship) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        stats.fluxDissipation.modifyMult(id, 1 + (0.25f * effectLevel))
    }

    fun onInit(ship: ShipAPI) {
        startLocation = Vector2f(ship.location)
        targetLocation = findTargetLocation(ship, 5f)
        startDistance = MathUtils.getDistance(startLocation, targetLocation)
    }

    fun findTargetLocation(ship: ShipAPI, mult: Float) : Vector2f {
        var iter = Global.getCombatEngine().allObjectGrid.getCheckIterator(ship.location, 1000f, 1000f)

        var velocity = Vector2f(ship.velocity.x * mult, ship.velocity.y * mult)
        var velX = MathUtils.clamp(velocity.x, -200f, 200f)
        var velY = MathUtils.clamp(velocity.y, -200f, 200f)
        velocity = Vector2f(velX, velY)

        var location = Vector2f(ship.location.x + velocity.x, ship.location.y + velocity.y)

        if (mult < 0.1f) return location

        for (it in iter) {
            if (it == ship) continue
            if (it !is CombatEntityAPI) continue
            if (it is DamagingProjectileAPI) continue
            var colRadius = (it.collisionRadius / 2 + ship.collisionRadius / 2) * 1.5f

            if (MathUtils.getDistance(location, it.location) < colRadius) {
                return findTargetLocation(ship, mult - 0.2f)
            }
        }

        return location
    }
}