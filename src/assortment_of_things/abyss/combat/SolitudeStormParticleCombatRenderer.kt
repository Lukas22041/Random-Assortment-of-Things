package assortment_of_things.abyss.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.BiomeParticleManager
import assortment_of_things.abyss.procgen.biomes.SeaOfSolitude
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class SolitudeStormParticleCombatRenderer(var color: Color, var darkColor: Color) : BaseCombatLayeredRenderingPlugin() {

    var particles = ArrayList<BiomeParticleManager.AbyssalLightParticle>()

    var particleInterval = IntervalUtil(0.2f, 0.2f)
    var halo = Global.getSettings().getSprite("rat_terrain", "halo")

    var vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette_reversed.png")

    var minAlpha = 0.25f
    var maxAlpha = 0.45f

    override fun advance(amount: Float) {
        super.advance(amount)

        handleParticles(amount)

        var viewport = Global.getCombatEngine().viewport

        particleInterval.advance(amount)
        if (particleInterval.intervalElapsed()) {


            var count = 40
            var fadeInOverwrite = false

            if (particles.size <= 50) {
                count = 500
                fadeInOverwrite = true
            }


            for (i in 0..count) {

                var velocity = Vector2f(0f, 0f)
                velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(200f, 550f), MathUtils.getRandomNumberInRange(180f, 210f)))

                var spawnLocation = Vector2f(viewport.llx + viewport.visibleWidth, viewport.lly + viewport.visibleHeight / 2)

                //var spawnLocation = MathUtils.getPointOnCircumference(Vector2f(), 45f, entity.facing + 180)

                var randomX = MathUtils.getRandomNumberInRange(-5000f, 5000f)
                var randomY = MathUtils.getRandomNumberInRange(-5000f, 5000f)

                spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

                var fadeIn = MathUtils.getRandomNumberInRange(1f, 1.5f)
                if (fadeInOverwrite) fadeIn = 0.05f
                var duration = MathUtils.getRandomNumberInRange(2f, 4f)
                var fadeOut = MathUtils.getRandomNumberInRange(2f, 2.5f)

                var size = MathUtils.getRandomNumberInRange(20f, 40f)

                var alpha = MathUtils.getRandomNumberInRange(minAlpha, maxAlpha)

                particles.add(BiomeParticleManager.AbyssalLightParticle(
                    null,
                    fadeIn,
                    duration,
                    fadeOut,
                    color,
                    alpha,
                    size,
                    spawnLocation,
                    velocity,
                    /* Next few variables are unused */ IntervalUtil(0f, 0f), 0f, 0f, 0f))
            }
        }
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS, CombatEngineLayers.JUST_BELOW_WIDGETS)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    fun handleParticles(amount: Float) {
        for (particle in ArrayList(particles)) {

            if (particle.state == BiomeParticleManager.AbyssalLightParticle.ParticleState.FadeIn) {
                particle.fadeIn -= 1 * amount

                var level = (particle.fadeIn - 0f) / (particle.maxFadeIn - 0f)
                particle.level = 1 - level

                if (particle.fadeIn < 0) {
                    particle.state = BiomeParticleManager.AbyssalLightParticle.ParticleState.Mid
                }
            }

            if (particle.state == BiomeParticleManager.AbyssalLightParticle.ParticleState.Mid) {
                particle.duration -= 1 * amount


                particle.level = 1f

                if (particle.duration < 0) {
                    particle.state = BiomeParticleManager.AbyssalLightParticle.ParticleState.FadeOut
                }
            }

            if (particle.state == BiomeParticleManager.AbyssalLightParticle.ParticleState.FadeOut) {
                particle.fadeOut -= 1 * amount

                particle.level = (particle.fadeOut - 0f) / (particle.maxFadeOut - 0f)

                if (particle.fadeOut < 0) {
                    particles.remove(particle)
                    continue
                }
            }

            particle.adjustInterval.advance(amount)
            if (particle.adjustInterval.intervalElapsed()) {
                var velocity = Vector2f(0f, 0f)
                particle.adjustment = MathUtils.getRandomNumberInRange(-1f, 1f)
            }

            particle.velocity = particle.velocity.rotate(particle.adjustment * amount)


            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)
        }
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        if (layer == CombatEngineLayers.BELOW_PLANETS) {
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

        if (layer == CombatEngineLayers.JUST_BELOW_WIDGETS) {
            vignette.color = darkColor.darker()
            vignette.alphaMult = 0.25f

            var offset = 300
            vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
            vignette.render(viewport!!.llx - (offset * 0.5f), viewport!!.lly - (offset * 0.5f))
        }

    }

}