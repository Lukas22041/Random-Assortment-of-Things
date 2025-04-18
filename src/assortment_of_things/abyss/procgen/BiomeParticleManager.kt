package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.IntervalUtil
import lunalib.lunaUtil.campaign.LunaCampaignRenderer
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

//Not Transient since particles should stay in the same location
class BiomeParticleManager(var manager: AbyssBiomeManager) : LunaCampaignRenderingPlugin {

    init {
        LunaCampaignRenderer.addRenderer(this)
    }

    class AbyssalLightParticle(var biome: BaseAbyssBiome,
        var fadeIn: Float, var duration: Float, var fadeOut: Float,
        var color: Color, var alpha: Float, var size: Float, var location: Vector2f, var velocity: Vector2f,
        var adjustInterval: IntervalUtil, var startAdjustment: Float, var adjustMin: Float, var adjustMax: Float) {

        enum class ParticleState {
            FadeIn, Mid, FadeOut
        }

        var state = ParticleState.FadeIn

        var level = 0f

        var maxFadeIn = fadeIn
        var maxDuration = duration
        var maxFadeOut = fadeOut

        var adjustment = startAdjustment
    }

    var particles = ArrayList<AbyssalLightParticle>()

    var particleInterval = IntervalUtil(0.2f, 0.2f)

    @Transient
    var halo: SpriteAPI? = null


    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

        if (!AbyssUtils.isPlayerInAbyss()) {
            particles.clear()
            return
        }

        //Manage Particles
        for (particle in ArrayList(particles)) {

            //Make particles die faster in other biomes
            var lifeTimeMult = 1f
            var cell = manager.getCell(particle.location.x, particle.location.y)
            if (cell.getBiome() != particle.biome) lifeTimeMult = 2.5f

            if (particle.state == AbyssalLightParticle.ParticleState.FadeIn) {
                particle.fadeIn -= 1 * amount

                var level = (particle.fadeIn - 0f) / (particle.maxFadeIn - 0f)
                particle.level = 1 - level

                if (particle.fadeIn < 0) {
                    particle.state = AbyssalLightParticle.ParticleState.Mid
                }
            }

            if (particle.state == AbyssalLightParticle.ParticleState.Mid) {
                particle.duration -= 1 * amount * lifeTimeMult


                particle.level = 1f

                if (particle.duration < 0) {
                    particle.state = AbyssalLightParticle.ParticleState.FadeOut
                }
            }

            if (particle.state == AbyssalLightParticle.ParticleState.FadeOut) {
                particle.fadeOut -= 1 * amount * lifeTimeMult

                particle.level = (particle.fadeOut - 0f) / (particle.maxFadeOut - 0f)

                if (particle.fadeOut < 0) {
                    particles.remove(particle)
                    continue
                }
            }

            particle.adjustInterval.advance(amount)
            if (particle.adjustInterval.intervalElapsed()) {
                var velocity = Vector2f(0f, 0f)
                particle.adjustment = MathUtils.getRandomNumberInRange(particle.adjustMin, particle.adjustMax)
            }

            particle.velocity = particle.velocity.rotate(particle.adjustment * amount)


            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)

        }

        //Spawn Particles
        particleInterval.advance(amount)
        if (particleInterval.intervalElapsed()) {
            var playerCell = manager.getPlayerCell()
            var surrounding = playerCell.getSurroundingWithCenter()

            for (cell in surrounding) {
                var biome = cell.getBiome()
                biome?.spawnParticlesForCell(this, cell)
            }
        }


    }


    var layers = EnumSet.of(CampaignEngineLayers.ABOVE)
    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return layers
    }


    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        for (particle in particles) {

            if (viewport!!.isNearViewport(particle.location, particle.size * 2)) {
                halo!!.alphaMult = 0 + (particle.alpha * particle.level)
                halo!!.color = particle.color
                halo!!.setSize(particle.size / 2, particle.size / 2)
                halo!!.setAdditiveBlend()
                halo!!.renderAtCenter(particle.location.x, particle.location.y)
            }
        }
    }
}