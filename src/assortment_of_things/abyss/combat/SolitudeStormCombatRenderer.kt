package assortment_of_things.abyss.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.FlickerUtilV2Abyssal
import assortment_of_things.abyss.procgen.BiomeDepth
import assortment_of_things.abyss.procgen.biomes.SeaOfSolitude
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.dark.shaders.light.LightShader
import org.dark.shaders.light.StandardLight
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.lwjgl.util.vector.Vector3f
import java.awt.Color
import java.util.*

class SolitudeStormCombatRenderer(var solitude: SeaOfSolitude) : BaseCombatLayeredRenderingPlugin() {

    var thunder = Global.getSettings().getAndLoadSprite("graphics/fx/rat_solitude_thunder.png")



    override fun isExpired(): Boolean {
        return false
    }

    var stormInterval = IntervalUtil(12f, 22f)
    var stormDuration = 0f

    var flicker1 = FlickerUtilV2Abyssal(0.5f)
    var flicker2 = FlickerUtilV2Abyssal(0.7f)

    var addedLight = false
    var sun1 = StandardLight()
    var sun2 = StandardLight()
    var lightDirection1 = Vector2f()
    var lightDirection2 = Vector2f()
    //var flicker2 = FlickerUtilV2(4f)

    init {
        flicker1.numBursts = MathUtils.getRandomNumberInRange(5,7)
        flicker2.numBursts = MathUtils.getRandomNumberInRange(2,3)
    }


    override fun advance(amount: Float) {

        if (!AbyssUtils.isPlayerInAbyss()) return

        var manager = AbyssUtils.getBiomeManager()
        var dominant = manager.getDominantBiome()
        var cell = manager.getPlayerCell()

        if (stormDuration <= 0 && dominant == solitude && cell.depth != BiomeDepth.BORDER) stormInterval.advance(amount) //Only advance if not storming
        if (stormInterval.intervalElapsed()) {
            stormDuration = MathUtils.getRandomNumberInRange(10f, 16f)
            stormInterval.advance(0f)

            flicker1.numBursts = MathUtils.getRandomNumberInRange(5,7)
            flicker2.numBursts = MathUtils.getRandomNumberInRange(2,3)

            var x = MathUtils.getRandomNumberInRange(-0.5f, 0.5f)
            var y = MathUtils.getRandomNumberInRange(-0.5f, 0.5f)
            lightDirection1 = Vector2f(x, y)

            x = MathUtils.getRandomNumberInRange(-0.5f, 0.5f)
            y = MathUtils.getRandomNumberInRange(-0.5f, 0.5f)
            lightDirection2 = Vector2f(x, y)
        }

        stormDuration -= 1 * amount

        //Only advance while in the biome, or if it has to finish flashing
        if (stormDuration <= 0f || dominant != solitude || cell.depth == BiomeDepth.BORDER) {
            flicker1.stopAll = true
            flicker2.stopAll = true
        } else {
            flicker1.stopAll = false
            flicker2.stopAll = false
        }

        flicker1.advance(amount * 0.15f)
        flicker2.advance(amount * 0.20f)

        //flicker2.advance(amount)

        if (flicker1.isPeakFrame) {
            Global.getSoundPlayer().playSound("rat_abyss_solitude_storm_sounds", 0.8f, 1.25f, Global.getCombatEngine().viewport.center, Vector2f())
        }

        if (!addedLight) {
            addedLight = true

            sun1.type = 3
            sun1.specularIntensity = 0.020f
            sun1.makePermanent()
            LightShader.addLight(sun1)

            sun2.type = 3
            sun2.specularIntensity = 0.020f
            sun2.makePermanent()
            LightShader.addLight(sun2)

        }

        sun1.direction = Vector3f(lightDirection1.x, lightDirection1.y, 0.25f).normalise() as Vector3f
        sun1.intensity = 20.000f * flicker1.brightness
        sun1.setColor(0.8f, 0.01f, 0.03f)

        sun2.direction = Vector3f(lightDirection2.x, lightDirection2.y, 0.5f).normalise() as Vector3f
        sun2.intensity = 2.000f * flicker2.brightness
        sun2.setColor(1f, 1f, 1f)
    }

    var layers = EnumSet.of(CombatEngineLayers.BELOW_PLANETS)

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return layers
    }

    override fun getRenderRadius(): Float {
        return 1000000f
    }

    override fun render(layer: CombatEngineLayers, viewport: ViewportAPI) {

        var llx = viewport.llx - 100
        var lly = viewport.lly - 100

        var width = viewport.visibleWidth + 200
        var height = viewport.visibleHeight + 200

        thunder!!.setNormalBlend()
        thunder!!.alphaMult = 0.055f * flicker1.brightness /** flicker2.brightness*/
        thunder!!.color = solitude.getBiomeColor()
        thunder!!.setSize(width, height)
        thunder!!.render(llx, lly)


        thunder!!.setNormalBlend()
        thunder!!.alphaMult = 0.025f * flicker2.brightness /** flicker2.brightness*/
        thunder!!.color = Color(255, 220, 220)
        thunder!!.setSize(width, height)
        thunder!!.render(llx, lly)
    }

}