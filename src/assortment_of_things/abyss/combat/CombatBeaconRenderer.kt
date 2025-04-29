package assortment_of_things.abyss.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.DynamicRingBand
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class CombatBeaconRenderer(var beacon: SectorEntityToken, var biomeLightColor: Color) : BaseCombatLayeredRenderingPlugin() {

    var color = AbyssUtils.ABYSS_COLOR.setAlpha(255)
    //var color = Color(0, 120, 255)

    var fader = FaderUtil(1f, 30f, MathUtils.getRandomNumberInRange(29f, 30f), false, false)

    var rotation = 0f
    var offset = Vector2f(0f, 0f)

    var station = Global.getSettings().getAndLoadSprite("graphics/stations/rat_abyss_beacon.png")
    var glow = Global.getSettings().getAndLoadSprite("graphics/stations/rat_abyss_beacon_glow.png")
    var halo = Global.getSettings().getSprite("rat_terrain", "halo")

    var rotSpeed = MathUtils.getRandomNumberInRange(1f, 1.5f)
    var facing = beacon.facing

    init {

        var playerfleet = Global.getSector().playerFleet
        var distance = MathUtils.getDistance(playerfleet, beacon)
        var angle = Misc.getAngleInDegrees(playerfleet.location, beacon.location)
        var plugin = beacon.customPlugin as AbyssalLight

        color = plugin.color

        var min = 0f
        var max = plugin.radius / 10

        var level = (distance - min) / (max - min)

        offset = MathUtils.getPointOnCircumference(Vector2f(0f , 0f), 600f * level, angle)

    }

    override fun advance(amount: Float) {
        fader.advance(amount)
        if (fader.brightness >= 1)
        {
            fader.fadeOut()
        }
        else if (fader.brightness <= 0)
        {
            fader.fadeIn()
        }

        facing += rotSpeed * amount
    }

    override fun getRenderRadius(): Float {
        return 100000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.PLANET_LAYER)
    }

    var baseRadius = 80f
    fun getRadius() : Float {
        var viewport = Global.getCombatEngine().viewport

        var plugin = beacon.customPlugin as AbyssalLight
        var min = 0f
        var max = plugin.radius / 10
        var playerfleet = Global.getSector().playerFleet
        var distance = MathUtils.getDistance(playerfleet, beacon)
        var level = (distance - min) / (max - min)

        var radius = baseRadius
        radius -= radius * 0.33f * level //Decrease in size further away from the core
        radius *= viewport.viewMult
        return radius
    }

    fun getLightRadius() : Float {
        var viewport = Global.getCombatEngine().viewport

        var plugin = beacon.customPlugin as AbyssalLight
        var min = 0f
        var max = plugin.radius / 10
        var playerfleet = Global.getSector().playerFleet
        var distance = MathUtils.getDistance(playerfleet, beacon)
        var level = (distance - min) / (max - min)

        var radius = baseRadius-60f
        radius += 60f * easeInOutSine(fader.brightness)
        radius -= radius * 0.33f * level //Decrease in size further away from the core
        radius *= viewport.viewMult
        return radius
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        var adjustedOffset = Vector2f(offset.x * viewport!!.viewMult, offset.y * viewport.viewMult)

        var playerXLevel = MathUtils.clamp(viewport.center.x.levelBetween(-Global.getCombatEngine().mapWidth/2, Global.getCombatEngine().mapWidth/2), 0f, 1f)
        var playerYLevel = MathUtils.clamp(viewport.center.y.levelBetween(-Global.getCombatEngine().mapHeight/2, Global.getCombatEngine().mapHeight/2), 0f, 1f)


        var playerXOff = 100f - 200f * playerXLevel
        var playerYOff = 100f - 200f * playerYLevel

        adjustedOffset = adjustedOffset.plus(Vector2f(playerXOff * viewport.viewMult, playerYOff * viewport.viewMult))

        var location = Vector2f()
        location = Vector2f((viewport!!.llx + viewport!!.visibleWidth / 2), (viewport!!.lly + viewport!!.visibleHeight / 2)).plus(adjustedOffset)



        var radius = getRadius()

        val var1: Float = radius * 0.45f
        val var2: Float = radius * 3.1415927f * 2.0f
        var var3 = var2 / 50.0f
        if (var3 < 3.0f) {
            var3 = 3.0f
        }

        if (var3 > 10.0f) {
            var3 = 10.0f
        }

        val var4: Float = radius * 1.25f
        var var5: Float = radius * 0.005f
        if (var5 > 0.5f) {
            var5 = 0.5f
        }


        station.angle = facing
        station.setSize(radius, radius)
        station.color = biomeLightColor
        station.renderAtCenter(location.x, location.y)


        var lightR = getLightRadius()

        halo!!.alphaMult = 1f
        halo!!.color = color.setAlpha(30)
        halo!!.setSize(lightR * 15, lightR * 15 )
        halo!!.setAdditiveBlend()
        halo!!.renderAtCenter(location.x, location.y)

        halo!!.alphaMult = 1f
        halo!!.color = color.setAlpha(30)
        halo!!.setSize(lightR * 2f, lightR * 2f )
        halo!!.setAdditiveBlend()
        halo!!.renderAtCenter(location.x, location.y)




        glow.angle = facing
        glow.setSize(radius, radius)
        glow.alphaMult = 0.02f  + (0.45f * fader.brightness)
        glow.color = color
        glow.setAdditiveBlend()
        glow.renderAtCenter(location.x, location.y)
    }

}