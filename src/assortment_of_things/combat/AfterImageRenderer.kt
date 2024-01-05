package assortment_of_things.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.interpolateColor
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//Class by Niatahl from Tahlan-Shipworks, repurposed with their permission.
//Source: https://github.com/niatahl/tahlan-shipworks/blob/master/jars/src/org/niatahl/tahlan/plugins/CustomRender.kt
class AfterImageRenderer : BaseEveryFrameCombatPlugin() {

    override fun init(engine: CombatEngineAPI) {
        val layerRenderer: CombatLayeredRenderingPlugin = CustomRenderer(this)
        engine.addLayeredRenderingPlugin(layerRenderer)
    }

    // Ticking our lifetimes and removing expired
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine()
        if (engine.isPaused) return

        // tick and clean up afterimage list
        val afterimageToRemove = ArrayList<Long>()
        afterimageData.forEach { (_, afterimage) ->
            afterimage.lifetime += engine.elapsedInLastFrame
            if (afterimage.lifetime > afterimage.duration)
                afterimageToRemove.add(afterimage.id)
        }
        afterimageToRemove.forEach { afterimageData.remove(it) }
    }

    fun render(layer: CombatEngineLayers, view: ViewportAPI) {

        // afterimages
        if (layer == CombatEngineLayers.BELOW_SHIPS_LAYER) afterimageData.forEach { (_, afterimage) ->
            renderAfterimage(afterimage, view)
        }
    }

    private fun renderAfterimage(afterimage: Afterimage, view: ViewportAPI) {
        if (!view.isNearViewport(afterimage.location, view.visibleWidth)) return

        // Sprite offset fuckery - Don't you love trigonometry?
        val sprite = afterimage.sprite

        sprite.angle = afterimage.facing - 90f
        sprite.color = afterimage.colorIn.interpolateColor(afterimage.colorOut, afterimage.lifetime / afterimage.duration)
        sprite.alphaMult = 1 - afterimage.lifetime / afterimage.duration
        sprite.setAdditiveBlend()
        if (!afterimage.additive) {
            sprite.setNormalBlend()
        }

        if (!Global.getCombatEngine().isPaused && afterimage.jitter > 0f) {
            afterimage.actualLoc = MathUtils.getRandomPointInCircle(afterimage.location, afterimage.jitter)
        }

        sprite.renderAtCenter(afterimage.actualLoc.x, afterimage.actualLoc.y)
    }

    data class ActiveAfterImage(var duration: Float, var interval: IntervalUtil, var data: Afterimage)

    data class Afterimage(
        val id: Long,
        val sprite: SpriteAPI,
        val location: Vector2f,
        val facing: Float,
        val colorIn: Color,
        val colorOut: Color,
        val duration: Float,
        val jitter: Float,
        val additive: Boolean = true
    ) {
        var lifetime = 0f
        var actualLoc = location
    }

    companion object {
        private val afterimageData = HashMap<Long, Afterimage>()


        fun addAfterimage(ship: ShipAPI,
                          colorIn: Color,
                          colorOut: Color = colorIn,
                          duration: Float,
                          jitter: Float = 0f,
                          location: Vector2f = Vector2f(ship.location), additive: Boolean = true) = Afterimage(id = Misc.random.nextLong(),
            sprite = Global.getSettings().getSprite(ship.hullSpec.spriteName),
            location = location,
            facing = ship.facing,
            colorIn = colorIn,
            colorOut = colorOut,
            duration = duration,
            jitter = jitter,
            additive = additive).also { afterimage ->

                val sprite = ship.spriteAPI
                val offsetX = sprite.width / 2 - sprite.centerX
                val offsetY = sprite.height / 2 - sprite.centerY
                val trueOffsetX = FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble()))
                    .toFloat() * offsetX - FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble()))
                    .toFloat() * offsetY
                val trueOffsetY = FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble()))
                    .toFloat() * offsetX + FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble()))
                    .toFloat() * offsetY

                afterimage.location.x += trueOffsetX
                afterimage.location.y += trueOffsetY
                afterimage.actualLoc = afterimage.location

                afterimageData[afterimage.id] = afterimage
            }

        fun addAfterimageWithSpritepath(ship: ShipAPI,
                          colorIn: Color,
                          colorOut: Color = colorIn,
                          duration: Float,
                          jitter: Float = 0f,
                          location: Vector2f = Vector2f(ship.location),
                          spritePath: String) = Afterimage(id = Misc.random.nextLong(),
            sprite = Global.getSettings().getSprite(spritePath),
            location = location,
            facing = ship.facing,
            colorIn = colorIn,
            colorOut = colorOut,
            duration = duration,
            jitter = jitter).also { afterimage ->

            val sprite = ship.spriteAPI
            val offsetX = sprite.width / 2 - sprite.centerX
            val offsetY = sprite.height / 2 - sprite.centerY
            val trueOffsetX = FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble()))
                .toFloat() * offsetX - FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble()))
                .toFloat() * offsetY
            val trueOffsetY = FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble()))
                .toFloat() * offsetX + FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble()))
                .toFloat() * offsetY

            afterimage.location.x += trueOffsetX
            afterimage.location.y += trueOffsetY
            afterimage.actualLoc = afterimage.location

            afterimageData[afterimage.id] = afterimage
        }
    }

    internal class CustomRenderer
        (private val parentPlugin: AfterImageRenderer) : BaseCombatLayeredRenderingPlugin() {
        override fun render(layer: CombatEngineLayers, view: ViewportAPI) {
            parentPlugin.render(layer, view)
        }

        override fun getRenderRadius(): Float {
            return 9.9999999E14f
        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.allOf(CombatEngineLayers::class.java)
        }
    }
}