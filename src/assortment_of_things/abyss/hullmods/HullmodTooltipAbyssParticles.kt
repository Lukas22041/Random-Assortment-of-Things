package assortment_of_things.abyss.hullmods

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import lunalib.lunaUI.elements.LunaElement
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class HullmodTooltipAbyssParticles(var tooltip: TooltipMakerAPI, var initialHeight: Float, var color: Color = AbyssUtils.ABYSS_COLOR) {

    companion object {
        var particles = ArrayList<AbyssalLightParticle>()
    }

    class AbyssalLightParticle(var fadeIn: Float, var duration: Float, var fadeOut: Float, var color: Color, var alpha: Float, var size: Float, var location: Vector2f, var velocity: Vector2f) {

        enum class ParticleState {
            FadeIn, Mid, FadeOut
        }

        var state = ParticleState.FadeIn

        var level = 0f

        var maxFadeIn = fadeIn
        var maxDuration = duration
        var maxFadeOut = fadeOut

        var adjustment = MathUtils.getRandomNumberInRange(-5f, 5f)
        var adjustmentInterval = IntervalUtil(0.5f, 0.75f)
    }


    var particleInterval = IntervalUtil(0.2f, 0.2f)

    var halo = Global.getSettings().getSprite("rat_terrain", "halo")
    var foreground = Global.getSettings().getAndLoadSprite("graphics/fx/rat_darkener.png")


    fun advance(element: LunaElement, amount: Float) {

        for (particle in ArrayList(particles)) {

            if (particle.state == AbyssalLightParticle.ParticleState.FadeIn) {
                particle.fadeIn -= 1 * amount

                var level = (particle.fadeIn - 0f) / (particle.maxFadeIn - 0f)
                particle.level = 1 - level

                if (particle.fadeIn < 0) {
                    particle.state = AbyssalLightParticle.ParticleState.Mid
                }
            }

            if (particle.state == AbyssalLightParticle.ParticleState.Mid) {
                particle.duration -= 1 * amount


                particle.level = 1f

                if (particle.duration < 0) {
                    particle.state = AbyssalLightParticle.ParticleState.FadeOut
                }
            }

            if (particle.state == AbyssalLightParticle.ParticleState.FadeOut) {
                particle.fadeOut -= 1 * amount

                particle.level = (particle.fadeOut - 0f) / (particle.maxFadeOut - 0f)

                if (particle.fadeOut < 0) {
                    particles.remove(particle)
                    continue
                }
            }

            particle.adjustmentInterval.advance(amount)
            if (particle.adjustmentInterval.intervalElapsed()) {
                var velocity = Vector2f(0f, 0f)
                particle.adjustment = MathUtils.getRandomNumberInRange(-20f, 20f)
            }

            particle.velocity = particle.velocity.rotate(particle.adjustment * amount)


            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)

        }

        particleInterval.advance(amount)
        if (particleInterval.intervalElapsed()) {



            var count = 2
            var fadeInOverwrite = false


            if (particles.size <= 10) {
                count = 10
                fadeInOverwrite = true
            }

            for (i in 0..count) {

                var velocity = Vector2f(0f, 0f)
                velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(30f, 50f), MathUtils.getRandomNumberInRange(0f, 360f)))

                var spawnLocation = Vector2f(element.x + tooltip.widthSoFar / 2, element.y - tooltip.heightSoFar / 2)
                //var spawnLocation = MathUtils.getPointOnCircumference(Vector2f(), 45f, entity.facing + 180)

                var randomX = MathUtils.getRandomNumberInRange(-200f, 200f)
                var randomY = MathUtils.getRandomNumberInRange(-200f, 200f)

                spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

                var fadeIn = MathUtils.getRandomNumberInRange(1.5f, 3f)
                //if (fadeInOverwrite) fadeIn = 0.05f
                var duration = MathUtils.getRandomNumberInRange(2f, 3f)
                var fadeOut = MathUtils.getRandomNumberInRange(1f, 2f)

                var size = MathUtils.getRandomNumberInRange(15f, 30f)

                var alpha = MathUtils.getRandomNumberInRange(0.15f, 0.20f)

                particles.add(AbyssalLightParticle(fadeIn, duration, fadeOut, color, alpha, size, spawnLocation, velocity))
            }
        }

    }

    fun renderBelow(element: LunaElement, alphaMult: Float) {

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        startStencil(element)

        for (particle in particles) {

            halo!!.alphaMult = 0 + (particle.alpha * particle.level )
            halo!!.color = particle.color
            halo!!.setSize(particle.size / 2, particle.size / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(particle.location.x, particle.location.y)
        }

        endStencil()
    }

    fun renderForeground(element: LunaElement, alphaMult: Float) {

        startStencil(element)

        foreground.color = Color(50, 0, 0)
        foreground.alphaMult = 0.2f
        foreground.setSize(tooltip.widthSoFar + 40, tooltip.heightSoFar + 40)
        foreground.render(element.x - 20, element.y - tooltip.heightSoFar + 20)

        endStencil()

    }

    fun startStencil(element: LunaElement) {

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


        GL11.glRectf(element.x - 10, element.y + initialHeight + 6, element.x + tooltip.widthSoFar + 10, element.y - tooltip.heightSoFar + initialHeight - 6)


        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

}