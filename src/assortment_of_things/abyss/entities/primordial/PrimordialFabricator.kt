package assortment_of_things.abyss.entities.primordial

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.graphics.util.Fader
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CustomCampaignEntityPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.fadeInOutAndExpire
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class PrimordialFabricator : BaseCustomEntityPlugin() {

    @Transient
    var sprite: SpriteAPI? = null

    var manager = AbyssUtils.getBiomeManager()
    var biome = manager.getBiome("primordial_waters") as PrimordialWaters
    var isInRange: Boolean? = null

    var fade = 0f

    override fun advance(amount: Float) {
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
                var indicator = ReflectionUtils.invoke("getIndicator", entity)
                var fader = ReflectionUtils.getFieldOfType(Fader::class.java, indicator!!) as Fader
                fader.durationIn = 0.75f //Base is 0.25
                entity.fadeInIndicator()
            } else {
                ReflectionUtils.invoke("setShowIconOnMap", entity.customEntitySpec, false)
                entity.addTag(Tags.NO_ENTITY_TOOLTIP)
                entity.addTag(Tags.NON_CLICKABLE)
                entity.forceOutIndicator()
                fade = 0f
            }
        }

       /* if (biome.getLevel() >= 0.5f) {
            entity.fadeInIndicator()
        }*/

        if (isInRange == true) {
            fade += 1f * amount
            fade = MathUtils.clamp(fade, 0f, 1f)

        }
    }

    override fun getRenderRange(): Float {
        return 10000f
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {

        if (entity == null) return

        if (sprite == null) {
            sprite = Global.getSettings().getAndLoadSprite("graphics/stations/rat_abyss_fabricator.png")
        }

        var level = biome.getLevel()

        if (level <= 0) return

        if (level > 0 && level < 1) biome.startStencil(false)

        //sprite!!.alphaMult = 0.5f
        sprite!!.color = biome.getSystemLightColor().darker().darker()
        sprite!!.angle = entity.facing
        sprite!!.setSize(99f, 81f)
        sprite!!.renderAtCenter(entity.location.x, entity.location.y)

        if (level > 0 && level < 1) biome.endStencil()
    }

}