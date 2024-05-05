package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class AbyssalLightsource : BaseCustomEntityPlugin(), AbyssalLight {

    override var radius = 2000f
    override var color = AbyssUtils.ABYSS_COLOR.setAlpha(50)

    @Transient
    var halo: SpriteAPI? = null


    override fun advance(amount: Float) {
        super.advance(amount)
        initSpritesIfNull()
    }

    fun initSpritesIfNull()
    {
        if (halo == null) {
            if (entity.starSystem != null) {
                color = AbyssUtils.getSystemData(entity.starSystem).getColor().setAlpha(50)
            }
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }
    }

    override fun getRenderRange(): Float {
        return super.getRenderRange() + radius * 1.1f
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        initSpritesIfNull()

        if (halo == null) {
            halo = Global.getSettings().getSprite("rat_terrain", "halo")
        }

        if (layer == CampaignEngineLayers.ABOVE)
        {
            halo!!.alphaMult = 1f
            halo!!.color = color
            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(entity.location.x, entity.location.y)
        }
    }
}