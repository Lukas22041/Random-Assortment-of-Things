package assortment_of_things.abyss.ui

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.BiomeCell
import assortment_of_things.combat.ParallaxParticleRenderer
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class AbyssalMapScreen(tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var baseScale = 0.016f / Global.getSettings().screenScaleMult
    var scale = baseScale
    var playerPoint = Global.getSettings().getAndLoadSprite("graphics/starscape/star4.png")
    var nebulaSprite: SpriteAPI = Global.getSettings().getAndLoadSprite("graphics/terrain/rat_map_nebula.png")

    var font: LazyFont? = LazyFont.loadFont(Fonts.INSIGNIA_VERY_LARGE)
    var labelText: LazyFont.DrawableString = font!!.createText("The Abyssal Depths", AbyssUtils.ABYSS_COLOR.setAlpha(255), 80f)

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

    override fun processInput(events: MutableList<InputEventAPI>?) {
        super.processInput(events)

       /* for (event in events!!) {
            if (event.isConsumed) return
            if (event.isMouseScrollEvent) {
                scale += event.eventValue * 0.0001f
                scale = MathUtils.clamp(scale, baseScale, baseScale * 4)
            }
        }*/
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        var manager = AbyssUtils.getBiomeManager()
        var grid = manager.grid


        startStencil()

        for (column in grid) {
            for (cell in column) {


                //Use this if theres performance issues
                //if (cell.x.mod(2) != 0 && cell.y.mod(2) != 0) continue


                var horOffset = (manager.mapHorizontalSize / 2) * scale
                var verOffset = (manager.mapVerticalSize / 2) * scale

                var cellSize = cell.size * scale

                var cellX = x + (width / 2) + cell.x * cellSize - horOffset
                var cellY = y + (height / 2) + cell.y * cellSize - verOffset




                var renderCells = false


                if (!renderCells) {
                    nebulaSprite.setSize(cellSize * 10f, cellSize * 10f)
                    nebulaSprite.color = cell.color
                    nebulaSprite.alphaMult = cell.spriteAlpha * 0.023f * alphaMult
                    nebulaSprite.angle = cell.spriteAngle
                    nebulaSprite.renderAtCenter(cellX + cellSize/2, cellY + cellSize / 2)
                }

                else {
                    renderCell(cell, alphaMult)
                }



            }
        }


        var player = Global.getSector().playerFleet
        var loc = toMapPosition(player.location)

        playerPoint.alphaMult = alphaMult * 1f
        playerPoint.color = Misc.getBasePlayerColor()

        playerPoint.renderAtCenter(loc.x, loc.y)


        for (biome in manager.biomes) {
            var median = biome.centralCell

            var horOffset = (manager.mapHorizontalSize / 2) * scale
            var verOffset = (manager.mapVerticalSize / 2) * scale

            var cellSize = median.size * scale

            var cellX = x + (width / 2) + median.x * cellSize - horOffset
            var cellY = y + (height / 2) + median.y * cellSize - verOffset

            labelText.text = biome.name
            labelText.fontSize = 1600f * scale
            labelText.baseColor = biome.labelColor.setAlpha((200 * alphaMult).toInt())
            labelText.blendDest = GL11.GL_ONE_MINUS_SRC_ALPHA
            labelText.blendSrc = GL11.GL_SRC_ALPHA
            labelText.drawAtAngle(cellX - (labelText.width / 2), cellY + (labelText.height), biome.labelAngle)
        }

        endStencil()
    }

    fun toMapPosition(loc: Vector2f) : Vector2f {
        return Vector2f(x + (width / 2) + loc.x * scale, y + (height / 2) + loc.y * scale)
    }

    fun toRealPosition(loc: Vector2f) : Vector2f {
        return Vector2f((loc.x - (width / 2) - x) / scale, (loc.y - (height / 2) - y) / scale)
    }

    fun renderCell(cell: BiomeCell, alphaMult: Float) {
        var manager = AbyssUtils.getBiomeManager()
        var grid = manager.grid
        var playerCell = manager.getPlayerCell()

        var horOffset = (manager.mapHorizontalSize / 2) * scale
        var verOffset = (manager.mapVerticalSize / 2) * scale

        var cellSize = cell.size * scale

        var cellX = x + (width / 2) + cell.x * cellSize - horOffset
        var cellY = y + (height / 2) + cell.y * cellSize - verOffset

        var color = cell.color

        if (cell == playerCell) {
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

        var c = cell.color

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
        GL11.glPopMatrix()
    }


    fun startStencil(inverted: Boolean = false) {

        GL11.glClearStencil(0);
        GL11.glStencilMask(0xff);
        //set everything to 0
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        //disable drawing colour, enable stencil testing
        GL11.glColorMask(false, false, false, false); //disable colour
        GL11.glEnable(GL11.GL_STENCIL_TEST); //enable stencil

        // ... here you render the part of the scene you want masked, this may be a simple triangle or square, or for example a monitor on a computer in your spaceship ...
        //begin masking
        //put 1s where I want to draw
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff); // Do not test the current value in the stencil buffer, always accept any value on there for drawing
        GL11.glStencilMask(0xff);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE); // Make every test succeed

        // <draw a quad that dictates you want the boundaries of the panel to be>

        GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        if (!inverted) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        }
        else
        {
            GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        }
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}