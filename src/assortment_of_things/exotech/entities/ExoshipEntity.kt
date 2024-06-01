package assortment_of_things.exotech.entities

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.impl.campaign.entities.GateHaulerEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.CampaignEngineGlowIndividualEngine
import com.fs.starfarer.api.util.CampaignEngineGlowUtil
import com.fs.starfarer.api.util.CampaignEntityMovementUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ExoshipEntity : BaseCustomEntityPlugin() {

    @Transient
    var glow: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_ext_lights.png")
    @Transient
    var lights: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_lights.png")

    @Transient
    var jitter: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_jitter.png")

    var lastJitterLocations = ArrayList<Vector2f>()

    // implements EngineGlowControls {
    var MAX_SPEED = 1000f
    var ACCELERATION = 15f
    var TURN_ACCELERATION = 2.5f
    var MAX_TURNRATE = 18f

    lateinit var movement: CampaignEntityMovementUtil
    lateinit var engineGlow: CampaignEngineGlowUtil
    var longBurn = false

    var isActivated = true
    var isActivating = false
    var isInTransit = false
    var getRemainingActivationDays = 0f

    var delay = 8f
    var active = false

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)

        val fringe = ExoUtils.color1
        val flame = ExoUtils.color2
        val core = Color(255, 255, 255, 255)

        engineGlow = CampaignEngineGlowUtil(entity, fringe, core, flame, 0.25f)

        val mainEngine = CampaignEngineGlowIndividualEngine(90f, 75f, 25f, 100f, Vector2f(-52f, 0f), engineGlow)
        mainEngine.flameTexSpanMult = 0.5f
        engineGlow!!.addEngine(mainEngine)

        val engineLeft1 = CampaignEngineGlowIndividualEngine(90f, 55f, 15f, 40f, Vector2f(-40f, 17f), engineGlow)
        engineLeft1.flameTexSpanMult = 0.5f
        engineGlow!!.addEngine(engineLeft1)

        val engineLeft2 = CampaignEngineGlowIndividualEngine(90f, 50f, 15f, 40f, Vector2f(-38f, 21f), engineGlow)
        engineLeft2.flameTexSpanMult = 0.5f
        engineGlow!!.addEngine(engineLeft2)

        val engineRight1 = CampaignEngineGlowIndividualEngine(90f, 55f, 15f, 40f, Vector2f(-38f, -17f), engineGlow)
        engineRight1.flameTexSpanMult = 0.5f
        engineGlow!!.addEngine(engineRight1)

        val engineRight2 = CampaignEngineGlowIndividualEngine(90f, 50f, 15f, 40f, Vector2f(-38f, -21f), engineGlow)
        engineRight2.flameTexSpanMult = 0.5f
        engineGlow!!.addEngine(engineRight2)

        movement = CampaignEntityMovementUtil(entity, TURN_ACCELERATION, MAX_TURNRATE, ACCELERATION, MAX_SPEED)
        movement.engineGlow = engineGlow

        /*entity!!.orbit = null
        entity!!.velocity.set(Vector2f())
        movement.moveToLocation(Vector2f(0f, 0f))*/


    }

    override fun getRenderRange(): Float {
        return 100000000f
    }

    override fun advance(amount: Float) {

        delay -= amount
        if (delay <= 0 && !active) {
            active = true

            movement.moveInDirection(45f)
            movement.setFaceInOppositeDirection(false)
            movement.setTurnThenAccelerate(true)
            longBurn = true

            isInTransit = true
        }

        //Handle Movement
        if (entity.isInCurrentLocation || isInTransit) {
            engineGlow.advance(amount)
            var soundVolume = engineGlow.lengthMult.curr * 0.5f
            if (soundVolume > 0.5f) soundVolume = 0.5f

            if (longBurn && movement.isDesiredFacingSet) {
                val angleDiff = Misc.getAngleDiff(movement.desiredFacing, entity.facing)
                if (angleDiff < 2f) {
                    val dir = Misc.getUnitVectorAtDegreeAngle(movement.desiredFacing)
                    var speedInDesiredDir = Vector2f.dot(dir, entity.velocity)
                    if (movement.isFaceInOppositeDirection) {
                        speedInDesiredDir *= -1f
                    }
                    val speed = entity.velocity.length()
                    if (speedInDesiredDir > 10f && speedInDesiredDir > speed * 0.7f) {
                        val speedForMaxEngineLength = 100f
                        var f = speedInDesiredDir / speedForMaxEngineLength
                        if (f < 0f) f = 0f
                        if (f > 1f) f = 1f
                        soundVolume = Math.min(soundVolume + f * 0.5f, 1f)

                        //System.out.println("longBurn factor: " + f);
                        val flickerZone = 0.5f
                        if (f < flickerZone) {
                            engineGlow.flickerRateMult.shift(this, 5f, 0f, 0.1f, 1f)
                            engineGlow.flickerMult.shift(this, 0.33f - 0.33f * f / flickerZone, 0f, 0.1f, 1f)
                        }
                        engineGlow.glowMult.shift(this, 2f, 1f, 1f, f)
                        engineGlow.lengthMult.shift(this, 5f, 1f, 1f, f)
                        engineGlow.widthMult.shift(this, 3f, 1f, 1f, f)
                    }
                }
            }

            if (soundVolume > 0) {
                if (entity.isInCurrentLocation && entity.isVisibleToPlayerFleet) {
                    Global.getSoundPlayer()
                        .playLoop("gate_hauler_engine_loop", entity, 1f, soundVolume, entity.location, entity.velocity)
                }
            }
        }

        movement.advance(amount)

    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (glow == null) {
            glow = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_ext_lights.png")
            lights = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_lights.png")
            jitter = Global.getSettings().getAndLoadSprite("graphics/stations/rat_exoship_jitter.png")
        }

        glow!!.alphaMult = 1f
        glow!!.angle = entity.facing - 90
        glow!!.setSize(95f, 140f)
        glow!!.renderAtCenter(entity.location.x, entity.location.y)

        lights!!.alphaMult = 1f
        lights!!.angle = entity.facing - 90
        lights!!.setSize(95f, 140f)
        lights!!.renderAtCenter(entity.location.x, entity.location.y)

        var data = ExoUtils.getExoshipData(entity)

        /*if (data.state != ExoShipData.State.Idle && data.moveLevel >= 0.7f) {
            var level = (data.moveLevel -0.7f) * 3
            doJitter(jitter!!, level * level, lastJitterLocations, 15, 40f * level)
        }*/

        var alphaMult = viewport!!.alphaMult
        alphaMult *= entity.sensorFaderBrightness
        alphaMult *= entity.sensorContactFaderBrightness
        if (alphaMult <= 0f) return

        engineGlow.render(alphaMult)
    }

    fun doJitter(sprite: SpriteAPI, level: Float, lastLocations: ArrayList<Vector2f>, jitterCount: Int, jitterMaxRange: Float) {

        var paused = Global.getSector().isPaused
        /*   var jitterCount = 5
           var jitterMaxRange = 2f*/
        var jitterAlpha = 0.1f


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
            sprite.angle = entity.facing - 90
            sprite!!.setSize(95f, 140f)
            sprite.renderAtCenter(entity.location.x + jitterLoc.x, entity.location.y + jitterLoc.y)
        }
    }

    override fun appendToCampaignTooltip(tooltip: TooltipMakerAPI?, level: SectorEntityToken.VisibilityLevel?) {

        var exoData = ExoUtils.getExoData()
        var shipData = ExoUtils.getExoshipData(entity)

        if (exoData.hasPartnership) {
            tooltip!!.addSpacer(10f)

            var days = shipData.getTimeTilNextMove().toInt()

            tooltip!!.addPara("The ship is preparing to move towards its next location in $days days", 0f,
                Misc.getGrayColor(), Misc.getHighlightColor(), "$days")
        }
    }


}