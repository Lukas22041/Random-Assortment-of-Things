package assortment_of_things.modular_weapons.scripts

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import data.scripts.plugins.MagicTrailPlugin
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color


class ModularWeaponCombatHandler {

    var engine = Global.getCombatEngine()
    var trailsInterval = IntervalUtil(0.025f, 0.025f)

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
            overvolt = overvolt.filter { !it.isExpired }.toMutableList()
            engine!!.customData.set("rat_modular_overvolt_projectiles", overvolt)
        }

        var homing = engine!!.customData.get("rat_modular_homing_projectiles") as MutableList<DamagingProjectileAPI>?
        if (homing != null)
        {
            homing = homing.filter { !it.isExpired }.toMutableList()
            engine!!.customData.set("rat_modular_homing_projectiles", homing)
        }

        var trails = engine!!.customData.get("rat_modular_trail_projectiles") as MutableList<DamagingProjectileAPI>?
        if (trails != null)
        {
            trails = trails.filter { !it.isExpired }.toMutableList()
            engine!!.customData.set("rat_modular_trail_projectiles", trails)
        }



    }



    fun overvolt(amount: Float)
    {
        var projectiles = engine!!.customData.get("rat_modular_overvolt_projectiles") as MutableList<DamagingProjectileAPI>? ?: return
        for (projectile in projectiles)
        {

            var interval = projectile.customData.get("rat_modular_overvolt_interval") as IntervalUtil ?: continue

            var target = CombatUtils.getShipsWithinRange(projectile.location, 400f).filter { it.owner != projectile.source.owner && it.isAlive && !it.isHulk }.randomOrNull() ?: continue

            interval.advance(amount)
            if (interval.intervalElapsed())
            {
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
            var range = projectile.projectileSpec.maxRange
            var speed = projectile.moveSpeed / 10
            var proj = projectile
            var targets: MutableList<ShipAPI> = ArrayList()
            var iter = CombatUtils.getShipsWithinRange(proj.location, range).filter { it.owner != proj.source.owner && it.isAlive && !it.isHulk }

            for (i in iter)
            {
                var target = i
                var sizes = listOf(HullSize.FRIGATE, HullSize.DESTROYER, HullSize.CRUISER, HullSize.CAPITAL_SHIP)
                if (target.owner == 1 && sizes.contains(target.hullSize)) targets.add(target)
            }
            var target: ShipAPI? = null
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

                    MagicTrailPlugin.AddTrailMemberSimple(projectile, id, Global.getSettings().getSprite("fx", "base_trail_rough"),
                        Vector2f(projectile.location.x, projectile.location.y) , 0f, projectile.facing, projectile.projectileSpec.width / 2, 2f, projectile.projectileSpec.fringeColor, 0.5f, 0.1f, 0.0f, 1f, false )

                }
            }
        }

    }



}