package assortment_of_things.abyss.entities.primordial

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.DynamicRingBand
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class PrimordialPhotosphere : BaseCustomEntityPlugin(), AbyssalLight {

    override var radius = 20000f
    override var color = AbyssUtils.ABYSS_COLOR.setAlpha(50)
    override var lightColor = color

    @Transient
    var halo: SpriteAPI? = null

    @Transient
    var band1: DynamicRingBand? = null

    @Transient
    var center: SpriteAPI? = null

    var rotation = 0f

    var manager = AbyssUtils.getBiomeManager()
    var biome = manager.getBiome("primordial_waters") as PrimordialWaters

    @Transient
    var isInRange: Boolean? = null

    override fun advance(amount: Float) {
        super.advance(amount)

        if (entity == null) return
        initSpritesIfNull()
        band1!!.advance(amount)

        var isNowInrange = false
        if (MathUtils.getDistance(entity.location, biome.getStencilCenter()) < biome.getRadius()) {
            isNowInrange = true
        }

        if (biome.getLevel() <= 0) isNowInrange = false

        //Only call if a change actually happened to avoid spamming reflection code
        if (isInRange != isNowInrange) {
            isInRange = isNowInrange

            if (isInRange!!) {
                ReflectionUtils.invoke("setShowIconOnMap", entity.customEntitySpec, true)
                entity.removeTag(Tags.NO_ENTITY_TOOLTIP)
                entity.removeTag(Tags.NON_CLICKABLE)
            } else {
                ReflectionUtils.invoke("setShowIconOnMap", entity.customEntitySpec, false)
                entity.addTag(Tags.NO_ENTITY_TOOLTIP)
                entity.addTag(Tags.NON_CLICKABLE)
            }
        }
    }

    fun initSpritesIfNull()
    {

        var radius = entity.radius
        if (band1 != null && halo != null) return

        color = AbyssUtils.getBiomeManager().getCell(entity).getBiome()?.getBiomeColor()?.darker()?.setAlpha(255) ?: AbyssUtils.ABYSS_COLOR
        lightColor = color

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
        center = Global.getSettings().getAndLoadSprite(centerPath)

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

        lightColor = color

        var level = biome.getLevel()

        if (level <= 0) return

        if (level > 0 && level < 1) biome.startStencil(false)

        if (layer == CampaignEngineLayers.TERRAIN_7A)
        {

            band1!!.color = color.setAlpha(125).darker()
            center!!.color = color.setAlpha(255).darker()

            center!!.setSize(entity.radius * 1.8f , entity.radius  * 1.8f)
            center!!.renderAtCenter(entity.location.x, entity.location.y)

            band1!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)
        }

        if (layer == CampaignEngineLayers.ABOVE)
        {

            halo!!.alphaMult = 1f
            halo!!.color = color.setAlpha(95)

            halo!!.setSize(radius / 20, radius / 20)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(entity.location.x, entity.location.y)

            halo!!.alphaMult = 1f
            halo!!.color = color.setAlpha(75)

            halo!!.setSize(radius / 2, radius / 2)
            halo!!.setAdditiveBlend()
            halo!!.renderAtCenter(entity.location.x, entity.location.y)
        }

        if (level > 0 && level < 1) biome.endStencil()

    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }

    override fun createMapTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createMapTooltip(tooltip, expanded)

        tooltip!!.addPara("Photosphere", 0f, Misc.getTextColor(), color, "Photosphere")

    }
}