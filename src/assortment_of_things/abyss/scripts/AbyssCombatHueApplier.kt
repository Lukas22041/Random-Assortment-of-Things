package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.abyss.terrain.AbyssalDarknessTerrainPlugin
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssCombatHueApplier(var color: Color, var depth: AbyssDepth, var darkness: AbyssalDarknessTerrainPlugin) : BaseCombatLayeredRenderingPlugin() {


    var sprite: SpriteAPI = Global.getSettings().getAndLoadSprite("graphics/fx/rat_darkener.png")

    var alpha = 25

    override fun init(entity: CombatEntityAPI?) {
        super.init(entity)

        alpha = when(depth) {
            AbyssDepth.Shallow -> 70
            AbyssDepth.Deep -> 75
        }

        if (darkness.getDarknessMult() <= 0.5f) {
            alpha = when(depth) {
                AbyssDepth.Shallow -> 80
                AbyssDepth.Deep -> 85
            }
        }

        if (darkness.entity.containingLocation.hasTag("rat_abyss_final")) {
            alpha = 30
        }
    }


    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES)
    }

    override fun getRenderRadius(): Float {
        return 1000000f
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        sprite.setSize(viewport!!.visibleWidth + 200f, viewport.visibleHeight + 200f)
        sprite.setNormalBlend()
        sprite.color = color.setAlpha(alpha)
        sprite.render(viewport.llx - 100f, viewport.lly - 100f)

    }
}