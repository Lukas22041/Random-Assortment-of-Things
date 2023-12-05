package assortment_of_things.frontiers.ui

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaUI.elements.LunaElement
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class SiteSelectionPickerElement(tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var sprite = Global.getSettings().getAndLoadSprite("graphics/ui/poly.png")

    init {
        enableTransparency = true
        renderBackground = false
    }

    //https://stackoverflow.com/questions/20734438/algorithm-to-generate-a-hexagonal-grid-with-coordinate-system
    private fun drawHexGridLoop(origin: Vector2f, size: Int, radius: Int, padding: Int) {
        val ang30 = Math.toRadians(30.0)
        val xOff = Math.cos(ang30) * (radius + padding)
        val yOff = Math.sin(ang30) * (radius + padding)
        val half = size / 2
        for (row in 0 until size) {
            val cols = size - Math.abs(row - half)
            for (col in 0 until cols) {
                val xLbl = if (row < half) col - row else col - half
                val yLbl = row - half
                val x = (origin.x + xOff * (col * 2 + 1 - cols)) as Double
                val y = (origin.y + yOff * (row - half) * 3) as Double

                sprite.setSize(radius.toFloat() * 2, radius.toFloat() * 2.2f)
                sprite.color = Color(200, 50, 50)
                sprite.renderAtCenter(x.toFloat(), y.toFloat())

                //drawHex(g, xLbl, yLbl, x, y, radius)
            }
        }
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        drawHexGridLoop(Vector2f(x + width / 2, y + height / 2), 5, 48, 10)
    }

}