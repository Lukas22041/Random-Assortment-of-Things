package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.DynamicRingBand
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class AbyssalBeacon : BaseCustomEntityPlugin(), AbyssalLight {

    var baseRadius = 3000f
    var extraRadius = 12000f
    override var radius = baseRadius + extraRadius
    override var color = AbyssUtils.ABYSS_COLOR.setAlpha(50)

    var fader = FaderUtil(1f, 15f, MathUtils.getRandomNumberInRange(14f, 15f), false, false)

    @Transient
    var halo: SpriteAPI? = null

    @Transient
    var glow: SpriteAPI? = null

    var rotationSpeed = MathUtils.getRandomNumberInRange(0.015f, 0.020f)

    override fun advance(amount: Float) {
        super.advance(amount)

        if (entity == null) return
        initSpritesIfNull()

        fader.advance(amount)
        if (fader.brightness >= 1)
        {
            fader.fadeOut()
        }
        else if (fader.brightness <= 0)
        {
            fader.fadeIn()
        }

        var extra = extraRadius * easeInOutSine(fader.brightness)
        radius = baseRadius + extra

        entity.facing += rotationSpeed
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }



    fun initSpritesIfNull()
    {

        var radius = entity.radius
        if (halo != null && glow != null) return

        color = AbyssUtils.getSystemData(entity.starSystem).getColor()
        halo = Global.getSettings().getSprite("rat_terrain", "halo")
        glow = Global.getSettings().getAndLoadSprite("graphics/stations/rat_abyss_beacon_glow.png")
    }

    override fun getRenderRange(): Float {
        return super.getRenderRange() + radius * 1.1f
    }



    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)


        initSpritesIfNull()

        if (layer == CampaignEngineLayers.ABOVE)
        {


            halo!!.alphaMult = 0.3f
            halo!!.color = color.setAlpha(75)

            halo!!.setSize(radius / 20, radius / 20)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(entity.location.x, entity.location.y)

            halo!!.alphaMult = 0.7f
            halo!!.color = color.setAlpha(55)

            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(entity.location.x, entity.location.y)


            glow!!.color = color
            glow!!.alphaMult = 0.02f  + (0.15f * fader.brightness)
            glow!!.setAdditiveBlend()
            glow!!.setSize(entity.customEntitySpec.spriteWidth + 20, entity.customEntitySpec.spriteHeight)
            glow!!.angle = entity.facing + 90
            glow!!.renderAtCenter(entity.location.x, entity.location.y)
        }
    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }

    override fun createMapTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createMapTooltip(tooltip, expanded)

        tooltip!!.addPara("Abyssal Beacon", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "Abyssal Beacon")

    }
}