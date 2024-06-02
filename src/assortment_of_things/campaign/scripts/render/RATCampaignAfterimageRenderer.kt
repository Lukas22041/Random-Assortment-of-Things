package assortment_of_things.campaign.scripts.render

import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUtil.campaign.LunaCampaignRenderer
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.interpolateColor
import java.awt.Color
import java.util.HashMap

class RATCampaignAfterimageRenderer {

    data class CampaignAfterimage(
        val layer: CampaignEngineLayers,
        val containinglocation: LocationAPI,
        val id: Long,
        var sprite: SpriteAPI,
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


    fun addAfterimage(layer: CampaignEngineLayers, containingLocation: LocationAPI, entity: SectorEntityToken,
                      colorIn: Color,
                      colorOut: Color = colorIn,
                      duration: Float,
                      jitter: Float = 0f,
                      location: Vector2f = Vector2f(entity.location), additive: Boolean = true, scale: Float = 1f) = CampaignAfterimage(
        id = Misc.random.nextLong(),
        sprite = Global.getSettings().getSprite(entity.customEntitySpec.spriteName),
        location = location,
        facing = entity.facing,
        colorIn = colorIn,
        colorOut = colorOut,
        duration = duration,
        jitter = jitter,
        additive = additive,
        containinglocation = containingLocation,
        layer = layer).also { afterimage ->

        //val sprite = ship.spriteAPI
        val sprite = Global.getSettings().getSprite(entity.customEntitySpec.spriteName)
        sprite.setSize(entity.customEntitySpec.spriteWidth * scale, entity.customEntitySpec.spriteHeight * scale)
        afterimage.sprite = sprite

      /*  val offsetX = sprite.width / 2 - sprite.centerX
        val offsetY = sprite.height / 2 - sprite.centerY*/
        /* val trueOffsetX = FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble()))
             .toFloat() * offsetX - FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble()))
             .toFloat() * offsetY
         val trueOffsetY = FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble()))
             .toFloat() * offsetX + FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble()))
             .toFloat() * offsetY

         afterimage.location.x += trueOffsetX
         afterimage.location.y += trueOffsetY*/

        /*afterimage.location.x += entity.location.x
        afterimage.location.y += entity.location.y*/

      //  var location = MathUtils.getPointOnCircumference(entity.location, -10f * scale, entity.facing)

        afterimage.actualLoc = location

        afterimageData[afterimage.id] = afterimage
    }

    val afterimageData = HashMap<Long, CampaignAfterimage>()


    fun advanceAfterimages(amount: Float) {
        if (Global.getSector().isPaused) return

        // tick and clean up afterimage list
        val afterimageToRemove = ArrayList<Long>()
        afterimageData.forEach { (_, afterimage) ->
            afterimage.lifetime += amount
            if (afterimage.lifetime > afterimage.duration)
                afterimageToRemove.add(afterimage.id)
        }
        afterimageToRemove.forEach { afterimageData.remove(it) }
    }

    fun renderAfterimages(layer: CampaignEngineLayers, view: ViewportAPI) {
        for (afterimage in afterimageData) {
            if (Global.getSector().playerFleet.containingLocation != afterimage.value.containinglocation) return
            if (afterimage.value.layer == layer) {
                renderAfterimage(afterimage.value, view)
            }
        }
    }

    private fun renderAfterimage(afterimage: CampaignAfterimage, view: ViewportAPI) {
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

        if (!Global.getSector().isPaused && afterimage.jitter > 0f) {
            afterimage.actualLoc = MathUtils.getRandomPointInCircle(afterimage.location, afterimage.jitter)
        }

        sprite.renderAtCenter(afterimage.actualLoc.x, afterimage.actualLoc.y)
    }

}