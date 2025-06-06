package assortment_of_things.abyss.boss

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class GenesisParticleManager() : BaseCustomEntityPlugin() {

    class GenesisParticle(var fadeIn: Float, var duration: Float, var fadeOut: Float, var color: Color, var alpha: Float, var size: Float, var location: Vector2f, var velocity: Vector2f) {

        enum class ParticleState {
            FadeIn, Mid, FadeOut
        }

        var state = ParticleState.FadeIn

        var level = 0f

        var maxFadeIn = fadeIn
        var maxDuration = duration
        var maxFadeOut = fadeOut

        var adjustment = MathUtils.getRandomNumberInRange(-1f, 1f)
        var adjustmentInterval = IntervalUtil(0.5f, 0.75f)

    }

    var particles = ArrayList<GenesisParticle>()

    var particleInterval = IntervalUtil(0.2f, 0.2f)

    @Transient
    var halo: SpriteAPI? = null

    var color = AbyssUtils.ABYSS_COLOR

    override fun advance(amount: Float) {

        if (Global.getSector().playerFleet.starSystem != entity.starSystem) {
            particles.clear()
            return
        }

        //color = AbyssUtils.getSystemData(entity.starSystem).getColor()

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        for (particle in ArrayList(particles)) {

            if (particle.state == GenesisParticle.ParticleState.FadeIn) {
                particle.fadeIn -= 1 * amount

                var level = (particle.fadeIn - 0f) / (particle.maxFadeIn - 0f)
                particle.level = 1 - level

                if (particle.fadeIn < 0) {
                    particle.state = GenesisParticle.ParticleState.Mid
                }
            }

            if (particle.state == GenesisParticle.ParticleState.Mid) {
                particle.duration -= 1 * amount


                particle.level = 1f

                if (particle.duration < 0) {
                    particle.state = GenesisParticle.ParticleState.FadeOut
                }
            }

            if (particle.state == GenesisParticle.ParticleState.FadeOut) {
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
                particle.adjustment = MathUtils.getRandomNumberInRange(-1f, 1f)
            }

            particle.velocity = particle.velocity.rotate(particle.adjustment * amount)


            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)

        }



        particleInterval.advance(amount)
        if (particleInterval.intervalElapsed()) {



            var amount = 20
            var fadeInOverwrite = false

            if (particles.size <= 50) {
                amount = 250
                fadeInOverwrite = true
            }


            for (i in 0..amount) {

                var velocity = Vector2f(0f, 0f)
                velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(200f, 550f), MathUtils.getRandomNumberInRange(180f, 210f)))

                var spawnLocation = Vector2f(Global.getSector().playerFleet.location)
                //var spawnLocation = MathUtils.getPointOnCircumference(Vector2f(), 45f, entity.facing + 180)

                var randomX = MathUtils.getRandomNumberInRange(-3000f, 3000f)
                var randomY = MathUtils.getRandomNumberInRange(-3000f, 3000f)

                spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

                var fadeIn = MathUtils.getRandomNumberInRange(1f, 1.5f)
                if (fadeInOverwrite) fadeIn = 0.05f
                var duration = MathUtils.getRandomNumberInRange(2f, 4f)
                var fadeOut = MathUtils.getRandomNumberInRange(1f, 2.5f)

                var size = MathUtils.getRandomNumberInRange(25f, 50f)

                var alpha = MathUtils.getRandomNumberInRange(0.25f, 0.45f)

                particles.add(GenesisParticle(fadeIn, duration, fadeOut, color, alpha, size, spawnLocation, velocity))
            }
        }

    }

    override fun getRenderRange(): Float {
        return 1000000f
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        for (particle in particles) {

            if (viewport!!.isNearViewport(particle.location, particle.size * 2)) {
                halo!!.alphaMult = 0 + (particle.alpha * particle.level )
                halo!!.color = particle.color
                halo!!.setSize(particle.size / 2, particle.size / 2)
                halo!!.setAdditiveBlend()
                halo!!.renderAtCenter(particle.location.x, particle.location.y)
            }
        }
    }
}