package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.SeaOfTranquility
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import lunalib.lunaUtil.campaign.LunaCampaignRenderer
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin
import org.dark.shaders.util.ShaderLib
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.magiclib.kotlin.elapsedDaysSinceGameStart
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

//Transient
class BiomeAtmosphereRenderer : LunaCampaignRenderingPlugin {

    var shader = 0

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

        var manager = AbyssUtils.getBiomeManager()



        var warper = AbyssUtils.getData().warper
        warper?.overwriteColor = manager.getCurrentBackgroundColor()
        //TODO Set warper color here

    }

    var layers = EnumSet.of(CampaignEngineLayers.ABOVE)
    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return layers
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        var data = AbyssUtils.getData()
        if (Global.getSector()?.playerFleet?.containingLocation != data.system) return

        //TODO put in to init
        shader = ShaderLib.loadShader(
            Global.getSettings().loadText("data/shaders/baseVertex.shader"),
            Global.getSettings().loadText("data/shaders/rat_biomeAtmosphereFragment.shader"))
        if (shader != 0) {
            GL20.glUseProgram(shader)

            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex"), 0)

            GL20.glUseProgram(0)
        } else {
            var test = ""
        }

        //Screen texture can be unloaded if graphicslib shaders are disabled, causing a blackscreen
        if (ShaderLib.getScreenTexture() != 0) {
            //Shader

            var manager = AbyssUtils.getBiomeManager()
            var levels = manager.getBiomeLevels()
            var saturation = levels.map { it.key.getSaturation() * it.value }.sum()

            ShaderLib.beginDraw(shader);
            GL20.glUniform1f(GL20.glGetUniformLocation(shader, "saturation"), saturation)
            GL20.glUniform3f(GL20.glGetUniformLocation(shader, "colorMult"), 1.2f, 1.1f, 1.2f)

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

            //Might Fix Incompatibilities with odd drivers
            GL20.glValidateProgram(shader)
            if (GL20.glGetProgrami(shader, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                ShaderLib.exitDraw()
                return
            }

            GL11.glDisable(GL11.GL_BLEND);
            ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0 + 0)
            ShaderLib.exitDraw()

        }
    }
}