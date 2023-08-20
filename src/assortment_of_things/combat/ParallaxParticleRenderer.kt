package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class ParallaxParticleRenderer : BaseEveryFrameCombatPlugin(), CombatLayeredRenderingPlugin {

    var sprite: SpriteAPI? = null
    var wormhole: SpriteAPI? = null
    var wormhole2: SpriteAPI? = null

    companion object {
        var particles = ArrayList<ParallaxParticle>()

        fun createParticle(duration: Float, location: Vector2f, velocity: Vector2f, radius: Float, randomSize: Float, points: Int) {
            particles.add(ParallaxParticle(duration, location, velocity, radius, randomSize, points))
        }
    }

    class ParallaxParticle(var duration: Float, var location: Vector2f, var velocity: Vector2f, var radius: Float, var randomSize: Float, var points: Int) {

        var extraX = ArrayList<Float>()
        var extraY = ArrayList<Float>()
        var maxDuration = duration

        init {
            for (i in 0..points) {
                extraX.add(MathUtils.getRandomNumberInRange(0f, randomSize))
                extraY.add(MathUtils.getRandomNumberInRange(0f, randomSize))
            }
        }
    }



    override fun init(engine: CombatEngineAPI?) {
        engine!!.addLayeredRenderingPlugin(this)
        sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/ParticleBackground.jpg")
        wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
        wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
    }

    override fun init(entity: CombatEntityAPI?) {

    }

    override fun cleanup() {
        particles.clear()
    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {
        for (particle in ArrayList(particles)) {
            particle.duration -= 1 * amount

            if (particle.duration < 0) {
                particles.remove(particle)
            }

            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)
        }
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
       return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES)
    }

    override fun getRenderRadius(): Float {
        return 1000000f
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        var width = viewport!!.visibleWidth
        var height = viewport.visibleHeight

        var x = viewport.llx
        var y = viewport.lly

        var color = AbyssUtils.ABYSS_COLOR

        startStencil()

        sprite!!.setSize(width, height)
        sprite!!.color = Color(50, 50, 50)
        sprite!!.alphaMult = 1f
        sprite!!.render(x, y)

        wormhole!!.setSize(width * 0.2f, width * 0.2f)
        wormhole!!.setAdditiveBlend()
        wormhole!!.alphaMult = 0.6f
        if (!Global.getCombatEngine().isPaused) wormhole!!.angle += 0.075f
        wormhole!!.color = color
        wormhole!!.renderAtCenter(x + width / 2, y + height / 2)

        wormhole!!.setSize(width * 0.4f, width * 0.4f)
        wormhole!!.setAdditiveBlend()
        wormhole!!.alphaMult = 0.6f
        if (!Global.getCombatEngine().isPaused) wormhole!!.angle += 0.075f
        wormhole!!.color = color
        wormhole!!.renderAtCenter(x + width / 2, y + height / 2)

        wormhole!!.setSize(width * 0.8f, width * 0.8f)
        wormhole!!.setAdditiveBlend()
        wormhole!!.alphaMult = 0.6f
        if (!Global.getCombatEngine().isPaused) wormhole!!.angle += 0.075f
        wormhole!!.color = color
        wormhole!!.renderAtCenter(x + width / 2, y + height / 2)

        wormhole2!!.setSize(width * 1.35f, width *  1.35f)
        wormhole2!!.setAdditiveBlend()
        wormhole2!!.alphaMult = 0.5f
        if (!Global.getCombatEngine().isPaused) wormhole2!!.angle += 0.05f
        wormhole2!!.color = color
        wormhole2!!.renderAtCenter(x + width / 2, y + height / 2)

        endStencil()


        startStencil(true)

        renderBorder(color)

        endStencil()
    }

    fun startStencil(inverted: Boolean = false) {

        GL11.glClearStencil(0);
        GL11.glStencilMask(0xff);
        //set everything to 0
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        //disable drawing colour, enable stencil testing
        GL11.glColorMask(false, false, false, false); //disable colour
        GL11.glEnable(GL11.GL_STENCIL_TEST); //enable stencil

        // ... here you render the part of the scene you want masked, this may be a simple triangle or square, or for example a monitor on a computer in your spaceship ...
        //begin masking
        //put 1s where I want to draw
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff); // Do not test the current value in the stencil buffer, always accept any value on there for drawing
        GL11.glStencilMask(0xff);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE); // Make every test succeed

        // <draw a quad that dictates you want the boundaries of the panel to be>

        for (particle in particles) {
            GL11.glBegin(GL11.GL_POLYGON) // Middle circle

            var level = (particle.duration - 0f) / (particle.maxDuration - 0f)
            var radius = particle.radius * level
            val x = particle.location.x
            val y = particle.location.y

            for (i in 0..particle.points) {

                var extraX = particle.extraX.get(i) * level
                var extraY = particle.extraY.get(i) * level

                val angle: Double = (2 * Math.PI * i / particle.points)
                val vertX: Double = Math.cos(angle) * (radius + extraX)
                val vertY: Double = Math.sin(angle) * (radius + extraY)
                GL11.glVertex2d(x + vertX, y + vertY)
            }

            GL11.glEnd()
        }



        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        if (!inverted) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        }
        else
        {
            GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        }
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    fun renderBorder(color: Color) {
        var c = color
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

        for (particle in particles) {
            GL11.glBegin(GL11.GL_LINE_STRIP)

            var level = (particle.duration - 0f) / (particle.maxDuration - 0f)
            var radius = (particle.radius + 0.5f) * level
            val x = particle.location.x
            val y = particle.location.y

            for (i in 0..particle.points) {

                var extraX = particle.extraX.get(i) * level
                var extraY = particle.extraY.get(i) * level

                val angle: Double = (2 * Math.PI * i / particle.points)
                val vertX: Double = Math.cos(angle) * (radius + extraX)
                val vertY: Double = Math.sin(angle) * (radius + extraY)
                GL11.glVertex2d(x + vertX, y + vertY)
            }

            GL11.glEnd()
        }

        GL11.glPopMatrix()
    }

}