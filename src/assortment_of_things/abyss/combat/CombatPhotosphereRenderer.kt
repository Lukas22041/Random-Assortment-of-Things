package assortment_of_things.abyss.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.DynamicRingBand
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class CombatPhotosphereRenderer(var photosphere: SectorEntityToken) : BaseCombatLayeredRenderingPlugin() {

    var color = AbyssUtils.ABYSS_COLOR.setAlpha(255)
    //var color = Color(0, 120, 255)

    @Transient
    var halo: SpriteAPI? = null

    @Transient
    var band1: DynamicRingBand? = null

    @Transient
    var center: SpriteAPI? = null

    var rotation = 0f
    var offset = Vector2f(0f, 0f)

    init {

        var playerfleet = Global.getSector().playerFleet
        var distance = MathUtils.getDistance(playerfleet, photosphere)
        var angle = Misc.getAngleInDegrees(playerfleet.location, photosphere.location)
        var plugin = photosphere.customPlugin as AbyssalLight

        color = plugin.color

        var min = 0f
        var max = plugin.radius / 10

        var level = (distance - min) / (max - min)

        offset = MathUtils.getPointOnCircumference(Vector2f(0f , 0f), 600f * level, angle)

        halo = Global.getSettings().getSprite("rat_terrain", "halo")

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

        band1 = DynamicRingBand("rat_terrain", "wormhole_bands", 64.0f, 3, Color.white, var4,  var3,  var1 + radius * 0.25f - var4 * 0.05f, 10.0f, var5, 100.0f, 10.0f, true)

        var centerPath = "graphics/fx/rat_center.png"
        Global.getSettings().loadTexture(centerPath)
        center = Global.getSettings().getSprite(centerPath)
    }

    override fun advance(amount: Float) {
        band1!!.advance(amount * 0.75f)
    }

    override fun getRenderRadius(): Float {
        return 100000f
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS)
    }


    fun getRadius() : Float {
        var viewport = Global.getCombatEngine().viewport

        var plugin = photosphere.customPlugin as AbyssalLight
        var min = 0f
        var max = plugin.radius / 10
        var playerfleet = Global.getSector().playerFleet
        var distance = MathUtils.getDistance(playerfleet, photosphere)
        var level = (distance - min) / (max - min)

        var radius = 60f
        radius -= radius * 0.33f * level //Decrease in size further away from the core
        radius *= viewport.viewMult
        return radius
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        var adjustedOffset = Vector2f(offset.x * viewport!!.viewMult, offset.y * viewport.viewMult)





        var playerXLevel = MathUtils.clamp(viewport.center.x.levelBetween(-Global.getCombatEngine().mapWidth/2, Global.getCombatEngine().mapWidth/2), 0f, 1f)
        var playerYLevel = MathUtils.clamp(viewport.center.y.levelBetween(-Global.getCombatEngine().mapHeight/2, Global.getCombatEngine().mapHeight/2), 0f, 1f)


        var playerXOff = 150f - 300f * playerXLevel
        var playerYOff = 150f - 300f * playerYLevel

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

        band1!!.bandWidthInEngine = var4
        band1!!.pixelsPerSegment = var3
        band1!!.innerRadius = var1 + radius * 0.25f - var4 * 0.05f
        band1!!.angularVelocity = 10.0f
        band1!!.maxFluctuation = var5
        band1!!.fluctuationAngVel = 100.0f
        band1!!.fluctuationAngleMult = 10.0f






        center!!.setSize(radius * 1.8f , radius  * 1.8f)
        center!!.color = color.setAlpha(130)
        center!!.renderAtCenter(location.x, location.y)

        band1!!.isAdditiveBlend = false
        band1!!.color = color.setAlpha(45)
        band1!!.render(location.x, location.y, viewport!!.alphaMult)


        halo!!.alphaMult = 1f
        halo!!.color = color.setAlpha(30)
        halo!!.setSize(radius * 15, radius * 15 )
        halo!!.setAdditiveBlend()
        halo!!.renderAtCenter(location.x, location.y)


    }

}