package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.abyss.terrain.AbyssalDarknessTerrainPlugin
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.Misc
import org.dark.shaders.light.LightShader
import org.dark.shaders.light.StandardLight
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector3f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssCombatHueApplier(var color: Color, var tier: AbyssProcgen.Tier, var darkness: AbyssalDarknessTerrainPlugin) : BaseCombatLayeredRenderingPlugin() {


    var sprite: SpriteAPI

    var alpha = 25

    init {
        sprite = Global.getSettings().getAndLoadSprite("graphics/fx/rat_darkener.png")



    }

    override fun init(entity: CombatEntityAPI?) {
        super.init(entity)


        alpha = when(tier) {
            AbyssProcgen.Tier.Low -> 25
            AbyssProcgen.Tier.Mid -> 70
            AbyssProcgen.Tier.High -> 75
        }

        if (!darkness.containsEntity(Global.getSector().playerFleet)) {
            alpha = when(tier) {
                AbyssProcgen.Tier.Low -> 30
                AbyssProcgen.Tier.Mid -> 80
                AbyssProcgen.Tier.High -> 85
            }
        }

       /* var photosphere = darkness.containingPhotosphere(Global.getSector().playerFleet)
        if (photosphere != null) {
            var playerfleet = Global.getSector().playerFleet
            var angle = Misc.getAngleInDegrees(playerfleet.location, photosphere.location)

            val sun = StandardLight()
            sun.type = 3
            sun.direction = Vector3f(1f, -1f, -0.5f).normalise() as Vector3f
            sun.intensity = 10.020f
            sun.specularIntensity = 10.020f
            sun.setColor(color.red / 255f, color.green / 255f, color.blue / 255f)
            sun.makePermanent()
            LightShader.addLight(sun)
        }*/

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