package assortment_of_things.abyss.weapons

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.util.*

class AbssalWaveEmitterEffect : BeamEffectPlugin {

    var empInterval = IntervalUtil(0.25f, 0.4f)
    var damage = 50f

    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI) {

        var weapon = beam.weapon
        var color = AbyssalsAdaptabilityHullmod.getColorForCore(weapon.ship)
        var secondaryColor = AbyssalsAdaptabilityHullmod.getSecondaryColorForCore(weapon.ship)
        var beamColor = Misc.interpolateColor(secondaryColor, color, weapon.ship.system.effectLevel * weapon.ship.system.effectLevel)


        beam.fringeColor = beamColor
        beam.weapon.glowSpriteAPI.color = beamColor

        var target = beam.damageTarget

        var empColor = Misc.interpolateColor(color, secondaryColor, Random().nextFloat() * 0.8f)

        beam.damage.isForceHardFlux = true

        empInterval.advance(amount)
        if (empInterval.intervalElapsed()) {

            var from = Vector2f(weapon.location)

            var leftLoc = Vector2f(2f, 9f)
            var leftAngle = MathUtils.getRandomNumberInRange(20f, 70f)
            var left = Vector2f(from).plus(leftLoc.rotate(weapon.ship.facing))

            var rightLoc = Vector2f(2f, -9f)
            var rightAngle = MathUtils.getRandomNumberInRange(-20f, -70f)
            var right = Vector2f(from).plus(rightLoc.rotate(weapon.ship.facing))

            if (target is ShipAPI) {
                Global.getCombatEngine()!!.spawnEmpArc(weapon.ship, left, target, target, DamageType.ENERGY, damage, damage, 100000f,"tachyon_lance_emp_impact",
                    10f, empColor,  empColor)

                Global.getCombatEngine()!!.spawnEmpArc(weapon.ship, right, target, target, DamageType.ENERGY, damage, damage, 100000f,"tachyon_lance_emp_impact",
                    10f, empColor,  empColor)

            } else {
                var toLeft = MathUtils.getPointOnCircumference(from, MathUtils.getRandomNumberInRange(20f, 40f), weapon.ship.facing + leftAngle)
                var toRight = MathUtils.getPointOnCircumference(from, MathUtils.getRandomNumberInRange(20f, 40f), weapon.ship.facing + rightAngle)

                Global.getCombatEngine().spawnEmpArcVisual(left, weapon.ship, toLeft, SimpleEntity(toLeft), 5f, empColor.setAlpha(75), empColor.setAlpha(75))
                Global.getCombatEngine().spawnEmpArcVisual(right, weapon.ship, toRight, SimpleEntity(toRight), 5f, empColor.setAlpha(75), empColor.setAlpha(75))
            }
        }


    }
}