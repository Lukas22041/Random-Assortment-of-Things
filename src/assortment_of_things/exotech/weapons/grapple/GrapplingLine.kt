package assortment_of_things.exotech.weapons.grapple

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
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

        Normal_Length = 15f
        gravity = 0f
        dampening = 0.97f
        var strength = 0.5f



        for (i in currentPoints.indices) {
            if (i == 0) continue

            if (i == currentPoints.size - 1) {
                var X_vector1 = previousPoints[i - 1].x - previousPoints[i].x
                var Y_vector1 = previousPoints[i - 1].y - previousPoints[i].y

                var Magnitude1 = Vector2f(X_vector1, Y_vector1).length()
                var Extension1 = Magnitude1 - Normal_Length

                var xv = (X_vector1 / Magnitude1 * Extension1)
                var yv = (Y_vector1 / Magnitude1 * Extension1) + gravity

                velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength * amount), velocities[i].y * dampening + (yv * strength * amount))
                currentPoints[i] = Vector2f(previousPoints[i].x + velocities[i].x, previousPoints[i].y + velocities[i].y)
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

            velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength * amount), velocities[i].y * dampening + (yv * strength * amount))
            currentPoints[i] = Vector2f(previousPoints[i].x + velocities[i].x, previousPoints[i].y + velocities[i].y)

            //currentPoints[i] = Vector2f(previousPoints[i].x + (xv * .11f), previousPoints[i].y + (yv * .11f))
        }

        currentPoints[0] = Vector2f(weapon.location)

        if (target != null) {

            var loc: Vector2f? = Vector2f(targetPoint)
            loc = Misc.rotateAroundOrigin(loc, target!!.facing)
            Vector2f.add(target!!.location, loc, loc)

            currentPoints[pointCount - 1] = Vector2f(loc)
            //target!!.velocity.set(target!!.velocity.plus(velocities[pointCount - 1]))



        }
        else if (!projectile.isFading) {
            currentPoints[pointCount - 1] = Vector2f(projectile.location)
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
            c.alpha / 255f * (1f))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        for (point in currentPoints) {
            GL11.glVertex2f(point.x, point.y)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }

}