package assortment_of_things.combat

import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.FlickerUtilV2
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class VFXRenderer : BaseCombatLayeredRenderingPlugin() {

    companion object {

        fun addExplosion(duration: Float, loc: Vector2f, color: Color, secondaryColor: Color, size: Float, angle: Float, flickerStrength: Float) {
            var renderer = get()

            renderer.explosions.add(RATExplosion(duration, loc, color, secondaryColor, size, angle, flickerStrength))
        }

        fun addLensflare(duration: Float, loc: Vector2f, color: Color, secondaryColor: Color, size: Float, angle: Float) {
            var renderer = get()

            renderer.lensflares.add(RATLensflare(duration, loc, color, secondaryColor, size, angle))
        }

        private fun get() : VFXRenderer {
            var instance =  Global.getCombatEngine()?.customData?.get("rat_vfx_renderer") as VFXRenderer?
            if (instance == null) {
                instance = VFXRenderer()
                Global.getCombatEngine()?.customData?.set("rat_vfx_renderer", instance)
                Global.getCombatEngine().addLayeredRenderingPlugin(instance)
            }
            return instance
        }
    }

    data class RATLensflare(var duration: Float, var loc: Vector2f, var color: Color, val secondaryColor: Color, var size: Float, var angle: Float) {
        var maxDuration = duration;
        var flicker = FlickerUtilV2(0.2f)
        var rot1 = MathUtils.getRandomNumberInRange(0f, 360f)
        var rot2 = MathUtils.getRandomNumberInRange(0f, 360f)
        var rot3 = MathUtils.getRandomNumberInRange(0f, 360f)
    }
    data class RATExplosion(var duration: Float, var loc: Vector2f, var color: Color, val secondaryColor: Color, var size: Float, var angle: Float, var flickerStrength: Float) {

        var inDuration = 0.1f
        var maxInDuration = inDuration

        var maxDuration = duration;
        var flicker = FlickerUtilV2(0.2f)
        var rotOuter1 = MathUtils.getRandomNumberInRange(0f, 360f)
        var rotOuter2 = MathUtils.getRandomNumberInRange(0f, 360f)
        var rotInner = MathUtils.getRandomNumberInRange(0f, 360f)
        var rotCenter = MathUtils.getRandomNumberInRange(0f, 360f)
    }

    var explosions = ArrayList<RATExplosion>()
    var explosionCore = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")

    var lensflares = ArrayList<RATLensflare>()
    var flareCore = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")

    override fun init(entity: CombatEntityAPI?) {
        super.init(entity)
        Global.getCombatEngine()?.customData?.set("rat_vfx_renderer", this)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES)
    }

    override fun advance(amount: Float) {
        for (explosion in ArrayList(explosions)) {




            explosion.rotOuter1 += 7 * amount
            explosion.rotOuter2 -= 4 * amount
            explosion.rotInner += 2 * amount
            explosion.rotCenter += 0.3f * amount

            if (!Global.getCombatEngine().isPaused) {
                explosion.flicker.advance(amount)
            }

            if (explosion.inDuration > 0) {
                explosion.inDuration -= 1 * amount
            } else {
                explosion.duration -= 1 * amount;
            }

            if (explosion.duration < 0) {
                explosions.remove(explosion)
            }
        }

        for (flare in ArrayList(lensflares)) {
            if (!Global.getCombatEngine().isPaused) {
                flare.flicker.advance(amount)
            }
            flare.duration -= 1 * amount;
            if (flare.duration < 0) {
                lensflares.remove(flare)
            }
        }
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        for (explosion in explosions) {



            var level = explosion.duration.levelBetween(0f, explosion.maxDuration * 0.8f)

            if (explosion.inDuration > 0) {
                level = explosion.inDuration.levelBetween(explosion.maxInDuration, 0f)
            }

            var brightness = 1f + explosion.flicker.brightness * (0.1f * explosion.flickerStrength) * level * level

            var ease = easeInOutSine(level)

            var width = explosion.size
            var height = explosion.size

            explosionCore.setAdditiveBlend()

            explosionCore.setSize(width * 2f * brightness, height * 2f * brightness)
            explosionCore.color = explosion.secondaryColor
            explosionCore.alphaMult = ease * 0.1f
            explosionCore.angle = explosion.rotOuter1
            explosionCore.renderAtCenter(explosion.loc.x, explosion.loc.y)

            explosionCore.setSize(width * 1.4f * brightness, height * 1.4f * brightness)
            explosionCore.color = explosion.secondaryColor
            explosionCore.alphaMult = ease * 0.2f
            explosionCore.angle = explosion.rotOuter1
            explosionCore.renderAtCenter(explosion.loc.x, explosion.loc.y)

            explosionCore.setSize(width * 1.2f * brightness, height * 1.2f * brightness)
            explosionCore.color = Misc.interpolateColor(explosion.color, explosion.secondaryColor, 0.5f)
            explosionCore.alphaMult = ease * 0.3f
            explosionCore.angle = explosion.rotOuter2
            explosionCore.renderAtCenter(explosion.loc.x, explosion.loc.y)

            explosionCore.setSize(width, height)
            explosionCore.color = explosion.color
            explosionCore.alphaMult = ease * 0.8f
            explosionCore.angle = explosion.rotInner
            explosionCore.renderAtCenter(explosion.loc.x, explosion.loc.y)

            explosionCore.setSize(width * 0.5f, height * 0.5f)
            explosionCore.color = Color(255, 255, 255)
            explosionCore.alphaMult = ease
            explosionCore.angle = explosion.rotCenter
            explosionCore.renderAtCenter(explosion.loc.x, explosion.loc.y)

        }



        for (flare in lensflares) {

            var level = flare.duration.levelBetween(0f, flare.maxDuration * 0.8f)

            var brightness = 1f + flare.flicker.brightness * 0.1f * level * level

            var ease = easeInOutSine(level)

            var coreWidth = flare.size * 0.5f
            var coreHeight = flare.size * 0.5f

            flareCore.setAdditiveBlend()

            flareCore.setSize(coreWidth * 1.4f * brightness, coreHeight * 1.4f * brightness)
            flareCore.color = flare.secondaryColor
            flareCore.alphaMult = ease * 0.2f
            flareCore.angle = flare.rot1
            flareCore.renderAtCenter(flare.loc.x, flare.loc.y)

            flareCore.setSize(coreWidth * 1.2f * brightness, coreHeight * 1.2f * brightness)
            flareCore.color = flare.secondaryColor
            flareCore.alphaMult = ease * 0.3f
            flareCore.angle = flare.rot1
            flareCore.renderAtCenter(flare.loc.x, flare.loc.y)

            flareCore.setSize(coreWidth, coreHeight)
            flareCore.color = flare.color
            flareCore.alphaMult = ease * 0.8f
            flareCore.angle = flare.rot2
            flareCore.renderAtCenter(flare.loc.x, flare.loc.y)

            flareCore.setSize(coreWidth * 0.5f, coreHeight * 0.5f)
            flareCore.color = Color(255, 255, 255)
            flareCore.alphaMult = ease
            flareCore.angle = flare.rot3
            flareCore.renderAtCenter(flare.loc.x, flare.loc.y)

        }

    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }


}