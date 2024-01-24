package assortment_of_things.abyss.shipsystem.activators

import activators.CombatActivator
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

class PrimordialSeaActivator(var ship: ShipAPI) : CombatActivator(ship) {

    var addedRenderer = false
    var renderer: PrimordialSeaRenderer = PrimordialSeaRenderer(ship, this)

    var range = 3000f

    init {
        Global.getCombatEngine().addLayeredRenderingPlugin(renderer)
    }

    override fun getBaseActiveDuration(): Float {
        return 15f
    }

    override fun getBaseCooldownDuration(): Float {
        return 3f
    }

    override fun getBaseInDuration(): Float {
        return 2f
    }

    override fun getBaseOutDuration(): Float {
        return 1f
    }

    override fun isToggle(): Boolean {
        return false
    }

    override fun shouldActivateAI(amount: Float): Boolean {
        return true
    }

    override fun getDisplayText(): String {
        return "Primordial Sea"
    }

    override fun getHUDColor(): Color {
        return AbyssUtils.GENESIS_COLOR
    }

    override fun advance(amount: Float) {
        if (!addedRenderer) {
            addedRenderer = true
        }
    }

    fun getCurrentRange() : Float {
        return range * effectLevel * effectLevel
    }
}

class PrimordialSeaRenderer(var ship: ShipAPI, var activator: PrimordialSeaActivator) : CombatLayeredRenderingPlugin {

    var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    override fun init(entity: CombatEntityAPI?) {

    }

    override fun cleanup() {

    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    override fun render(layer: CombatEngineLayers, viewport: ViewportAPI) {
        var width = viewport.visibleWidth
        var height = viewport.visibleHeight

        var x = viewport.llx
        var y = viewport.lly

        var color = Color(100, 0, 255)

        var radius = activator.getCurrentRange()
        var segments = 100

        startStencil(ship!!, radius, segments)

        sprite.setSize(width, height)
        sprite.color = color
        sprite.alphaMult = 1f
        sprite.render(x, y)

        wormhole.setSize(width * 1.3f, width *  1.3f)
        wormhole.setAdditiveBlend()
        wormhole.alphaMult = 0.3f
        if (!Global.getCombatEngine().isPaused) wormhole.angle += 0.075f
        wormhole.color = color
        wormhole.renderAtCenter(x + width / 2, y + height / 2)

        wormhole2.setSize(width * 1.35f, width *  1.35f)
        wormhole2.setAdditiveBlend()
        wormhole2.alphaMult = 0.2f
        if (!Global.getCombatEngine().isPaused) wormhole2.angle += 0.05f
        wormhole2.color = Color(50, 0, 255)
        wormhole2.renderAtCenter(x + width / 2, y + height / 2)

        endStencil()

        renderBorder(ship!!, radius, color, segments)
    }

    fun startStencil(ship: ShipAPI, radius: Float, circlePoints: Int) {

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

        val x = ship.location.x
        val y = ship.location.y

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

    fun renderBorder(ship: ShipAPI, radius: Float, color: Color, circlePoints: Int) {
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

        val x = ship.location.x
        val y = ship.location.y


        for (i in 0..circlePoints) {
            val angle: Double = (2 * Math.PI * i / circlePoints)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }
}