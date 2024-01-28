package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.util.IntervalUtil
import java.awt.Color
import java.util.*

class AbyssFinalSystemAmbience : BaseCustomEntityPlugin() {


    var randomRotation = Random().nextFloat() > 0.5

    @Transient
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    @Transient
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    @Transient
    var wormhole3 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    override fun advance(amount: Float) {
        super.advance(amount)

        if (randomRotation)
        {
            entity.facing += 0.015f
        }
        else
        {
            entity.facing -= 0.015f
        }
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        if (wormhole == null) {
            wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
            wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
            wormhole3 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
        }

        if (layer == CampaignEngineLayers.TERRAIN_7) {
            wormhole.setSize(6000f, 6000f)
            wormhole.setAdditiveBlend()
            wormhole.alphaMult = 0.15f
            if (!Global.getSector().isPaused) wormhole.angle += 0.05f
            wormhole.color = Color(255, 0, 100)
            wormhole.renderAtCenter(entity.location.x, entity.location.y)

            wormhole2.setSize(12000f, 12000f)
            wormhole2.setAdditiveBlend()
            wormhole2.alphaMult = 0.15f
            if (!Global.getSector().isPaused) wormhole2.angle += 0.025f
            wormhole2.color = Color(255, 0, 100)
            wormhole2.renderAtCenter(entity.location.x, entity.location.y)

            wormhole3.setSize(18000f, 18000f)
            wormhole3.setAdditiveBlend()
            wormhole3.alphaMult = 0.15f
            if (!Global.getSector().isPaused) wormhole2.angle += 0.030f
            wormhole3.color = Color(255, 0, 100)
            wormhole3.renderAtCenter(entity.location.x, entity.location.y)
        }
    }

    override fun getRenderRange(): Float {
        return 1000000f
    }
}