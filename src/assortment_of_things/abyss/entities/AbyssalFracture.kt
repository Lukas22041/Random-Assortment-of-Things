package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.misc.getAndLoadSprite
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
    var colorOverride: Color? = null

    @Transient
    var center: SpriteAPI? = null

    @Transient
    var whirl: SpriteAPI? = null

    @Transient
    var halo: SpriteAPI? = null

    @Transient
    var band1: DynamicRingBand? = null

    @Transient
    var band2: DynamicRingBand? = null

    var angle = 0f

    init {
        initBandIfNull()
    }


    override fun advance(amount: Float) {
        super.advance(amount)

        initBandIfNull()

        band1!!.advance(amount)
        band2!!.advance(amount)

        if (!Global.getSector().isPaused) {
            angle += 10f * amount
            if (angle >= 360) {
                angle = 0f
            }
        }



    }



    fun initBandIfNull()
    {
        if (band1 != null && center != null) return
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

        //center = Global.getSettings().getSprite("gates", "starfield")
        center = Global.getSettings().getSprite("warroom", "icon_explosion")
        whirl = Global.getSettings().getAndLoadSprite("graphics/fx/rat_fracture_whirl.png")
        halo = Global.getSettings().getSprite("rat_terrain", "halo")
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        initBandIfNull()

        if (layer == CampaignEngineLayers.TERRAIN_SLIPSTREAM)
        {
            center!!.setSize(170f, 170f)
            center!!.color = Color(0, 0, 0)
            center!!.renderAtCenter(entity.location.x, entity.location.y)

            var color = AbyssUtils.ABYSS_COLOR.darker().darker().setAlpha(75)

            if (connectedEntity != null && !connectedEntity!!.containingLocation.isHyperspace) {
                var destinationData = AbyssUtils.getSystemData(connectedEntity!!.starSystem)

                if (destinationData.depth == AbyssDepth.Deep) {
                    whirl!!.setSize(170f, 170f)
                    whirl!!.color = color.setAlpha(255)
                    whirl!!.angle = -angle
                    whirl!!.alphaMult = 0.3f
                    whirl!!.setNormalBlend()
                    whirl!!.renderAtCenter(entity.location.x, entity.location.y)
                }
            }


            /* band1!!.color = AbyssUtils.ABYSS_COLOR.setAlpha(100)
             band2!!.color = AbyssUtils.ABYSS_COLOR.setAlpha(200)*/

            if (!entity.containingLocation.isHyperspace) color = AbyssUtils.getSystemData(entity.starSystem).getDarkColor().setAlpha(75)
            if (colorOverride != null) color = colorOverride!!

            band1!!.color = color.setAlpha(100)
            band2!!.color = color.setAlpha(200)

            if (entity.containingLocation.isHyperspace) {
                band1!!.color = color.setAlpha(75)
                band2!!.color = color.setAlpha(150)
            }

            band1!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)
            band2!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)
        }
    }

    override fun appendToCampaignTooltip(tooltip: TooltipMakerAPI?, level: SectorEntityToken.VisibilityLevel?) {
        tooltip!!.addSpacer(10f)
        var system = AbyssProcgen.getConnectedFracture(entity).containingLocation

        if (entity.containingLocation.isHyperspace) {
            tooltip.addPara("It leads towards the ever descending abyssal depths.", 0f)
        }
        else if (system.isHyperspace) {
            tooltip!!.addPara("It connects towards ${system.nameWithNoType}.", 0f, Misc.getTextColor(),
                Color(0, 100, 255), "")
        }
        else if (system.lastPlayerVisitTimestamp == 0L)
        {
            tooltip!!.addPara("It is unknown where in the abyss it connects to.", 0f, Misc.getTextColor(),
                AbyssUtils.ABYSS_COLOR, "Zone", "abyss")
        }
        else
        {
            tooltip!!.addPara("It connects towards the ${system.nameWithNoType}.", 0f, Misc.getTextColor(),
                AbyssUtils.ABYSS_COLOR, "${system.nameWithNoType}")
        }

    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }



    override fun createMapTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createMapTooltip(tooltip, expanded)

        var system = AbyssProcgen.getConnectedFracture(entity).containingLocation

        if (system.lastPlayerVisitTimestamp == 0L)
        {
            tooltip!!.addPara("Abyssal Fracture", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "Abyssal Fracture")
        }
        else
        {
            tooltip!!.addPara("Abyssal Fracture (${system.nameWithNoType})", 0f, Misc.getTextColor(),
                AbyssUtils.ABYSS_COLOR, "Abyssal Fracture")
        }
    }



}