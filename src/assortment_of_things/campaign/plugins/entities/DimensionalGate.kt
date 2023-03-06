package assortment_of_things.campaign.plugins.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Items
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel
import com.fs.starfarer.api.impl.campaign.world.ZigLeashAssignmentAI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.*
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color


class DimensionalGate : BaseCustomEntityPlugin()
{

    var teleportLocation: SectorEntityToken? = null
    var active = false


    @Transient
    protected var baseSprite: SpriteAPI? = null

    @Transient
    protected var scannedGlow: SpriteAPI? = null

    @Transient
    protected var activeGlow: SpriteAPI? = null

    @Transient
    protected var whirl1: SpriteAPI? = null

    @Transient
    protected var whirl2: SpriteAPI? = null

    @Transient
    protected var starfield: SpriteAPI? = null

    @Transient
    protected var rays: SpriteAPI? = null

    @Transient
    protected var concentric: SpriteAPI? = null

    @Transient
    protected var warp: WarpingSpriteRendererUtil? = null

    protected var beingUsedFader: FaderUtil? = FaderUtil(0f, 1f, 1f, false, true)
    protected var glowFader: FaderUtil? = FaderUtil(0f, 1f, 1f, true, true)
    protected var madeActive = false
    protected var addedIntel = false
    protected var showBeingUsedDur = 0f

    protected var jitterColor: Color? = null
    protected var jitter: JitterUtil? = null
    protected var jitterFader: FaderUtil? = null

