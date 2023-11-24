package assortment_of_things.exotech.entities

import assortment_of_things.exotech.ExoShipData
import assortment_of_things.exotech.ExoUtils
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
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ExoshipEntity : BaseCustomEntityPlugin() {

    class ExoshipThrusterParticle(var duration: Float, var color: Color, var size: Float, var location: Vector2f, var velocity: Vector2f) {
        var maxDuration = duration
    }

    @Transient
    var particleSprite: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/explosion3.png")

    @Transient
    var glow: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_ext_lights.png")
    @Transient
    var lights: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_lights.png")



    var particles = ArrayList<ExoshipThrusterParticle>()
    var particleInterval = IntervalUtil(0.015f, 0.015f)


    var thrusterLevel = 0f

    override fun getRenderRange(): Float {
        return 100000000f
    }

    override fun advance(amount: Float) {

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

        var data = ExoUtils.getExoshipData(entity)
        var state = data.state

        if (state == ExoShipData.State.Idle) {
           thrusterLevel = 0f
        }

        if (state == ExoShipData.State.Travelling) {
            thrusterLevel += 0.5f * amount
        }

        if (state == ExoShipData.State.Arriving) {
            thrusterLevel -= 0.3f * amount
        }

        thrusterLevel = MathUtils.clamp(thrusterLevel, 0f, 1f)

        if (state != ExoShipData.State.Idle && Global.getSector().playerFleet.containingLocation == entity.containingLocation) {
            particleInterval.advance(amount)
            if (particleInterval.intervalElapsed()) {
                var spawnLocation = MathUtils.getPointOnCircumference(Vector2f(), 50f, entity.facing + 180)

                var locLeft = spawnLocation.plus(Vector2f(10f, 20f).rotate(entity.facing))
                var locRight = spawnLocation.plus(Vector2f(10f, -20f).rotate(entity.facing))

                //Main Engine
                addThrusterParticles(spawnLocation, 0.3f, 20, 2f, 6f, 10f)

                addThrusterParticles(locLeft, 0.2f, 15, 2f, 3f, 8f)
                addThrusterParticles(locRight, 0.2f, 15, 2f, 3f, 8f)

            }
        }

    }

    fun addThrusterParticles(spawnLocation: Vector2f, duration: Float, amount: Int, width: Float, sizeMin: Float, sizeMax: Float) {
        var velocity = Vector2f(0f, 0f)

        velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), 200f * thrusterLevel, entity.facing + 180))

        for (i in 0..amount) {


            var randomX = MathUtils.getRandomNumberInRange(-width, width)
            var randomY = MathUtils.getRandomNumberInRange(-width, width)
            particles.add(ExoshipThrusterParticle(duration * thrusterLevel, Color(255, 155, 0), MathUtils.getRandomNumberInRange(sizeMin * thrusterLevel, sizeMax * thrusterLevel), Vector2f(spawnLocation.x + randomX, spawnLocation.y + randomY), velocity))
        }
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (particleSprite == null) {
            particleSprite = Global.getSettings().getAndLoadSprite("graphics/fx/explosion3.png")
            glow = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_ext_lights.png")
            lights = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_lights.png")
        }

        particleSprite!!.setAdditiveBlend()

        glow!!.alphaMult = 1f
        glow!!.angle = entity.facing - 90
        glow!!.setSize(95f, 140f)
        glow!!.renderAtCenter(entity.location.x, entity.location.y)

        lights!!.alphaMult = 1f
        lights!!.angle = entity.facing - 90
        lights!!.setSize(95f, 140f)
        lights!!.renderAtCenter(entity.location.x, entity.location.y)

        for (particle in particles) {
            var level = (particle.duration - 0f) / (particle.maxDuration - 0f)
            particleSprite!!.alphaMult = 0 + (0.1f * level )
            var location = particle.location.plus(entity.location)

            particleSprite!!.color = particle.color


            particleSprite!!.setSize(particle.size, particle.size)

            particleSprite!!.renderAtCenter(location.x, location.y)
        }

    }



}