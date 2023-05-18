package assortment_of_things.modular_weapons.scripts

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.entities.Ship
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getDistanceSq
import org.magiclib.plugins.MagicTrailPlugin
import java.awt.Color


class ModularWeaponCombatHandler {

    var engine = Global.getCombatEngine()
    var trailsInterval = IntervalUtil(0.0125f, 0.0125f)

    fun advance(amount: Float)
    {
        if (Global.getCurrentState() == GameState.TITLE) return
        if (engine.isPaused) return


        filterOutExpired()
        doHoming(amount)
        addTrails(amount)
        overvolt(amount)

    }



    fun filterOutExpired()
    {
        var overvolt = engine!!.customData.get("rat_modular_overvolt_projectiles") as MutableList<DamagingProjectileAPI>?
        if (overvolt != null)
        {
            overvolt = overvolt.filter { !it.isExpired || !it.isFading }.toMutableList()
            engine!!.customData.set("rat_modular_overvolt_projectiles", overvolt)
        }

        var homing = engine!!.customData.get("rat_modular_homing_projectiles") as MutableList<DamagingProjectileAPI>?
        if (homing != null)
        {
            homing = homing.filter { !it.isExpired || !it.isFading }.toMutableList()
            engine!!.customData.set("rat_modular_homing_projectiles", homing)
        }

        var trails = engine!!.customData.get("rat_modular_trail_projectiles") as MutableList<DamagingProjectileAPI>?
        if (trails != null)
        {
            trails = trails.filter { !it.isExpired || !it.isFading }.toMutableList()
            engine!!.customData.set("rat_modular_trail_projectiles", trails)
        }



    }



    fun overvolt(amount: Float)
    {
        var projectiles = engine!!.customData.get("rat_modular_overvolt_projectiles") as MutableList<DamagingProjectileAPI>? ?: return
        for (projectile in projectiles)
        {

            var interval = projectile.customData.get("rat_modular_overvolt_interval") as IntervalUtil ?: continue

            interval.advance(amount)
            if (interval.intervalElapsed())
            {



                var targets: MutableList<CombatEntityAPI> = ArrayList()

                targets.addAll(CombatUtils.getShipsWithinRange(projectile.location, 400f).filter { it.owner != projectile.source.owner && it.isAlive && !it.isHulk })
               // targets.addAll(CombatUtils.getProjectilesWithinRange(projectile.location, 400f).filter { it.collisionClass == CollisionClass.MISSILE_FF || it.collisionClass == CollisionClass.MISSILE_NO_FF })

                var target = targets.randomOrNull() ?: continue

                val emp = projectile!!.empAmount * 0.1f
                val dam = projectile.damageAmount * 0.1f
                var color = projectile!!.projectileSpec.fringeColor.darker().darker()

                engine!!.spawnEmpArc(projectile!!.source, projectile.location, target, target, DamageType.ENERGY, dam, emp,  // emp
                    100000f,  // max range
                    "tachyon_lance_emp_impact", 20f,  // thickness
                    color, Color(255, 255, 255, 255))
            }
        }

    }

    //Somewhat copied from Nickes Guidance Script
    fun doHoming(amount: Float)
    {
        var homingProjectiles = engine!!.customData.get("rat_modular_homing_projectiles") as MutableList<DamagingProjectileAPI>? ?: return

        for (projectile in homingProjectiles)
        {
            var range = projectile.projectileSpec.maxRange * 0.75f
            var speed = projectile.moveSpeed / 10
            var proj = projectile
            var targets: MutableList<CombatEntityAPI> = ArrayList()


            var pd = projectile.customData.get("rat_modular_isPD") as Boolean

            if (!pd)
            {
                var iter = CombatUtils.getShipsWithinRange(proj.location, range).filter { it.owner != proj.source.owner && it.isAlive && !it.isHulk }

                for (i in iter)
                {
                    var target = i
                    var sizes = listOf(HullSize.FRIGATE, HullSize.DESTROYER, HullSize.CRUISER, HullSize.CAPITAL_SHIP)
                    if (sizes.contains(target.hullSize)) targets.add(target)
                }
            }
            else
            {
                speed = projectile.moveSpeed / 2

                var iter = engine.allObjectGrid.getCheckIterator(proj.location, range, range)
                for (it in iter)
                {
                    if (it is DamagingProjectileAPI)
                    {
                        if (it.owner == proj.owner) continue
                        if (it.maxHitpoints == 0f) continue
                        if (it.hitpoints == 0f) continue
                        targets.add(it)
                    }
                    if (it is ShipAPI)
                    {
                        if (!it.isFighter) continue
                        if (it.owner == proj.owner) continue
                        if (it.maxHitpoints == 0f) continue
                        if (it.hitpoints == 0f) continue
                        targets.add(it)
                    }


                }
            }


            var target: CombatEntityAPI? = null
            var distance = 100000f
            for (tar in targets)
            {
                var dist = MathUtils.getDistance(proj, tar)
                if (dist < distance)
                {
                    target = tar
                    distance = dist
                }
            }
            if (target == null) continue

            var facing = VectorUtils.getAngle(proj.location, target.location)

            var facingSwayless: Float = proj.facing - 1f
            val angleToHit = VectorUtils.getAngle(proj.location, target.location)
            var angleDiffAbsolute = Math.abs(angleToHit - facingSwayless)
            while (angleDiffAbsolute > 180f) {
                angleDiffAbsolute = Math.abs(angleDiffAbsolute - 360f)
            }
            facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, speed * amount)
            proj.facing = facingSwayless + 1f
            proj.velocity.x =
                MathUtils.getPoint(Vector2f(Misc.ZERO), proj.velocity.length(), facingSwayless + 1f).x
            proj.velocity.y =
                MathUtils.getPoint(Vector2f(Misc.ZERO), proj.velocity.length(), facingSwayless + 1f).y
        }
    }

    fun addTrails(amount: Float)
    {

        trailsInterval.advance(amount)

        if (trailsInterval.intervalElapsed())
        {
            var projectiles = engine!!.customData.get("rat_modular_trail_projectiles") as MutableList<DamagingProjectileAPI>?
            if (projectiles != null)
            {

                for (projectile in projectiles)
                {
                    var id = projectile.customData.get("rat_trail_id") as Float

                    if (projectile.isFading) continue

                    MagicTrailPlugin.addTrailMemberSimple(projectile, id, Global.getSettings().getSprite("fx", "base_trail_rough"),
                        Vector2f(projectile.location.x, projectile.location.y) , projectile.moveSpeed / 10, projectile.facing + MathUtils.getRandomNumberInRange(-1f, 1f), projectile.projectileSpec.width / 2, 2f, projectile.projectileSpec.fringeColor, 0.5f, 0.1f, 0.0f, 1f, false )

                }
            }
        }

    }

}