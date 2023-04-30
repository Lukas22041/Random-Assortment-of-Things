package assortment_of_things.modular_weapons.effects

import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha


class PassiveAcidicPayload : ModularWeaponEffect() {
    override fun getName(): String {
        return "Acidic Payload"
    }

    override fun getCost(): Int {
        return 20
    }

    override fun getIcon(): String {
        return ""
    }

    override fun getTooltip(tooltip: TooltipMakerAPI) {
        tooltip.addPara("25% chance for a projectile to get stuck in the enemy hull, causing its acidic payload to spill, which deals damage over a timeframe of 10 seconds equal to twice the projectiles damage." +
                " Does nothing if a shield is hit.", 0f)
    }

    override fun getResourceCost(): MutableMap<String, Float> {
        return hashMapOf()
    }

    override fun getType(): ModularEffectType {
        return ModularEffectType.Onhit
    }


    override fun onHit(projectile: DamagingProjectileAPI?, target: CombatEntityAPI?, point: Vector2f?, shieldHit: Boolean, damageResult: ApplyDamageResultAPI?, engine: CombatEngineAPI?) {
        super.onHit(projectile, target, point, shieldHit, damageResult, engine)

        if (shieldHit) return
        if (target !is ShipAPI) return
        if (target.isHulk || !target.isAlive) return

        if (!ModularWeaponLoader.getData(projectile!!.weapon.id).rngCheck(0.25f, 0)) return

        engine!!.addPlugin( object : EveryFrameCombatPlugin {

            var offset = Vector2f()
            var loc = Vector2f()
            var done = false

            var damageDealt = projectile!!.damageAmount * 2

            var color = projectile!!.projectileSpec.fringeColor

            var duration = 10f
            var interval = IntervalUtil(0.05f, 0.05f)

            override fun init(engine: CombatEngineAPI?) {
                offset = Vector2f.sub(point, target!!.location, Vector2f())
                offset = Misc.rotateAroundOrigin(offset, -target.facing)
            }

            override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {


            }

            override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
                if (engine.isPaused) return
                if (target.isHulk || !target.isAlive || done)
                {
                    engine.removePlugin(this)
                }

                duration -= 1 * amount
                if (duration < 1)
                {
                    done = true
                }

                loc = Vector2f(offset)
                loc = Misc.rotateAroundOrigin(loc, target!!.facing)
                loc = Vector2f.add(target!!.location, loc, loc)

                var vel = Vector2f(0f, 1f).rotate(target.facing - MathUtils.getRandomNumberInRange(130, 230))

                engine.applyDamage(target, loc, damageDealt * amount / duration , DamageType.HIGH_EXPLOSIVE, 0f, true, true, projectile.weapon.ship)

                interval.advance(amount)
                if (interval.intervalElapsed())
                {
                    engine.addNebulaParticle(loc, vel, 10f, 2f , 0.2f, 0.2f, 1f, color.setAlpha(90));
                }

            }

            override fun renderInWorldCoords(viewport: ViewportAPI?) {

            }

            override fun renderInUICoords(viewport: ViewportAPI?) {

            }
        })




    }



}