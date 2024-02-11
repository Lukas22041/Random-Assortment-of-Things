package assortment_of_things.exotech.hullmods

import assortment_of_things.misc.SpriteWithShader
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.util.*
import kotlin.collections.ArrayList

class ExogridRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

    lateinit var systemGlow: SpriteAPI
    lateinit var phaseGlow: SpriteAPI
    var arkasPhantomGlow: SpriteAPI? = null
    var hasPhase = false

 /*   var vertex = Global.getSettings().loadText("data/shaders/testVertex.shader")
    var frag = Global.getSettings().loadText("data/shaders/testFragment.shader")

    var shaderRenderer = SpriteWithShader("graphics/ships/rat_makara.png", vertex, frag)*/

    init {
        systemGlow = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow1.png")
        hasPhase = ship.baseOrModSpec().hints.contains(ShipHullSpecAPI.ShipTypeHints.PHASE)

        if (hasPhase) {
            phaseGlow = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow2.png")
        }

        if (ship.baseOrModSpec().hullId == "rat_arkas_phantom") {
            arkasPhantomGlow = Global.getSettings().getAndLoadSprite("graphics/ships/exo/rat_arkas_glow2.png")
        }
    }

    var lastSystemJitterLocations = ArrayList<Vector2f>()
    var lastPhaseJitterLocations = ArrayList<Vector2f>()
    var lastArkasPhantomJitterLocations = ArrayList<Vector2f>()

    override fun getRenderRadius(): Float {
        return 100000000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        if (!ship.isAlive) return

        var active = false
        var level = ship.system?.effectLevel ?: 0f
        var systemState = ship.system?.state ?: ShipSystemAPI.SystemState.IDLE
        var phaseState = ship.phaseCloak?.state ?: ShipSystemAPI.SystemState.IDLE
        var exogridOverload = ship.variant.hasHullMod("rat_exogrid_overload")


        if (ship.baseOrModSpec().hullId == "rat_apheidas") {
            renderLeaniraModule()
        }

        if (arkasPhantomGlow != null) {
            renderArkasPhantom()
        }

        if ((exogridOverload) && !ship.fluxTracker.isOverloaded) {
            renderOverload()
        }

        if (systemState == ShipSystemAPI.SystemState.ACTIVE || systemState == ShipSystemAPI.SystemState.IN || systemState == ShipSystemAPI.SystemState.OUT) {
            renderSystem()
        }

        if (hasPhase && phaseState == ShipSystemAPI.SystemState.ACTIVE || phaseState == ShipSystemAPI.SystemState.IN || phaseState == ShipSystemAPI.SystemState.OUT || phaseState == ShipSystemAPI.SystemState.COOLDOWN) {
            renderPhase()
        }

        if (active) {


        }

        /*shaderRenderer.angle = ship.facing - 90
        shaderRenderer.renderAtCenter(ship.location.x + 200f, ship.location.y + 0f)
*/



    }

    fun renderSystem() {
        var coilLocationDeco = ship.allWeapons.find { it.spec.weaponId == "rat_exo_coil_location" }
        var location = ship.location
        if (coilLocationDeco != null) {
            location = coilLocationDeco.location
        }


        var level = ship.system?.effectLevel ?: 0f
        var levelOverride = ship!!.customData.get("rat_exogrid_level_override") as Float?
        if (levelOverride != null) {
            level = levelOverride
        }

        var systemState = ship.system.state

        var baseAlpha = 0.2f
        var extraRangeMult = 1.2f
        var radius = (ship.collisionRadius * level) * extraRangeMult
        if (systemState == ShipSystemAPI.SystemState.OUT ) {
            radius = ship.collisionRadius * extraRangeMult
        }

        startStencilAroundShip(location, radius)

        if (ship.phaseCloak != null) {
            level -= ship.phaseCloak.effectLevel
            level = MathUtils.clamp(level, 0f, 1f)

        }

        if (systemState == ShipSystemAPI.SystemState.OUT || ship.isPhased) {
            baseAlpha = 0.2f * level
        }

        systemGlow.setNormalBlend()
        systemGlow.alphaMult = baseAlpha + (level * 0.8f)
        systemGlow.angle = ship.facing - 90
        systemGlow.renderAtCenter(ship.location.x, ship.location.y)

        doJitter(systemGlow, level, lastSystemJitterLocations, 5, 2f)

        endStencil()
    }

    fun renderPhase() {
        var location = ship.location

        var level = ship.phaseCloak.effectLevel

        var outPercent = ship.phaseCloak.chargeDownDur / ship.phaseCloak.cooldown
        if (ship.phaseCloak.state == ShipSystemAPI.SystemState.OUT) {
            level = (1 - outPercent) + ship.phaseCloak.effectLevel * outPercent
        }
        if (ship.phaseCloak.state == ShipSystemAPI.SystemState.COOLDOWN) {
            var cooldownLevel = (ship.phaseCloak.cooldownRemaining - 0f) / (ship.phaseCloak.cooldown - 0f)
            level = cooldownLevel * (1 - outPercent)
        }

        var systemState = ship.phaseCloak.state

        phaseGlow.setNormalBlend()
        phaseGlow.alphaMult = level
        phaseGlow.angle = ship.facing - 90
        phaseGlow.renderAtCenter(ship.location.x, ship.location.y)

        doJitter(phaseGlow, level, lastPhaseJitterLocations, 5, 2 + (2f * level))

    }

    fun doJitter(sprite: SpriteAPI, level: Float, lastLocations: ArrayList<Vector2f>, jitterCount: Int, jitterMaxRange: Float) {

        var paused = Global.getCombatEngine().isPaused
     /*   var jitterCount = 5
        var jitterMaxRange = 2f*/
        var jitterAlpha = 0.2f


        if (!paused) {
            lastLocations.clear()
        }

        for (i in 0 until jitterCount) {

            var jitterLoc = Vector2f()

            if (!paused) {
                var x = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)
                var y = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)

                jitterLoc = Vector2f(x, y)
                lastLocations.add(jitterLoc)
            }
            else {
                jitterLoc = lastLocations.getOrElse(i) {
                    Vector2f()
                }
            }

            sprite.setAdditiveBlend()
            sprite.alphaMult = level * jitterAlpha
            sprite.renderAtCenter(ship.location.x + jitterLoc.x, ship.location.y + jitterLoc.y)
        }
    }

    fun renderOverload() {
        systemGlow.setNormalBlend()
        systemGlow.alphaMult = 1f
        systemGlow.angle = ship.facing - 90
        systemGlow.renderAtCenter(ship.location.x, ship.location.y)
    }

    fun renderLeaniraModule() {
        var parent = ship.customData.get("rat_apheidas_parent") as ShipAPI? ?: return

        systemGlow.setNormalBlend()
        systemGlow.alphaMult = parent.system.effectLevel
        systemGlow.angle = ship.facing - 90
        systemGlow.renderAtCenter(ship.location.x, ship.location.y)

        doJitter(systemGlow, parent.system.effectLevel, lastSystemJitterLocations, 4, 2f)
    }

    fun renderArkasPhantom() {

        var parent = ship.customData.get("rat_phantom_parent") as ShipAPI ?: return
        var level = parent.customData.get("rat_exogrid_level_override") as Float ?: return

        level *= level * 0.3f

        arkasPhantomGlow!!.setAdditiveBlend()
        arkasPhantomGlow!!.alphaMult = level
        arkasPhantomGlow!!.angle = ship.facing - 90
        arkasPhantomGlow!!.renderAtCenter(ship.location.x, ship.location.y)

        doJitter(arkasPhantomGlow!!, level, lastArkasPhantomJitterLocations, 5, 5f)
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