package assortment_of_things.frontiers.ui

import assortment_of_things.frontiers.SettlementData
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f

class SiteDisplayElement(var site: SettlementData, var planetSprite: SpriteAPI, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    init {
        enableTransparency = true
        renderBackground = false
        renderBorder = false
        borderAlpha = 0.8f

        onClick {
            playClickSound()
        }
        onHoverEnter {
            playScrollSound()
        }
    }

    var bounds = ArrayList<Vector2f>()

    override fun positionChanged(position: PositionAPI?) {
        super.positionChanged(position)

        bounds = ArrayList<Vector2f>()

        var radius = width * 0.5f
        var angle = 30f
        for (i in 0 until 6) {
            bounds.add(MathUtils.getPointOnCircumference(Vector2f(position!!.centerX, position!!.centerY), radius.toFloat(), angle))
            angle += 60
        }
    }



    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        startStencil()

        planetSprite.alphaMult = alphaMult
        planetSprite.setNormalBlend()
        planetSprite.setSize(1024f * 1.25f, 512f * 1.25f)
        var siteLoc = MathUtils.getPointOnCircumference(Vector2f(x, y), site.distanceFromCenteer, site.angleFromCenter)
        var loc = Vector2f(siteLoc.x + width / 2 , siteLoc.y + height / 2)
        planetSprite.renderAtCenter(loc.x, loc.y)

        if (isHovering) {
            planetSprite.alphaMult = alphaMult * 0.2f
            planetSprite.setAdditiveBlend()
            planetSprite.renderAtCenter(loc.x, loc.y)
        }

        var glowTexture = site.primaryPlanet.spec.glowTexture
        if (glowTexture != null) {
            var glowSprite = Global.getSettings().getAndLoadSprite(glowTexture)

            glowSprite.alphaMult = 0.7f
            glowSprite.setSize(1024f * 1.25f, 512f * 1.25f)
            glowSprite.renderAtCenter(x + width / 2, y + height / 2)
            glowSprite.setAdditiveBlend()
        }

        endStencil()

        var c = Misc.getDarkPlayerColor().brighter().brighter()
        var alpha = 0.8f

        if (isHovering) {
            alpha = 1f
            c = Misc.getBasePlayerColor()

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

        var points = bounds
        for (point in points) {
            GL11.glVertex2f(point.x, point.y)
        }
        GL11.glVertex2f(points.first().x, points.first().y)

        GL11.glEnd()
        GL11.glPopMatrix()


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


        GL11.glBegin(GL11.GL_POLYGON)

        var points = bounds
        for (point in points) {
            GL11.glVertex2f(point.x, point.y)
        }
        GL11.glVertex2f(points.first().x, points.first().y)

        GL11.glEnd()

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