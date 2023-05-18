package assortment_of_things.modular_weapons.effects.plugins

import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect
import com.fs.starfarer.api.impl.combat.RiftLanceEffect
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ModularRiftEmitterPlugin(var projectile: DamagingProjectileAPI, var loopId: String?, var color: Color?) : BaseEveryFrameCombatPlugin() {

    data class EmitterPoints(var location: Vector2f, var emitInterval: IntervalUtil, var despawnInterval: IntervalUtil)

    var interval = IntervalUtil(0.2f, 0.3f)
    var points: MutableList<EmitterPoints> = ArrayList()

    override fun advance(amount: Float, events: List<InputEventAPI?>?) {
        if (Global.getCombatEngine().isPaused) return

        if (loopId != null) {
            Global.getSoundPlayer().playLoop(loopId, projectile, 1f, projectile!!.brightness, projectile!!.location, projectile!!.velocity)
        }

        interval.advance(amount)
        if (interval.intervalElapsed() && !projectile!!.isExpired && !projectile!!.didDamage() && Global.getCombatEngine().isEntityInPlay(projectile)) {

            if (ModularWeaponLoader.getData(projectile!!.weapon.id).rngCheck(0.30f, 0))
            {
                addParticles()
            }
        }

        for (point in points)
        {
            point.emitInterval.advance(amount)
            if (point.emitInterval.intervalElapsed())
            {
                var targets: MutableList<CombatEntityAPI> = ArrayList()

                targets.addAll(CombatUtils.getShipsWithinRange(point.location, 400f).filter { it.owner != projectile!!.source.owner && it.isAlive && !it.isHulk })
                // targets.addAll(CombatUtils.getProjectilesWithinRange(projectile.location, 400f).filter { it.collisionClass == CollisionClass.MISSILE_FF || it.collisionClass == CollisionClass.MISSILE_NO_FF })

                var target = targets.randomOrNull() ?: continue

                val emp = (projectile!!.empAmount * 0.2f) + 10
                val dam = (projectile!!.damageAmount * 0.2f) + 10
                var color = projectile!!.projectileSpec.fringeColor.darker().darker()

                var loc = Vector2f(point.location.x + MathUtils.getRandomNumberInRange(-10f, 10f), point.location.y + MathUtils.getRandomNumberInRange(-10f, 10f))

                Global.getCombatEngine()!!.spawnEmpArc(projectile!!.source, loc, SimpleEntity(loc), target, DamageType.ENERGY, dam, emp,  // emp
                    100000f,  // max range
                    "tachyon_lance_emp_impact", 20f,  // thickness
                    color, Color(255, 255, 255, 255))

            }
        }

        for (point in ArrayList(points))
        {
            point.despawnInterval.advance(amount)
            if (point.despawnInterval.intervalElapsed())
            {
                points.remove(point)
            }
        }


        if (points.isEmpty() && ( projectile!!.isExpired || projectile!!.didDamage() || !Global.getCombatEngine().isEntityInPlay(projectile))) {
            Global.getCombatEngine().removePlugin(this)
        }
    }


    fun addParticles() {
        val engine = Global.getCombatEngine()
        var c = RiftLanceEffect.getColorForDarkening(RiftCascadeEffect.STANDARD_RIFT_COLOR)

        var color = color
        val b = 1f
        color = Misc.scaleAlpha(color, b)
        val baseDuration = 4f
        var size = projectile!!.projectileSpec.width
        val point = Vector2f(projectile!!.location)
        val pointOffset = Vector2f(projectile!!.velocity)
        pointOffset.scale(0.1f)
        Vector2f.add(point, pointOffset, point)
        val vel = Vector2f()

        var rampUp = 0f
        rampUp = 0.5f
        c = color
        for (i in 0..1) {
            var loc: Vector2f? = Vector2f(point)
            loc = Misc.getPointWithinRadius(loc, size * 1f)
            val s = size * 3f * (0.5f + Math.random().toFloat() * 0.5f)

            engine.addNebulaParticle(loc, vel, s, 1.5f, rampUp, 0f, baseDuration, c)

            points.add(EmitterPoints(loc, IntervalUtil(0.5f, 2f), IntervalUtil(3f, 3.5f)))
        }
    }
}