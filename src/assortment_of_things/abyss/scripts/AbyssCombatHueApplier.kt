package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssCombatHueApplier(var color: Color, var tier: AbyssProcgen.Tier) : BaseCombatLayeredRenderingPlugin() {


    var sprite: SpriteAPI

    init {
        sprite = Global.getSettings().getAndLoadSprite("graphics/fx/rat_darkener.png")
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES)
    }

    override fun getRenderRadius(): Float {
        return 1000000f
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)


        var alpha = when(tier) {
            AbyssProcgen.Tier.Low -> 25
            AbyssProcgen.Tier.Mid -> 70
            AbyssProcgen.Tier.High -> 75
        }

        sprite.setSize(viewport!!.visibleWidth + 200f, viewport.visibleHeight + 200f)
        sprite.setNormalBlend()
        sprite.color = color.setAlpha(alpha)
        sprite.render(viewport.llx - 100f, viewport.lly - 100f)


    }
}