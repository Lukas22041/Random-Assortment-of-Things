package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.OpenGLUtil
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class RiftEntrance : BaseCustomEntityPlugin() {

    @Transient
    var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")

    @Transient
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    @Transient
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    @Transient
    var wormhole3 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    var radius = 300f

    @Transient
    var line1 = Global.getSettings().getSprite("fx", "base_trail_zapWithCore")

    @Transient
    var line2 = Global.getSettings().getSprite("fx", "base_trail_heavySmoke")

    var time = 0f

    override fun init(entity: SectorEntityToken?, params: Any?) {
        this.entity = entity

    }

    override fun advance(amount: Float) {
        if (!Global.getSector().isPaused) {
            time += 10f * amount
        }
        if (time > 1000000) time = 0f
    }

    override fun getRenderRange(): Float {
        return 1000000f
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (sprite == null) {
            sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")
            wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
            wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
            wormhole3 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
            line1 = Global.getSettings().getSprite("fx", "base_trail_zapWithCore")
            line2 = Global.getSettings().getSprite("fx", "base_trail_heavySmoke")
        }

        if (entity == null) return


        var color = Color(200, 0, 50)

        var width = viewport!!.visibleWidth
        var height = viewport.visibleHeight

        var x = viewport.llx
        var y = viewport.lly





        startStencil(entity, radius, 100)

   /*     sprite.setSize(width, height)
        sprite.color = color
        sprite.alphaMult = 1f
        sprite.setNormalBlend()
        sprite.render(x, y)*/

        wormhole3.setSize(1100f, 1100f)
        wormhole3.setAdditiveBlend()
        wormhole3.alphaMult = 0.5f
        if (!Global.getSector().isPaused) wormhole3.angle += 0.075f
        wormhole3.color = color
        wormhole3.renderAtCenter(entity.location.x, entity.location.y)


        wormhole.setSize(width * 1.3f, width *  1.3f)
        wormhole.setAdditiveBlend()
        wormhole.alphaMult = 0.3f
        if (!Global.getSector().isPaused) wormhole.angle += 0.075f
        wormhole.color = color
        wormhole.renderAtCenter(x + width / 2, y + height / 2)

        wormhole2.setSize(width * 1.35f, width *  1.35f)
        wormhole2.setAdditiveBlend()
        wormhole2.alphaMult = 0.2f
        if (!Global.getSector().isPaused) wormhole2.angle += 0.05f
        wormhole2.color = Color(200, 0, 100)
        wormhole2.renderAtCenter(x + width / 2, y + height / 2)


        endStencil()

        //renderBorder(entity!!, radius, color.setAlpha(100), 100)

        line1.color = color.setAlpha(75)
        line2.color = color.setAlpha(25)

        OpenGLUtil.drawTexturedRing(line1, entity.location, radius, 10f, 100, 1f, time)
        OpenGLUtil.drawTexturedRing(line2, entity.location, radius, 15f, 100, 1.5f, time)
        OpenGLUtil.drawTexturedRing(line2, entity.location, radius, 20f, 100, 1.25f, time)

    }

    fun startStencil(entity: SectorEntityToken, radius: Float, circlePoints: Int) {

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

        GL11.glBegin(GL11.GL_POLYGON) // Middle circle

        val x = entity.location.x
        val y = entity.location.y

        for (i in 0..circlePoints) {

            val angle: Double = (2 * Math.PI * i / circlePoints)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)

        }

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

    fun renderBorder(entity: SectorEntityToken, radius: Float, color: Color, circlePoints: Int) {
        var c = color
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (1f))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        val x = entity.location.x
        val y = entity.location.y


        for (i in 0..circlePoints) {


            val angle: Double = (2 * Math.PI * i / circlePoints)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)

        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }

    override fun createMapTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        tooltip!!.addPara("Abyssal Rift", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "Abyssal Rift")

    }
}