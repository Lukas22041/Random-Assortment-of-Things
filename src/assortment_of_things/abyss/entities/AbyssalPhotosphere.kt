package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.DynamicRingBand
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class AbyssalPhotosphere : BaseCustomEntityPlugin(), AbyssalLight {

    override var radius = 20000f
    override var color = AbyssUtils.ABYSS_COLOR.setAlpha(50)

    @Transient
    var halo: SpriteAPI? = null

    @Transient
    var band1: DynamicRingBand? = null

    @Transient
    var center: SpriteAPI? = null

    var rotation = 0f

    override fun advance(amount: Float) {
        super.advance(amount)

        if (entity == null) return
        initSpritesIfNull()
        band1!!.advance(amount)
    }

    fun initSpritesIfNull()
    {

        var radius = entity.radius
        if (band1 != null && halo != null) return

        color = AbyssUtils.getSystemData(entity.starSystem).getColor()
        var overwrite = entity.memoryWithoutUpdate.get("\$rat_photosphere_color_overwrite") as Color?
        if (overwrite != null) {
            color = overwrite
            entity.customEntitySpec
        }
        halo = Global.getSettings().getSprite("rat_terrain", "halo")

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

    override fun getRenderRange(): Float {
        return super.getRenderRange() + radius * 1.1f
    }



    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        initSpritesIfNull()

        if (band1 == null) return

        var posX = entity.location.x
        var posY = entity.location.y

        if (!viewport!!.isNearViewport(entity.location, radius)) return

        if (layer == CampaignEngineLayers.TERRAIN_7A)
        {

            band1!!.color = color.setAlpha(125)
            center!!.color = color.setAlpha(255)

            center!!.setSize(entity.radius * 1.8f , entity.radius  * 1.8f)
            center!!.renderAtCenter(entity.location.x, entity.location.y)

            band1!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)
        }

        if (layer == CampaignEngineLayers.ABOVE)
        {

            halo!!.alphaMult = 1f
            halo!!.color = color.setAlpha(75)

            halo!!.setSize(radius / 20, radius / 20)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(entity.location.x, entity.location.y)

            halo!!.alphaMult = 1f
            halo!!.color = color.setAlpha(55)

            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(entity.location.x, entity.location.y)
        }
    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }

    override fun createMapTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createMapTooltip(tooltip, expanded)

        tooltip!!.addPara("Photosphere", 0f, Misc.getTextColor(), color, "Photosphere")

    }
}