    protected var moteSpawn: IntervalUtil? = null

    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)
        readResolve()
    }


    fun readResolve(): Any? {
        scannedGlow = Global.getSettings().getSprite("gates", "glow_scanned")
        activeGlow = Global.getSettings().getSprite("gates", "glow_ring_active")
        concentric = Global.getSettings().getSprite("gates", "glow_concentric")
        rays = Global.getSettings().getSprite("gates", "glow_rays")
        whirl1 = Global.getSettings().getSprite("gates", "glow_whirl1")
        whirl2 = Global.getSettings().getSprite("gates", "glow_whirl2")
        starfield = Global.getSettings().getSprite("gates", "starfield")

        //warp = new WarpingSpriteRendererUtil(10, 10, 10f, 20f, 2f);
        if (beingUsedFader == null) {
            beingUsedFader = FaderUtil(0f, 1f, 1f, false, true)
        }
        if (glowFader == null) {
            glowFader = FaderUtil(0f, 1f, 1f, true, true)
            glowFader!!.fadeIn()
        }
        inUseAngle = 0f
        return this
    }

    fun jitter() {
        if (jitterFader == null) {
            jitterFader = FaderUtil(0f, 2f, 2f)
            jitterFader!!.isBounceDown = true
        }
        if (jitter == null) {
            jitter = JitterUtil()
            jitter!!.updateSeed()
        }
        jitterFader!!.fadeIn()
    }

    fun getJitterLevel(): Float {
        return if (jitterFader != null) jitterFader!!.brightness else 0f
    }

    fun isActive(): Boolean {
        return madeActive
    }

    fun showBeingUsed(dur: Float) {
        beingUsedFader!!.fadeIn()
        showBeingUsedDur = dur

//		if (withSound && entity.isInCurrentLocation()) {
//			Global.getSoundPlayer().playSound("gate_being_used", 1, 1, entity.getLocation(), entity.getVelocity());
//		}
    }

    fun getProximitySoundFactor(): Float {
        val player = Global.getSector().playerFleet
        var dist = Misc.getDistance(player.location, entity.location)
        val radSum = entity.radius + player.radius

        //if (dist < radSum) return 1f;
        dist -= radSum
        var f = 1f
        if (dist > 300f) {
            f = 1f - (dist - 300f) / 100f
        }

        //float f = 1f - dist / 300f;
        if (f < 0) f = 0f
        if (f > 1) f = 1f
        return f
    }

    protected var inUseAngle = 0f
    override fun advance(amount: Float) {
        if (showBeingUsedDur > 0 || !beingUsedFader!!.isIdle) {
            showBeingUsedDur -= amount
            if (showBeingUsedDur > 0) {
                beingUsedFader!!.fadeIn()
            } else {
                showBeingUsedDur = 0f
            }
            inUseAngle += amount * 60f
            if (warp != null) {
                warp!!.advance(amount)
            }
        }
        glowFader!!.advance(amount)

//
        if (jitterFader != null) {
            jitterFader!!.advance(amount)
            if (jitterFader!!.isFadedOut) {
                jitterFader = null
            }
        }

        beingUsedFader!!.advance(amount)
    }

    override fun getRenderRange(): Float {
        return entity.radius + 500f
    }

    override fun createMapTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        val color: Color = entity.faction.baseUIColor

        tooltip.addPara("Strange Gate", Misc.getGrayColor(), 3f)
    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }

    override fun appendToCampaignTooltip(tooltip: TooltipMakerAPI, level: VisibilityLevel?) {

    }

    @Transient
    protected var scaledSprites = false
    protected fun scaleGlowSprites() {
        if (scaledSprites) return
        val spec = entity.customEntitySpec
        if (spec != null) {
            baseSprite = Global.getSettings().getSprite(spec.spriteName)
            baseSprite!!.setSize(spec.spriteWidth, spec.spriteHeight)
            scaledSprites = true
            val scale = spec.spriteWidth / Global.getSettings().getSprite(spec.spriteName).width
            scannedGlow!!.setSize(scannedGlow!!.width * scale, scannedGlow!!.height * scale)
            activeGlow!!.setSize(activeGlow!!.width * scale, activeGlow!!.height * scale)
            rays!!.setSize(rays!!.width * scale, rays!!.height * scale)
            whirl1!!.setSize(whirl1!!.width * scale, whirl1!!.height * scale)
            whirl2!!.setSize(whirl2!!.width * scale, whirl2!!.height * scale)
            starfield!!.setSize(starfield!!.width * scale, starfield!!.height * scale)
            concentric!!.setSize(concentric!!.width * scale, concentric!!.height * scale)
        }
    }


    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {

        if (layer == CampaignEngineLayers.STATIONS) {
            var alphaMult = viewport.alphaMult
            alphaMult *= entity.sensorFaderBrightness
            alphaMult *= entity.sensorContactFaderBrightness
            if (alphaMult <= 0f) return
            val spec = entity.customEntitySpec ?: return
            val w = spec.spriteWidth
            val h = spec.spriteHeight
            val scale = spec.spriteWidth / Global.getSettings().getSprite(spec.spriteName).width
            val loc = entity.location
            var scannedGlowColor = Color(255, 200, 0, 255)
            var activeGlowColor = Color(200, 50, 255, 255)
            scannedGlowColor = Color.white
            activeGlowColor = Color.white
            var glowAlpha = 1f
            val glowMod1 = 0.5f + 0.5f * glowFader!!.brightness
            val glowMod2 = 0.75f + 0.25f * glowFader!!.brightness
            val beingUsed = !beingUsedFader!!.isFadedOut
            scaleGlowSprites()
            if (jitterFader != null && jitter != null) {
                var c: Color? = jitterColor
                if (c == null) c = Color(255, 255, 255, 255)
                baseSprite!!.color = c
                baseSprite!!.alphaMult = alphaMult * jitterFader!!.brightness
                baseSprite!!.setAdditiveBlend()
                jitter!!.render(baseSprite, loc.x, loc.y, 30f * jitterFader!!.brightness, 10)
                baseSprite!!.renderAtCenter(loc.x, loc.y)
            }

            if (beingUsed) {

                glowAlpha *= beingUsedFader!!.brightness
                var angle: Float
                rays!!.alphaMult = alphaMult * glowAlpha
                rays!!.setAdditiveBlend()
                rays!!.renderAtCenter(loc.x + 1.5f, loc.y)
                concentric!!.alphaMult = alphaMult * glowAlpha * 1f
                concentric!!.setAdditiveBlend()
                concentric!!.renderAtCenter(loc.x + 1.5f, loc.y)
               // concentric!!.color = Color(255, 0, 0)
                angle = -inUseAngle * 0.25f
                angle = Misc.normalizeAngle(angle)
                whirl1!!.angle = angle
                whirl1!!.alphaMult = alphaMult * glowAlpha
                whirl1!!.setAdditiveBlend()
                whirl1!!.renderAtCenter(loc.x + 1.5f, loc.y)
               // whirl1!!.color = Color(255, 0, 0)
                angle = -inUseAngle * 0.33f
                angle = Misc.normalizeAngle(angle)
                whirl2!!.angle = angle
                whirl2!!.alphaMult = alphaMult * glowAlpha * 0.5f
                whirl2!!.setAdditiveBlend()
                whirl2!!.renderAtCenter(loc.x + 1.5f, loc.y)
               // whirl2!!.color = Color(255, 0, 0)
            }
        }
    }
}