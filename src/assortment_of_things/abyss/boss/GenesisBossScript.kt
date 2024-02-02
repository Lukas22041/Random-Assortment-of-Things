package assortment_of_things.abyss.boss

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalStormParticleManager
import assortment_of_things.misc.GraphicLibEffects
import assortment_of_things.misc.StateBasedTimer
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.dark.shaders.distortion.RippleDistortion
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*
import kotlin.math.max

class GenesisBossScript(var ship: ShipAPI) : CombatLayeredRenderingPlugin, HullDamageAboutToBeTakenListener {

    var phase = Phases.P1
    var transitionTimer = StateBasedTimer(1.5f, 2f, 3f)
    var transitionDone = false

    var empInterval = IntervalUtil(2f, 2f)
    var activateZone = false

    var darken = Global.getSettings().getSprite("graphics/fx/rat_black.png")
    var vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette_reversed.png")
    var vignetteLevel = 0f

    var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    var particles = ArrayList<AbyssalStormParticleManager.AbyssalLightParticle>()

    var particleInterval = IntervalUtil(0.2f, 0.2f)
    var halo = Global.getSettings().getSprite("rat_terrain", "halo")

    var ripple: RippleDistortion? = null

    var startedMusic = false

    enum class Phases {
        P1, P2, P3
    }






    override fun init(entity: CombatEntityAPI?) {
        ship.addListener(this)
    }

    override fun cleanup() {

    }

    override fun isExpired(): Boolean {
        return false
    }



