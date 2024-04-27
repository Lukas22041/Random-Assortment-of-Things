package assortment_of_things.abyss.weapons

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class GenesisLarge1Effect : BaseCombatLayeredRenderingPlugin(), EveryFrameWeaponEffectPlugin {

    var weapon: WeaponAPI? = null

    var color = AbyssUtils.GENESIS_COLOR
    var particleSprite = Global.getSettings().getAndLoadSprite("graphics/fx/particlealpha64sq.png")
    var energyBallSprite = Global.getSettings().getAndLoadSprite("graphics/fx/genesis_weapon_glow.png")

    class BallGlow(var angle: Float, var speed: Float, var sizeMult: Float)

    class WeaponParticle(var location: Vector2f, var velocity: Vector2f, var maxLifetime: Float, var lifetime: Float)

    var ballGlows = ArrayList<BallGlow>()

    var empInterval = IntervalUtil(0.2f, 0.2f)

    init {
        Global.getCombatEngine().addLayeredRenderingPlugin(this)

        for (i in 0..10) {
            var glow = BallGlow(MathUtils.getRandomNumberInRange(0f, 360f), MathUtils.getRandomNumberInRange(10f, 60f), MathUtils.getRandomNumberInRange(0.5f, 1.2f))
            ballGlows.add(glow)

        }
    }

    override fun getRenderRadius(): Float {
        return 100000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER)
    }

    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI?) {
        this.weapon = weapon

        for (ball in ballGlows) {
            ball.angle += ball.speed * amount
        }

        var loc = weapon!!.location.plus(Vector2f(weapon!!.spec.turretFireOffsets.get(0)).rotate(weapon!!.currAngle))
        if (!weapon!!.slot.isHardpoint) weapon!!.location.plus(Vector2f(weapon!!.spec.hardpointFireOffsets.get(0)).rotate(weapon!!.currAngle))

        var level = weapon!!.chargeLevel * 1.5f
        level = level.coerceIn(0f, 1f)

        if (weapon!!.cooldownRemaining > 0) level = 0f


        var intervalAmount = amount * level
        empInterval.advance(intervalAmount)
        if (empInterval.intervalElapsed()) {
            weapon!!.ship.exactBounds.update(weapon.ship.location, weapon.ship.facing)
            var to = weapon!!.ship.exactBounds.segments.random().p1


            var color = Misc.interpolateColor(AbyssUtils.GENESIS_COLOR, Color(156, 40, 65), MathUtils.getRandomNumberInRange(0f, 1f) * 0.5f)

            Global.getCombatEngine().spawnEmpArcVisual(loc, weapon.ship, to, SimpleEntity(to),
                MathUtils.getRandomNumberInRange(1f, 5f), color, color)

            Global.getCombatEngine()!!.spawnExplosion(loc, weapon!!.ship.velocity, color, 5f, 5f)

        }





    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        if (weapon == null) return

        var loc = weapon!!.location.plus(Vector2f(weapon!!.spec.turretFireOffsets.get(0)).rotate(weapon!!.currAngle))
        if (!weapon!!.slot.isHardpoint) weapon!!.location.plus(Vector2f(weapon!!.spec.hardpointFireOffsets.get(0)).rotate(weapon!!.currAngle))

        for (ball in ballGlows) {

            var level = weapon!!.chargeLevel * 1.5f
            level = level.coerceIn(0f, 1f)

            if (weapon!!.cooldownRemaining > 0) level = 0f

            var sizeMult = ball.sizeMult * level


            energyBallSprite.color = color
            energyBallSprite.alphaMult = level * 0.33f
            energyBallSprite.setAdditiveBlend()
            energyBallSprite.setSize(60f * sizeMult, 60f * sizeMult)
            energyBallSprite.angle = ball.angle
            energyBallSprite.renderAtCenter(loc.x, loc.y)
        }



    }
}

