package assortment_of_things.abyss.ui

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbyssalMapScreen(tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var scale = 0.011f
    var playerPoint = Global.getSettings().getAndLoadSprite("graphics/starscape/star4.png")
    var nebulaSprite: SpriteAPI = Global.getSettings().getAndLoadSprite("graphics/terrain/rat_map_nebula.png")

    init {
        enableTransparency = true
        borderAlpha = 0.4f
        backgroundAlpha = 1f
        borderColor = AbyssUtils.ABYSS_COLOR
        backgroundColor = Color(20, 0, 0)

        onClick {
            var loc = toRealPosition(Vector2f(it.x.toFloat(), it.y.toFloat()))
            Global.getSector().playerFleet.location.set(loc)
        }
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        var manager = AbyssUtils.getBiomeManager()
        var grid = manager.grid
        var playerCell = manager.getPlayerCell()


        GL11.glPopMatrix()

        for (column in grid) {
            for (cell in column) {
                var color = backgroundColor



                var horOffset = (manager.mapHorizontalSize / 2) * scale
                var verOffset = (manager.mapVerticalSize / 2) * scale

                var cellSize = cell.size * scale

                var cellX = x + (width / 2) + cell.x * cellSize - horOffset
                var cellY = y + (height / 2) + cell.y * cellSize - verOffset

                nebulaSprite.setSize(cellSize * 4f, cellSize * 4f)
                nebulaSprite.color = AbyssUtils.ABYSS_COLOR
                nebulaSprite.alphaMult = cell.spriteAlpha * 0.05f * alphaMult
                nebulaSprite.angle = cell.spriteAngle
                nebulaSprite.renderAtCenter(cellX + cellSize/2, cellY + cellSize / 2)


                /*if (cell == playerCell) {
                    color = Misc.getHighlightColor()
                }

                //Fill
                GL11.glPushMatrix()
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_CULL_FACE)

                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


                GL11.glColor4f(color.red / 255f,
                    color.green / 255f,
                    color.blue / 255f,
                    color.alpha / 255f * (alphaMult * 0.2f))



                GL11.glRectf(cellX, cellY, cellX + cellSize, cellY + cellSize)

                GL11.glPopMatrix()



                //Lines

                var c = borderColor

                if (cell == playerCell) {
                    color = Misc.getHighlightColor()
                }

                GL11.glPushMatrix()

                GL11.glTranslatef(0f, 0f, 0f)
                GL11.glRotatef(0f, 0f, 0f, 1f)

                GL11.glDisable(GL11.GL_TEXTURE_2D)


                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


                GL11.glColor4f(c.red / 255f,
                    c.green / 255f,
                    c.blue / 255f,
                    c.alpha / 255f * (alphaMult * 0.2f))

                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glBegin(GL11.GL_LINE_STRIP)

                GL11.glVertex2f(cellX, cellY)
                GL11.glVertex2f(cellX, cellY + cellSize)
                GL11.glVertex2f(cellX + cellSize, cellY + cellSize)
                GL11.glVertex2f(cellX + cellSize, cellY)
                GL11.glVertex2f(cellX, cellY)

                GL11.glEnd()
                GL11.glPopMatrix()*/

            }
        }

        var player = Global.getSector().playerFleet
        var loc = toMapPosition(player.location)

        playerPoint.alphaMult = alphaMult * 1f
        playerPoint.color = Misc.getBasePlayerColor()

        playerPoint.renderAtCenter(loc.x, loc.y)


    }

    fun toMapPosition(loc: Vector2f) : Vector2f {
        return Vector2f(x + (width / 2) + loc.x * scale, y + (height / 2) + loc.y * scale)
    }

    fun toRealPosition(loc: Vector2f) : Vector2f {
        return Vector2f((loc.x - (width / 2) - x) / scale, (loc.y - (height / 2) - y) / scale)
    }
}