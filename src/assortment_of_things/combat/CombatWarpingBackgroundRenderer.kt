package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.getAndLoadSprite
import com.fs.graphics.Sprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.campaign.DynamicRingBand
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class CombatWarpingBackgroundRenderer(var background: String, var color: Color) : BaseCombatLayeredRenderingPlugin() {

    //var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2.jpg")

    var sprite: Sprite

    init {
        Global.getSettings().loadTexture(background)
        sprite = Sprite(background)
    }

    var renderer = CombatWarpingSpriteRenderer(8, 0.3f)

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)


        if (layer == CombatEngineLayers.BELOW_PLANETS) {

            //renderer.advance(0.05f)
            renderer.overwriteColor = color
            renderer.render(sprite, true, viewport!!)
        }
    }
}