package assortment_of_things.abyss.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.FlickerUtilV2Abyssal
import assortment_of_things.abyss.procgen.BiomeDepth
import assortment_of_things.abyss.procgen.biomes.SeaOfSolitude
import assortment_of_things.abyss.terrain.BaseFogTerrain
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.util.IntervalUtil
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class SolitudeStormCombatRenderer(var solitude: SeaOfSolitude) : BaseCombatLayeredRenderingPlugin() {

    var thunder = Global.getSettings().getAndLoadSprite("graphics/fx/rat_solitude_thunder.png")

    override fun isExpired(): Boolean {
        return false
    }


    var stormInterval = IntervalUtil(6f, 8f)

    var flicker1 = FlickerUtilV2Abyssal(0.5f)
    var flicker2 = FlickerUtilV2Abyssal(0.7f)
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

        stormInterval.advance(amount) //Only advance if not storming
        if (stormInterval.intervalElapsed()) {
            flicker1.newBurst()
            flicker2.newBurst()
            stormInterval.advance(0f)

            flicker1.numBursts = MathUtils.getRandomNumberInRange(5,7)
            flicker2.numBursts = MathUtils.getRandomNumberInRange(2,3)
        }

        flicker1.advance(amount * 0.15f)
        flicker2.advance(amount * 0.20f)

        //flicker2.advance(amount)

        if (flicker1.isPeakFrame) {
            Global.getSoundPlayer().playSound("rat_abyss_solitude_storm_sounds", 0.8f, 1.4f, Global.getCombatEngine().viewport.center, Vector2f())
        }
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