package assortment_of_things.abyss.weapons.genesis

import assortment_of_things.combat.VFXRenderer
import assortment_of_things.misc.GraphicLibEffects
import assortment_of_things.misc.levelBetween
import assortment_of_things.misc.levelBetweenReversed
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.entities.Missile
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.combat.getNearestPointOnBounds
import org.lwjgl.util.vector.Vector2f
import java.awt.Color


class AbaddonLanceOnHit : OnHitEffectPlugin {

    companion object {
        fun spawnExplosion(projectile: DamagingProjectileAPI?, point: Vector2f?) {
            if (projectile!!.customData.containsKey("rat_charge_exploded")) return
            projectile!!.setCustomData("rat_charge_exploded", true)

            val dam = projectile!!.damageAmount * 0.15f

          /*  var explosion = DamagingExplosionSpec(1f, 1200f, 600f, dam, dam * 0.5f,
                // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass,
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter,
                5f, 10f, 5f, 100,
                AbyssUtils.GENESIS_COLOR.setAlpha(150), Color(20, 45, 90))

            Global.getCombatEngine().spawnDamagingExplosion(explosion, projectile!!.weapon.ship, projectile.location, false)*/

            Global.getSoundPlayer().playSound("rat_abyss_genesis_large1_explosion", 0.8f, 0.9f, point, Vector2f())

            GraphicLibEffects.CustomRippleDistortion(point,
                Vector2f(),
                1500f,
                12f,
                false,
                0f,
                360f,
                1f,
                0f,0f,1.3f,
                1.5f,0f
            )

            VFXRenderer.addExplosion(7f, Vector2f(point), Color(150, 0, 200), Color(0, 150, 255),1400f, MathUtils.getRandomNumberInRange(0f, 360f), 1.25f)

            Global.getCombatEngine().addPlugin(AbaddonLanceExplosionScript(projectile, Vector2f(projectile.location),7f, projectile.baseDamageAmount))
        }
    }

    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {

        if (target is Missile) return

        if (target is ShipAPI) {
            var angle = Misc.getAngleInDegrees(projectile!!.location, target.location)
            var force = 100f
            if (target.isDestroyer) force += 150f
            if (target.isCruiser) force += 250f
            if (target.isCapital) force += 500f
            CombatUtils.applyForce(target, angle, force)
        }

        spawnExplosion(projectile, point)



       /* val emp = projectile.empAmount * 0.05f
        val dam = projectile.damageAmount * 0.05f

        var targetsNearby = Global.getCombatEngine().ships.filter { it.owner != projectile.weapon.ship.owner && MathUtils.getDistance(it, projectile) <= range && !it.isHulk}

        for (otherTarget in targetsNearby) {

            var count = 3
            if (otherTarget.isFighter) {
                if (Random().nextFloat() >= 0.8f) continue
                count = 2
            }

            for (i in 0 until 1) {
                var color = Misc.interpolateColor(AbyssUtils.GENESIS_COLOR, Color(47, 111, 237), MathUtils.getRandomNumberInRange(0f, 1f))
                color = color.setAlpha(220)

                engine!!.spawnEmpArcPierceShields(projectile.source, point, otherTarget, otherTarget, DamageType.ENERGY, dam, emp,  // emp
                    100000f,  // max range
                    "tachyon_lance_emp_impact", 25f,  // thickness
                    color, color)
            }
        }*/
    }

}

class AbaddonLanceExplosionScript(var projectile: DamagingProjectileAPI, var loc: Vector2f, var duration: Float, var damageAmount: Float) : BaseEveryFrameCombatPlugin() {

    //Prevent the same target from being targeted to often
    //data class ShipCooldowns(var ship: ShipAPI, var cooldown: Float)

    var maxDuration = duration

    var interval = IntervalUtil(0.25f, 0.33f)
    var sinceLastInterval = 0f
    var dmgMult = 0.15f

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)


        if (!Global.getCombatEngine().isPaused) {
            duration -= 1f * amount
            interval.advance(amount)
            sinceLastInterval += 1 * amount
        }

        if (duration < 0) {
            Global.getCombatEngine().removePlugin(this)
            return
        }


        if (interval.intervalElapsed()) {

            var level = duration.levelBetween(0f, maxDuration * 0.85f)

            var damage = damageAmount * sinceLastInterval * level * dmgMult
            var emp = damage * 0.1f

            sinceLastInterval = 0f

           /* var explosion = DamagingExplosionSpec(1f, 1500f, 600f, damage, damage * 0.4f,
                // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass,
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter,
                0f, 0f, 0f, 0,
                Color(0,0,0,0), Color(0, 0, 0))

            explosion.isShowGraphic = false
            explosion.soundSetId = null

            explosion.damageType = DamageType.ENERGY

            Global.getCombatEngine().spawnDamagingExplosion(explosion, projectile!!.weapon.ship, loc, true)*/

            var center = loc

            var targets = Global.getCombatEngine().allObjectGrid.getCheckIterator(projectile.location, 2500f, 2500f)
            for (obj in targets) {
                if (obj !is CombatEntityAPI) continue

                var distance = MathUtils.getDistance(obj, loc)
                if (distance >= 1500) continue

                var targetLoc = obj.location

                var dmgMult = 1f
                var outerRadius = obj.collisionRadius
                var shieldHit = false
                if (obj is ShipAPI) {
                    if (obj.shield != null) {
                        shieldHit = obj.shield.isWithinArc(center)
                    }
                    outerRadius = obj.shieldRadiusEvenIfNoShield
                    obj.exactBounds?.update(obj.location, obj.facing)
                }

                if (obj is MissileAPI) {
                    dmgMult = 0.5f
                }

                var angle = Misc.getAngleInDegrees(targetLoc, center)
                targetLoc = MathUtils.getPointOnCircumference(targetLoc, outerRadius, angle)

                var nearestPoint = obj.getNearestPointOnBounds(targetLoc)

                var rangeLevel = distance.levelBetweenReversed(300f, 1400f)

                Global.getCombatEngine().applyDamage(obj, nearestPoint, damage * dmgMult * rangeLevel, DamageType.ENERGY, emp * rangeLevel, !shieldHit, false, projectile.source, false)
            }




        }



    }




}
