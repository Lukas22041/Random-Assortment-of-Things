package assortment_of_things.exotech.weapons.grapple

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.util.*

class GrapplingLine(var weapon: WeaponAPI, var projectile: DamagingProjectileAPI) : BaseCombatLayeredRenderingPlugin() {


    var sprite = Global.getSettings().getSprite(Global.getSettings().getCommoditySpec(Commodities.ALPHA_CORE).iconName)
    var currentPoints = ArrayList<Vector2f>()
    var previousPoints = ArrayList<Vector2f>()
    var velocities = ArrayList<Vector2f>()

    var gravity = 0f
    var Normal_Length = 1f
    var dampening = 0.99f

    var pointCount = 30

    var target: CombatEntityAPI? = null
    var targetPoint = Vector2f()

    var firstFrame = true
    var hadTarget = false

    var fade = 1f

    init {
        for (i in 0 until pointCount) {

            var loc = (weapon.location.plus(Vector2f(1f * i, 1f * i)))


            currentPoints.add(Vector2f(loc))
            previousPoints.add(Vector2f(loc))
            velocities.add(Vector2f())
        }
    }


    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun getRenderRadius(): Float {
        return 1000000f
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        if (Global.getCombatEngine().isPaused) return

        Normal_Length = 5 + (10 * fade)
        var maximumLength = Normal_Length * pointCount
        gravity = 0f
        dampening = 0.97f
        //var strength = 0.5f
        var strength = 0.5f + ( 50 * (1 - fade))

        var test = projectile.velocity

        if (firstFrame) {
            for (i in 0 until velocities.size) {
                //if (i == 0 || i == pointCount - 1) continue
                var vel = Vector2f(projectile.velocity.x, projectile.velocity.y)
                velocities[i] = vel

                if (i == pointCount-1) {

                }
            }
        }
        firstFrame = false

        for (i in currentPoints.indices) {
            // if (i == 0) continue

            if (i == 0) {
                var X_vector2 = previousPoints[i + 1].x - previousPoints[i].x
                var Y_vector2 = previousPoints[i + 1].y - previousPoints[i].y
                var Magnitude2 = Vector2f(X_vector2, Y_vector2).length()
                var Extension2 = Magnitude2 - Normal_Length

                var xv = (X_vector2 / Magnitude2 * Extension2)
                var yv = (Y_vector2 / Magnitude2 * Extension2) + gravity

                velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength), velocities[i].y * dampening + (yv * strength))
                currentPoints[i] = Vector2f(previousPoints[i].x + (velocities[i].x * amount), previousPoints[i].y + (velocities[i].y * amount))
                //currentPoints[i] = Vector2f(previousPoints[i].x + (xv * 0.01f), previousPoints[i].y + (yv * 0.01f))

                continue
            }

            if (i == currentPoints.size - 1) {
                var X_vector1 = previousPoints[i - 1].x - previousPoints[i].x
                var Y_vector1 = previousPoints[i - 1].y - previousPoints[i].y

                var Magnitude1 = Vector2f(X_vector1, Y_vector1).length()
                var Extension1 = Magnitude1 - Normal_Length

                var xv = (X_vector1 / Magnitude1 * Extension1)
                var yv = (Y_vector1 / Magnitude1 * Extension1) + gravity

                velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength), velocities[i].y * dampening + (yv * strength))
                currentPoints[i] = Vector2f(previousPoints[i].x + (velocities[i].x * amount), previousPoints[i].y + (velocities[i].y * amount))
                //currentPoints[i] = Vector2f(previousPoints[i].x + (xv * 0.01f), previousPoints[i].y + (yv * 0.01f))

                continue
            }

            var X_vector1 = previousPoints[i - 1].x - previousPoints[i].x
            var Y_vector1 = previousPoints[i - 1].y - previousPoints[i].y

            var Magnitude1 = Vector2f(X_vector1, Y_vector1).length()
            var Extension1 = Magnitude1 - Normal_Length

            var X_vector2 = previousPoints[i + 1].x - previousPoints[i].x
            var Y_vector2 = previousPoints[i + 1].y - previousPoints[i].y
            var Magnitude2 = Vector2f(X_vector2, Y_vector2).length()
            var Extension2 = Magnitude2 - Normal_Length

            var xv = (X_vector1 / Magnitude1 * Extension1) + (X_vector2 / Magnitude2 * Extension2)
            var yv = (Y_vector1 / Magnitude1 * Extension1) + (Y_vector2 / Magnitude2 * Extension2) + gravity

            velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength), velocities[i].y * dampening + (yv * strength))
            currentPoints[i] = Vector2f(previousPoints[i].x + (velocities[i].x * amount), previousPoints[i].y + (velocities[i].y * amount))

            //currentPoints[i] = Vector2f(previousPoints[i].x + (xv * .11f), previousPoints[i].y + (yv * .11f))
        }

        currentPoints[0] = Vector2f(weapon.location)

        var combinedLength = 0f
        for (i in 0 until pointCount) {
            if (i == 0) continue
            var distance = MathUtils.getDistance(currentPoints[i], currentPoints[i-1])
            combinedLength += distance
        }

        if (target != null) {

            hadTarget = true
            var pullStrength = 0.1f

            var loc: Vector2f? = Vector2f(targetPoint)
            loc = Misc.rotateAroundOrigin(loc, target!!.facing)
            Vector2f.add(target!!.location, loc, loc)

            currentPoints[pointCount - 1] = Vector2f(loc)

            if (combinedLength >= 2000) {
                target = null
            }


            //target!!.velocity.set(target!!.velocity.plus(velocities[pointCount - 1]))
            //target!!.location.set(target!!.location.x + (velocities[pointCount-1].x * amount), target!!.location.y + (velocities[pointCount-1].y * amount))

            //weapon.ship!!.location.set(weapon.ship!!.location.x + (velocities[0].x * pullStrength * amount), weapon.ship!!.location.y + (velocities[0].y * pullStrength * amount))

           /* if (MathUtils.getDistance(weapon.ship!!, target!!) >= maximumLength) {
                weapon.ship!!.velocity.set(weapon.ship!!.velocity.x + (velocities[0].x * pullStrength * amount), weapon.ship!!.velocity.y + (velocities[0].y * pullStrength * amount))
            }
*/
        }
        else if (!projectile.isFading) {
            currentPoints[pointCount - 1] = Vector2f(projectile.location)
        }

        if ((hadTarget || projectile.isFading) && target == null) {
            fade -= 0.5f * amount
            fade = fade.coerceIn(0f, 1f)
        }

        //velocities[pointCount - 1] = Vector2f(projectile.velocity)

        //weapon.ship.velocity.set(weapon.ship.velocity.minus(velocities[1]))

        previousPoints.clear()
        previousPoints.addAll(currentPoints)
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        /*for (point in currentPoints) {
            sprite.setSize(40f, 40f)
            sprite.renderAtCenter(point.x, point.y)
        }*/

        var c = ExoUtils.color1
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (1f * fade))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        for (point in currentPoints) {
            GL11.glVertex2f(point.x, point.y)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }

}