package assortment_of_things.frontiers.ui

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.data.SiteData
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.awt.Polygon

class SiteSelectionPickerElement(var planet: PlanetAPI, var planetSprite: SpriteAPI, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    class SiteHexagon(var site: SiteData) {
        var selected = false
        var hovering = false
        var bounds = ArrayList<Vector2f>()
        var ressourceSprite: SpriteAPI? = null
        var ressourceSpriteSil: SpriteAPI? = null
    }

    var hexagons = ArrayList<SiteHexagon>()
    var hoveredHexagon: SiteHexagon? = null
    var selectedHexagon: SiteHexagon? = null

    init {
        enableTransparency = true
        renderBackground = false
        renderBorder = false
        borderAlpha = 0.8f
    }

    override fun positionChanged(position: PositionAPI?) {
        super.positionChanged(position)

        if (planet == null) return
        generateGrid(Vector2f(x + width / 2, y + height / 2), 5, 48, 5)
    }

    //https://stackoverflow.com/questions/20734438/algorithm-to-generate-a-hexagonal-grid-with-coordinate-system
    private fun generateGrid(origin: Vector2f, size: Int, radius: Int, padding: Int) {
        hexagons = ArrayList()
        hexagons.clear()

        var requiresDataGeneration = false
        var siteData = FrontiersUtils.getSiteData(planet!!)
        if (siteData.isEmpty()) {
            siteData = FrontiersUtils.generateNewSites(planet!!)
        }

        var locations = ArrayList<Vector2f>()

        val ang30 = Math.toRadians(30.0)
        val xOff = Math.cos(ang30) * (radius + padding)
        val yOff = Math.sin(ang30) * (radius + padding)
        val half = size / 2

        var index = 0
        for (row in 0 until size) {
            val cols = size - Math.abs(row - half)
            for (col in 0 until cols) {
                val x = (origin.x + xOff * (col * 2 + 1 - cols)) as Double
                val y = (origin.y + yOff * (row - half) * 3) as Double

                var site = siteData.get(index)
                var hexagon = SiteHexagon(site)
                hexagons.add(hexagon)

                var angle = 30f
                for (i in 0 until 6) {
                    hexagon.bounds.add(MathUtils.getPointOnCircumference(Vector2f(x.toFloat(), y.toFloat()), radius.toFloat(), angle))
                    angle += 60
                }

                var ressource = FrontiersUtils.getRessource(planet!!, site)
                if (ressource != null) {
                    hexagon.ressourceSprite = Global.getSettings().getSprite(ressource.getIcon())
                    hexagon.ressourceSpriteSil = Global.getSettings().getSprite(ressource.getSpec().iconSil)
                }

                site.location = Vector2f(x.toFloat(), y.toFloat())

                var angleFromCenter = Misc.getAngleInDegrees(site.location, origin)
                var distanceFromCenter = Misc.getDistance(site.location, origin)

                site.angleFromCenter = angleFromCenter
                site.distanceFromCenter = distanceFromCenter

                index += 1
            }
        }


    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        startStencil()

        planetSprite.alphaMult = alphaMult
        planetSprite.setSize(1024f * 1.25f, 512f * 1.25f)
        planetSprite.renderAtCenter(x + width / 2, y + height / 2)

        var glowTexture = planet.spec.glowTexture
        if (glowTexture != null) {
            var glowSprite = Global.getSettings().getAndLoadSprite(glowTexture)

            glowSprite.alphaMult = 0.7f
            glowSprite.setSize(1024f * 1.25f, 512f * 1.25f)
            glowSprite.renderAtCenter(x + width / 2, y + height / 2)
            glowSprite.setAdditiveBlend()
        }

        endStencil()

        for (hexagon in hexagons) {

            var ressource = FrontiersUtils.getRessource(planet!!, hexagon.site)

            var silColor = Misc.getPositiveHighlightColor()

            if (ressource != null) {
                var tiers = ressource.getSpec().tiers
                var count = tiers.size
                var tier = ressource.getTier().toFloat()
                var level = (1f / count)

                tier -= 1
                level *= tier

                if (count == 1) {
                    level = 1f
                }
                if (count == 2) {
                    if (ressource.getTier() == 1) {
                        level = 0f
                    }
                    else {
                        level = 1f
                    }
                }

                silColor = Misc.interpolateColor(Misc.getNegativeHighlightColor(), Misc.getPositiveHighlightColor(), level)
            }

            if (hexagon.ressourceSprite != null) {
                var sprite = hexagon.ressourceSprite
                var sil = hexagon.ressourceSpriteSil

                sil!!.alphaMult = alphaMult
                sil!!.color = silColor
                sil!!.setSize(65f, 65f)
                sil!!.renderAtCenter(hexagon.site.location.x, hexagon.site.location.y)

                sprite!!.alphaMult = alphaMult
                sprite!!.color = Color(255, 255, 255)
                sprite!!.setSize(60f, 60f)
                sprite!!.renderAtCenter(hexagon.site.location.x, hexagon.site.location.y)
            }

            var c = Misc.getDarkPlayerColor().brighter()
            var alpha = 0.7f

            if (hexagon.hovering) {
                alpha = 1f
                c = Misc.getBasePlayerColor()
            }
            if (hexagon.selected) {
                c = Misc.getHighlightColor()
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
                c.alpha / 255f * (alphaMult * alpha))

            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            var points = hexagon.bounds
            for (point in points) {
                GL11.glVertex2f(point.x, point.y)
            }
            GL11.glVertex2f(points.first().x, points.first().y)

            GL11.glEnd()
            GL11.glPopMatrix()
        }


    }


    override fun processInput(events: MutableList<InputEventAPI>?) {


        hoveredHexagon = null
        for (event in events!!) {
            if (event.isConsumed) continue
            if (event.isMouseEvent) {
                var point = Vector2f(event.x.toFloat(), event.y.toFloat())

                for (hexagon in hexagons) {

                    var x = hexagon.bounds.map { it.x.toInt() }.toIntArray()
                    var y = hexagon.bounds.map { it.y.toInt() }.toIntArray()

                    var poly = Polygon(x, y, 6)
                    if (poly.contains(point.x.toInt(), point.y.toInt())) {

                        if (!hexagon.hovering) {
                            playScrollSound()
                        }

                        hoveredHexagon = hexagon
                        hexagon.hovering = true
                        continue
                    }
                    else {
                        hexagon.hovering = false
                    }
                }
            }
            if (!event.isConsumed && event.isMouseDownEvent) {
                var point = Vector2f(event.x.toFloat(), event.y.toFloat())

                for (hexagon in hexagons) {

                    var x = hexagon.bounds.map { it.x.toInt() }.toIntArray()
                    var y = hexagon.bounds.map { it.y.toInt() }.toIntArray()

                    var poly = Polygon(x, y, 6)
                    if (poly.contains(point.x.toInt(), point.y.toInt())) {
                        hexagons.forEach { it.selected = false }
                        hexagon.selected = true
                        selectedHexagon = hexagon
                        event.consume()
                        playClickSound()
                        continue
                    }
                }
            }
        }

        //super.processInput(events)



    }


    fun startStencil() {

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


        for (hexagon in hexagons) {
            GL11.glBegin(GL11.GL_POLYGON)

            var points = hexagon.bounds
            for (point in points) {
                GL11.glVertex2f(point.x, point.y)
            }
            GL11.glVertex2f(points.first().x, points.first().y)

            GL11.glEnd()
        }



        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

}