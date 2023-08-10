package assortment_of_things.abyss.shipsystem

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*


//Only here to test funny stuff
class TestShipSystem : BaseShipSystemScript(), CombatLayeredRenderingPlugin {

    var ship: ShipAPI? = null
    var system: ShipSystemAPI? = null
    var addedRenderer = false

    var activated = false

    var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    var rangeMod = 1f

    var empInterval = IntervalUtil(0.2f, 0.2f)
    var afterimageInterval = IntervalUtil(0.2f, 0.2f)

    var modID = "rat_dimensional_bubble"

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        if (stats!!.entity == null) return

        ship = stats!!.entity as ShipAPI
        system = ship!!.system

        if (!addedRenderer) {
            Global.getCombatEngine().addLayeredRenderingPlugin(this)
            addedRenderer = true
        }
    }

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

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI) {
        if (ship == null) return

        var width = viewport.visibleWidth
        var height = viewport.visibleHeight

        var x = viewport.llx
        var y = viewport.lly

        var color = Color(100, 0, 255)

        if (system!!.isActive && !activated) {
            Global.getSoundPlayer().playSound("system_ammo_feeder", 1f, 1f, ship!!.location, ship!!.velocity)
            activated = true
        }
        if (!system!!.isActive) {
            activated = false
        }

        var base = 5.5f
        var bonus = 2f * system!!.effectLevel

        var enabled = true

        if (!Global.getCombatEngine().isPaused) {
            if (!ship!!.isAlive || ship!!.isHulk || ship!!.fluxTracker.isOverloaded) {
                rangeMod -= 0.1f * Global.getCombatEngine().elapsedInLastFrame
                enabled = false
            }
            else {
                rangeMod += 0.1f * Global.getCombatEngine().elapsedInLastFrame
                enabled = true
            }
        }
        rangeMod = MathUtils.clamp(rangeMod, 0f, 1f)



        ship!!.setJitter(this, color.setAlpha(30), 1f * rangeMod , 3, 0f, 0f)
        ship!!.setJitterUnder(this, color.setAlpha(75), 1f * rangeMod , 25, 0f, 10f)
      //  ship!!.engineController.fadeToOtherColor("rat_primordial_enginefade", color, color.setAlpha(75), 1f, 1f)

        var rad = (base + bonus) * rangeMod

        // var radius = Math.exp((5.5f * system!!.effectLevel).toDouble()).toFloat()
        var radius = Math.exp(rad.toDouble()).toFloat()
        var segments = 100

        var doEmp = false
        empInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
        if (empInterval.intervalElapsed() && !Global.getCombatEngine().isPaused && system!!.isActive) {
            doEmp = true
        }

        var doAfterimage = false
        afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
        if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
        {
            doAfterimage = true
        }


        for (other in Global.getCombatEngine().ships) {
            if (other.owner != ship!!.owner) continue
            var player = other == Global.getCombatEngine().playerShip

            var statMod = 1f
            var inRadius = false

            if ((MathUtils.getDistance(ship!!.location, other.location) < radius || other == ship) && other.isAlive && !other.isHulk) {
                if (system!!.isActive) statMod = 1f - (0.5f * system!!.effectLevel)
                else statMod = 1f

                if (doEmp) {
                    other!!.exactBounds.update(other!!.location, other!!.facing)
                    var from = Vector2f(other!!.exactBounds.segments.random().p1)
                    var to = Vector2f(other!!.exactBounds.segments.random().p1)
                    Global.getCombatEngine().spawnEmpArcVisual(from, other, to, SimpleEntity(to), 10f, color.setAlpha(100), color.setAlpha(50))
                }

                if (doAfterimage) {
                    AfterImageRenderer.addAfterimage(other!!, color.setAlpha(100), color.setAlpha(100), 0.75f + (0.25f * system!!.effectLevel), 2f, Vector2f().plus(other!!.location))
                }

                inRadius = true
            }
            else
            {
                statMod = 0f
            }

            if (!enabled) {
                statMod = 0f
            }

            var stats = other.mutableStats

            //val shipTimeMult = 1 + (1 * statMod)
            //val shipTimeMult = 1 + (0.5f * statMod)
            val shipTimeMult = 1 + (0.75f * statMod)
            stats.timeMult.modifyMult(modID, shipTimeMult)

            if (player) {
                    Global.getCombatEngine().timeMult.modifyMult(modID, 1f / shipTimeMult)
            }

            stats.fluxDissipation.modifyMult(modID, 1 + (0.20f * statMod))

            stats.ballisticRoFMult.modifyMult(modID, 1 + (0.10f * statMod))
            stats.energyRoFMult.modifyMult(modID, 1 + (0.10f * statMod))

            stats.ballisticWeaponDamageMult.modifyMult(modID, 1 + (0.10f * statMod))
            stats.energyWeaponDamageMult.modifyMult(modID, 1 + (0.10f * statMod))
            stats.beamWeaponDamageMult.modifyMult(modID, 1 + (0.10f * statMod))

            stats.ballisticWeaponFluxCostMod.modifyMult(modID, 1 - (0.10f * statMod))
            stats.energyWeaponFluxCostMod.modifyMult(modID, 1 - (0.10f * statMod))

            stats.ballisticAmmoRegenMult.modifyMult(modID, 1 - (0.10f * statMod))
            stats.energyAmmoRegenMult.modifyMult(modID, 1 - (0.10f * statMod))

            stats.shieldAbsorptionMult.modifyMult(modID, 1 - (0.10f * statMod))
            stats.armorDamageTakenMult.modifyMult(modID, 1 - (0.10f * statMod))
            stats.hullDamageTakenMult.modifyMult(modID, 1 - (0.10f * statMod))

        }

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