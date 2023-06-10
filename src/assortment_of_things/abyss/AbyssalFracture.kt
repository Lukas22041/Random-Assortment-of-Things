package assortment_of_things.abyss

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.DynamicRingBand
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class AbyssalFracture : BaseCustomEntityPlugin() {

    var connectedEntity: SectorEntityToken? = null
    val radius = 100.0f

    @Transient
    var center: SpriteAPI? = null

    @Transient
    var band1: DynamicRingBand? = null

    @Transient
    var band2: DynamicRingBand? = null

    init {
        initBandIfNull()

    }

    override fun advance(amount: Float) {
        super.advance(amount)

        initBandIfNull()


       /* var var17: Float = this.fader.getBrightness()
        var17 *= var17
        val var18: Float = this.getScale()
        val var19 = 1.0f
        val var22: Iterator<*> = this.bands.iterator()

        while (var22.hasNext()) {
            val var20 = var22.next() as DynamicRingBand
            var20.advance(var1)
        }

        val var21: Float = this.getRadius() * 0.45f
        var6 = this.getRadius() * 1.25f
        var var8: Float = this.getRadius() * 0.01f
        val var9: Float = this.getRadius() * 3.1415927f * 2.0f * var18
        var var10 = var9 / 50.0f
        if (var10 < 3.0f) {
            var10 = 3.0f
        }

        if (var10 > 10.0f) {
            var10 = 10.0f
        }

        if (var8 > 0.5f) {
            var8 = 0.5f
        }

        if (var17 == 0.0f) {
            var8 = 35.0f
        }*/


        band1!!.advance(amount)
        band2!!.advance(amount)

       /* band1!!.bandWidthInEngine =
            var6 * 0.3f + 20.0f + 0.6f * var6 / var18 + 20.0f * (1.0f - var17) / var18
        band1!!.innerRadius = var21 + this.getRadius() * 0.25f - var6 * 0.05f
        band1!!.pixelsPerSegment = var10
        band1!!.maxFluctuation = var8

        band2!!.bandWidthInEngine =
            var6 * 0.3f + 20.0f + 0.6f * var6 / var18 + 20.0f * (1.0f - var17) / var18
        band2!!.innerRadius = var21 + this.getRadius() * 0.25f - var6 * 0.05f
        band2!!.pixelsPerSegment = var10
        band2!!.maxFluctuation = var8*/
    }



    fun initBandIfNull()
    {
        if (band1 != null) return
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

        band1 = DynamicRingBand("rat_terrain", "wormhole_bands", 64.0f, 2, Color.white, var4,  var3,  var1 + radius * 0.25f - var4 * 0.05f, 5.0f, var5, 100.0f, 10.0f, true)
        band2 = DynamicRingBand("rat_terrain", "wormhole_bands", 64.0f, 2, Color.white, var4,  var3,  var1 + radius * 0.25f - var4 * 0.05f, -5.0f, var5, 100.0f, 10.0f, true)

        center = Global.getSettings().getSprite("warroom", "icon_explosion")
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        center!!.setSize(160f, 160f)
        center!!.color = Color(0, 0, 0)
        center!!.renderAtCenter(entity.location.x, entity.location.y)

        band1!!.color = AbyssUtils.ABYSS_COLOR.setAlpha(100)
        band2!!.color = AbyssUtils.ABYSS_COLOR.setAlpha(200)

        band1!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)
        band2!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)


    }

    override fun appendToCampaignTooltip(tooltip: TooltipMakerAPI?, level: SectorEntityToken.VisibilityLevel?) {
        tooltip!!.addSpacer(10f)
        var system = AbyssUtils.getConnectedFracture(entity).containingLocation
        if (system.lastPlayerVisitTimestamp == 0L)
        {
            tooltip!!.addPara("It is unknown where in the abyss it connects to.", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "Zone", "abyss")
        }
        else
        {
            tooltip!!.addPara("It connects towards the ${system.nameWithNoType} Zone within the abyss.", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "${system.nameWithNoType} Zone", "abyss")
        }

    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }

    override fun createMapTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createMapTooltip(tooltip, expanded)

        var system = AbyssUtils.getConnectedFracture(entity).containingLocation

        if (system.lastPlayerVisitTimestamp == 0L)
        {
            tooltip!!.addPara("Abyssal Fracture", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "Abyssal Fracture")
        }
        else
        {
            tooltip!!.addPara("Abyssal Fracture (${system.nameWithNoType} Zone)", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "Abyssal Fracture")
        }
    }



}