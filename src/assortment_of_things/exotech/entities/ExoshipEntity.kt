package assortment_of_things.exotech.entities

import assortment_of_things.exotech.interactions.exoship.ExoshipMoveScript
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ExoshipEntity : BaseCustomEntityPlugin() {

    class ExoshipThrusterParticle(var duration: Float, var color: Color, var size: Float, var location: Vector2f, var velocity: Vector2f) {
        var maxDuration = duration
    }

    @Transient
    var particleSprite: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/explosion3.png")

    var particles = ArrayList<ExoshipThrusterParticle>()
    var particleInterval = IntervalUtil(0.05f, 0.05f)


    var thrusterLevel = 0f

    override fun getRenderRange(): Float {
        return 100000000f
    }

    override fun advance(amount: Float) {
        particleInterval.advance(amount)

        for (particle in ArrayList(particles)) {
            particle.duration -= 1 * amount

            if (particle.duration < 0) {
                particles.remove(particle)
                continue
            }


            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)

        }


        var state = ExoshipMoveScript.ExoshipState.Arrived
        if (entity.memoryWithoutUpdate.get("\$exoship_state") != null) {
            state = entity.memoryWithoutUpdate.get("\$exoship_state") as ExoshipMoveScript.ExoshipState
        }

        if (state == ExoshipMoveScript.ExoshipState.Arrived) {
           thrusterLevel = 0f
        }

        if (state == ExoshipMoveScript.ExoshipState.Travelling) {
            thrusterLevel += 0.3f * amount
        }

        if (state == ExoshipMoveScript.ExoshipState.Arriving) {
            thrusterLevel -= 0.3f * amount
        }

        thrusterLevel = MathUtils.clamp(thrusterLevel, 0f, 1f)

        if (state != ExoshipMoveScript.ExoshipState.Arrived) {
            particleInterval.advance(amount)
            if (particleInterval.intervalElapsed()) {
                var velocity = Vector2f(0f, 0f)

                velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), 200f * thrusterLevel, entity.facing + 180))



                for (i in 0..40) {

                    var spawnLocation = MathUtils.getPointOnCircumference(Vector2f(), 45f, entity.facing + 180)

                    var randomX = MathUtils.getRandomNumberInRange(-10f, 10f)
                    var randomY = MathUtils.getRandomNumberInRange(-10f, 10f)
                    particles.add(ExoshipThrusterParticle(1f * thrusterLevel, Color(255, 155, 0), MathUtils.getRandomNumberInRange(5f * thrusterLevel, 25f * thrusterLevel), Vector2f(spawnLocation.x + randomX, spawnLocation.y + randomY), velocity))
                }
            }
        }
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (particleSprite == null) {
            particleSprite = Global.getSettings().getAndLoadSprite("graphics/fx/explosion3.png")
        }

        particleSprite!!.setAdditiveBlend()

        for (particle in particles) {
            var level = (particle.duration - 0f) / (particle.maxDuration - 0f)
            particleSprite!!.alphaMult = 0 + (0.3f * level )
            var location = particle.location.plus(entity.location)

            particleSprite!!.color = particle.color


            particleSprite!!.setSize(particle.size, particle.size)

            particleSprite!!.renderAtCenter(location.x, location.y)
        }

    }



}