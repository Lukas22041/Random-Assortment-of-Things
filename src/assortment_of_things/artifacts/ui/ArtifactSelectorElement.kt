package assortment_of_things.artifacts.ui

import assortment_of_things.artifacts.ArtifactSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement
import org.magiclib.kotlin.setBrightness
import java.awt.Color

class ArtifactSelectorElement(var artifact: ArtifactSpec, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var selectedArtifact = false
    init {
        enableTransparency = true
        renderBorder = false
        renderBackground = false
    }

    var sprite = Global.getSettings().getSprite(artifact.spritePath)

    override fun render(alphaMult: Float) {

        var color = Color(255, 255, 255)

        if (!isHovering) {
            color = color.setBrightness(color.red-20)
        }

        if (!selectedArtifact) {
            color = color.setBrightness(color.red-50)
        }

        sprite.color = color
        sprite.alphaMult = alphaMult
        sprite.setNormalBlend()
        sprite.setSize(width, height)
        sprite.render(x, y)

        if (selectedArtifact) {
            sprite.color = color
            sprite.alphaMult = alphaMult * 0.2f
            sprite.setAdditiveBlend()
            sprite.setSize(width, height)
            sprite.render(x, y)
        }
    }

}