    override fun advance(amount: Float) {

        handleParticles(amount)


        var soundplayer = Global.getSoundPlayer()

        if (!startedMusic && Global.getCombatEngine().getTotalElapsedTime(false) >= 1f) {
            soundplayer.playCustomMusic(1, 1, "rat_abyss_genesis1", true)
            startedMusic = true
        }

        if (phase == Phases.P2) {

            var realAmount = amount / Global.getCombatEngine().timeMult.modifiedValue

            if (!transitionDone) {

                ship.system.forceState(ShipSystemAPI.SystemState.COOLDOWN, 1f)
                ship.isHoldFireOneFrame = true
                transitionTimer.advance(realAmount)
                var level = transitionTimer.level
                var timeMult = 50f

                var color = AbyssUtils.GENESIS_COLOR

                ship!!.setJitter(this, color.setAlpha(150), level, 3, 0f, 0f)
                ship!!.setJitterUnder(this, color.setAlpha(255), level, 25, 0f, 60f)

                val realTimeMult = 1f + (timeMult - 1f) * level
                ship.mutableStats.timeMult.modifyMult("rat_boss_timemult", realTimeMult)
                Global.getCombatEngine().timeMult.modifyMult("rat_boss_timemult", 1f / realTimeMult)

                PostProcessShader.setNoise(false, 0.3f * level)
                PostProcessShader.setSaturation(false, 1f + (0.2f * level))
                Global.getSoundPlayer().applyLowPassFilter(1f, 1 - (0.3f * level))

                ship.hitpoints += (ship.maxHitpoints * 0.33f) * realAmount
                ship.hitpoints = MathUtils.clamp(ship.hitpoints, 0f, ship.maxHitpoints)

                var percentPerSecond = 0.1f
                if (ship.fluxLevel > 0f) {
                    ship.fluxTracker.decreaseFlux((ship.fluxTracker.maxFlux * percentPerSecond) * realAmount)
                }

                if (transitionTimer.state == StateBasedTimer.TimerState.Out && !activateZone) {
                    activateZone = true
                    Global.getSoundPlayer().playSound("rat_genesis_system_sound", 0.7f, 1.3f, ship.location, ship.velocity)

                    ripple = GraphicLibEffects.CustomRippleDistortion(ship!!.location, Vector2f(), ship.collisionRadius + 500, 75f, true, ship!!.facing, 360f, 1f
                        ,0.5f, 3f, 1f, 1f, 1f)

                }

                if (ripple != null) {
                    ripple!!.advance(realAmount)
                }

                if (transitionTimer.done) {
                    transitionDone = true
                    ship.mutableStats.timeMult.modifyMult("rat_boss_timemult", 1f)
                    Global.getCombatEngine().timeMult.modifyMult("rat_boss_timemult", 1f)
                    PostProcessShader.resetDefaults()
                }
            }

            particleInterval.advance(realAmount)
            if (particleInterval.intervalElapsed()) {



                var count = 25
                var fadeInOverwrite = false

                if (particles.size <= 50) {
                    count = 1000
                    fadeInOverwrite = true
                }


                for (i in 0..count) {

                    var velocity = Vector2f(0f, 0f)
                    velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(200f, 550f), MathUtils.getRandomNumberInRange(180f, 210f)))

                    var playership = Global.getCombatEngine().playerShip
                    var spawnLocation = ship.location
                    if (Random().nextFloat() >= 0.5f && playership != null) {
                        spawnLocation = playership.location
                    }
                    //var spawnLocation = MathUtils.getPointOnCircumference(Vector2f(), 45f, entity.facing + 180)

                    var randomX = MathUtils.getRandomNumberInRange(-5000f, 5000f)
                    var randomY = MathUtils.getRandomNumberInRange(-5000f, 5000f)

                    spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

                    var fadeIn = MathUtils.getRandomNumberInRange(1f, 1.5f)
                    if (fadeInOverwrite) fadeIn = 0.05f
                    var duration = MathUtils.getRandomNumberInRange(2f, 4f)
                    var fadeOut = MathUtils.getRandomNumberInRange(1f, 2.5f)

                    var size = MathUtils.getRandomNumberInRange(25f, 50f)

                    var alpha = MathUtils.getRandomNumberInRange(0.25f, 0.45f)

                    particles.add(AbyssalStormParticleManager.AbyssalLightParticle(fadeIn,
                        duration,
                        fadeOut,
                        AbyssUtils.GENESIS_COLOR,
                        alpha,
                        size,
                        spawnLocation,
                        velocity))
                }
            }
        }
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS, CombatEngineLayers.JUST_BELOW_WIDGETS)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    fun getPhase2Range() : Float {
        var zoneLevel = 1 - transitionTimer.level
        var radius = 10000 * (zoneLevel * zoneLevel)
        if (zoneLevel >= 0.95f) {
            radius = 30000 * (zoneLevel * zoneLevel)
        }
        return radius
    }

    override fun render(layer: CombatEngineLayers, viewport: ViewportAPI) {

        var width = viewport.visibleWidth
        var height = viewport.visibleHeight

        var x = viewport.llx
        var y = viewport.lly

        var color = Color(100, 0, 255)

        vignetteLevel = max(transitionTimer.level, vignetteLevel)
        vignetteLevel = MathUtils.clamp(vignetteLevel, 0f, 1f)
      /*  if (transitionTimer.level >= vignetteLevel) {
            vignetteLevel = transitionTimer.level
        }*/

        if (layer == CombatEngineLayers.BELOW_PLANETS) {
            darken.color = Color(0, 0, 0)
            darken.alphaMult = vignetteLevel * 0.7f
            darken.setSize(viewport!!.visibleWidth + 100, viewport!!.visibleHeight + 100)
            darken.render(viewport!!.llx - 50, viewport!!.lly - 50)

            if (activateZone) {

                var segments = 100
                var zoneLevel = 1 - transitionTimer.level
                var radius = getPhase2Range()

                startStencil(ship!!, radius, segments)

                sprite.setSize(width, height)
                sprite.color = color
                sprite.alphaMult = 1f
                sprite.render(x, y)

                wormhole.setSize(width * 1.3f, width *  1.3f)
                wormhole.setAdditiveBlend()
                wormhole.alphaMult = 0.2f
                if (!Global.getCombatEngine().isPaused) wormhole.angle += 0.075f
                wormhole.color = Color(200, 0, 50)
                wormhole.renderAtCenter(x + width / 2, y + height / 2)

                wormhole2.setSize(width * 1.35f, width *  1.35f)
                wormhole2.setAdditiveBlend()
                wormhole2.alphaMult = 0.2f
                if (!Global.getCombatEngine().isPaused) wormhole2.angle += 0.05f
                wormhole2.color = Color(50, 0, 255)
                wormhole2.renderAtCenter(x + width / 2, y + height / 2)

                for (particle in particles) {

                    if (viewport!!.isNearViewport(particle.location, particle.size * 2)) {
                        halo!!.alphaMult = 0 + (particle.alpha * particle.level )
                        halo!!.color = particle.color
                        halo!!.setSize(particle.size / 2, particle.size / 2)
                        halo!!.setAdditiveBlend()
                        halo!!.renderAtCenter(particle.location.x, particle.location.y)
                    }
                }

                endStencil()

                renderBorder(ship!!, radius, color, segments)
            }
        }




        if (layer == CombatEngineLayers.JUST_BELOW_WIDGETS) {
            vignette.color = AbyssUtils.GENESIS_COLOR.darker()
            vignette.alphaMult = 0.5f * vignetteLevel

            var offset = 300
            vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
            vignette.render(viewport!!.llx - (offset * 0.5f), viewport!!.lly - (offset * 0.5f))
        }
    }


    override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?, point: Vector2f?, damageAmount: Float): Boolean {

        if (phase == Phases.P1 || phase == Phases.P2) {

            if (phase == Phases.P2) {
                return true
            }

            if (ship!!.hitpoints - damageAmount <= 0) {

                if (phase == Phases.P1) {

                    ship.hitpoints = 10f
                    ship.fluxTracker.stopOverload()

                    phase = Phases.P2
                    ship.setCustomData("rat_boss_second_phase", true)

                    var color = AbyssUtils.GENESIS_COLOR

                    for (i in 0 until 100) {
                        ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                        var from = Vector2f(ship!!.exactBounds.segments.random().p1)

                        var angle = Misc.getAngleInDegrees(ship.location, from)
                        var to = MathUtils.getPointOnCircumference(ship.location, MathUtils.getRandomNumberInRange(100f, 300f) + ship.collisionRadius, angle + MathUtils.getRandomNumberInRange(-30f, 30f))

                        Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, color, color)
                    }

                    Global.getSoundPlayer().playSound("rat_bloodstream_trigger", 1f, 2f, ship.location, ship.velocity)
                    Global.getSoundPlayer().playSound("system_entropy", 1f, 1.5f, ship.location, ship.velocity)
                    Global.getSoundPlayer().playSound("explosion_ship", 1f, 1f, ship.location, ship.velocity)

                    GraphicLibEffects.CustomRippleDistortion(Vector2f(ship.location), Vector2f(), 3000f, 10f, true, ship.facing, 360f, 1f
                        ,1f, 1f, 1f, 1f, 1f)

                    GraphicLibEffects.CustomBubbleDistortion(Vector2f(ship.location), Vector2f(), 1000f + ship.collisionRadius, 25f, true, ship.facing, 360f, 1f
                        ,0.1f, 0.1f, 1f, 0.3f, 1f)

                    //Global.getSoundPlayer().pauseCustomMusic()
                    Global.getSoundPlayer().playCustomMusic(1, 1, "rat_abyss_genesis2", true)

                }

                return true
            }

        }


        return false

    }

    fun handleParticles(amount: Float) {
        for (particle in ArrayList(particles)) {

            if (particle.state == AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.FadeIn) {
                particle.fadeIn -= 1 * amount

                var level = (particle.fadeIn - 0f) / (particle.maxFadeIn - 0f)
                particle.level = 1 - level

                if (particle.fadeIn < 0) {
                    particle.state = AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.Mid
                }
            }

            if (particle.state == AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.Mid) {
                particle.duration -= 1 * amount


                particle.level = 1f

                if (particle.duration < 0) {
                    particle.state = AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.FadeOut
                }
            }

            if (particle.state == AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.FadeOut) {
                particle.fadeOut -= 1 * amount

                particle.level = (particle.fadeOut - 0f) / (particle.maxFadeOut - 0f)

                if (particle.fadeOut < 0) {
                    particles.remove(particle)
                    continue
                }
            }

            particle.adjustmentInterval.advance(amount)
            if (particle.adjustmentInterval.intervalElapsed()) {
                var velocity = Vector2f(0f, 0f)
                particle.adjustment = MathUtils.getRandomNumberInRange(-1f, 1f)
            }

            particle.velocity = particle.velocity.rotate(particle.adjustment * amount)


            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)
        }
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