package assortment_of_things.artifacts.ui

import assortment_of_things.artifacts.ArtifactSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement

class ArtifactDisplayElement(var artifact: ArtifactSpec, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    init {
        enableTransparency = true
        renderBorder = false
        renderBackground = false
    }

    var sprite = Global.getSettings().getSprite(artifact.spritePath)

    override fun processInput(events: MutableList<InputEventAPI>?) {
        //super.processInput(events)
    }

    override fun render(alphaMult: Float) {

        sprite.alphaMult = alphaMult
        sprite.setSize(width, height)
        sprite.render(x, y)

    }

}