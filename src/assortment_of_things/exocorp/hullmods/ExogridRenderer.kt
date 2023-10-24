package assortment_of_things.exocorp.hullmods

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.util.*
import kotlin.collections.ArrayList

class ExogridRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

    var glow = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow1.png")
    var lastJitterLocations = ArrayList<Vector2f>()

    override fun getRenderRadius(): Float {
        return 100000000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        var active = false
        var level = ship.system.effectLevel
        var state = ship.system.state
        var paused = Global.getCombatEngine().isPaused

        if (state == ShipSystemAPI.SystemState.ACTIVE || state == ShipSystemAPI.SystemState.IN || state == ShipSystemAPI.SystemState.OUT) {
            active = true
        }

        if (ship.phaseCloak != null) {
            level = ship.phaseCloak.effectLevel
            state = ship.phaseCloak.state
        }


        if (active) {
            var coilLocationDeco = ship.allWeapons.find { it.spec.weaponId == "rat_exo_coil_location" }
            var location = ship.location
            if (coilLocationDeco != null) {
                location = coilLocationDeco.location
            }

            startStencilAroundShip(location, (ship.collisionRadius * level) * 1.2f)

            glow.setNormalBlend()
            glow.alphaMult = 0.2f + (level * 0.8f)
            glow.angle = ship.facing - 90
            glow.renderAtCenter(ship.location.x, ship.location.y)


            var jitterCount = 5
            var jitterMaxRange = 2f //Works better without being effected b
            var jitterAlpha = 0.2f

            if (!paused) {
                lastJitterLocations.clear()
            }

            for (i in 0 until jitterCount) {

                var jitterLoc = Vector2f()

                if (!paused) {
                    var x = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)
                    var y = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)

                    jitterLoc = Vector2f(x, y)
                    lastJitterLocations.add(jitterLoc)
                }
                else {
                    jitterLoc = lastJitterLocations.getOrElse(i) {
                        Vector2f()
                    }
                }




                glow.setAdditiveBlend()
                glow.alphaMult = 0f + (level * jitterAlpha)
                glow.renderAtCenter(ship.location.x + jitterLoc.x, ship.location.y + jitterLoc.y)
            }


            endStencil()

        }






    }


    fun startStencilAroundShip(location: Vector2f, radius: Float) {

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

        val x = location.x
        val y = location.y

        var points = 10
        for (i in 0..points) {

            val angle: Double = (2 * Math.PI * i / points)
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

}