package assortment_of_things.combat

import assortment_of_things.RATModPlugin
import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine
import lunalib.lunaTitle.BaseLunaTitleScreenPlugin
import org.dark.shaders.post.PostProcessShader
import org.dark.shaders.util.ShaderLib
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import java.awt.Color

class AbyssSpecialTitleScreen : BaseLunaTitleScreenPlugin() {

    override fun pickBasedOnSystemCondition(lastSystemID: String, lastSystemTags: ArrayList<String>): Boolean {
        if (RATModPlugin.isHalloween) return true
        return false
    }

    override fun getWeight(): Float {
        if (RATModPlugin.isHalloween) return Float.MAX_VALUE
        return super.getWeight()
    }

    var shader = 0

    init {
        shader = ShaderLib.loadShader(
            Global.getSettings().loadText("data/shaders/baseVertex.shader"),
            Global.getSettings().loadText("data/shaders/rat_titleShader.shader"))
        if (shader != 0) {
            GL20.glUseProgram(shader)

            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex"), 0)

            GL20.glUseProgram(0)
        } else {
            var test = ""
        }
    }

    override fun init(engine: CombatEngineAPI?) {
        Global.getCombatEngine().addLayeredRenderingPlugin(IonicStormCombatRenderer().apply { color = Color(255, 111, 0); minAlpha = 0.25f; maxAlpha = 0.6f })

        Global.getCombatEngine().addLayeredRenderingPlugin(CombatTitlePhotosphereRenderer(150f, 0f).apply { color = Color(194, 88, 0) })
        Global.getCombatEngine().addLayeredRenderingPlugin(AbyssTitleCombatHueApplier(Color(33, 15, 0)).apply { alpha = 140 })

        Global.getCombatEngine().backgroundColor = Color(0, 0, 0)
        CombatEngine.replaceBackground("graphics/backgrounds/abyss/Abyss2.jpg", true)


        Global.getSoundPlayer().playCustomMusic(1, 1, "rat_music_abyss", true)
    }



    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {

    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {


        //Disabling shaders makes the screen texture unable to load
        if (ShaderLib.getScreenTexture() != 0) {
            var noiseMult = 1f
            if (Global.getCombatEngine().isSimulation) noiseMult = 0.5f;

            ShaderLib.beginDraw(shader);
            GL20.glUniform1f(GL20.glGetUniformLocation(shader, "iTime"), Global.getCombatEngine().getTotalElapsedTime(false) / 8f)
            GL20.glUniform1f(GL20.glGetUniformLocation(shader, "noise"), 0.05f * noiseMult)

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

            GL11.glDisable(GL11.GL_BLEND);
            ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0 + 0)
            ShaderLib.exitDraw()
        }
    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

        if (Global.getCombatEngine().isSimulation) return

        var viewport = Global.getCombatEngine().viewport
        viewport.isExternalControl = true

        var llx = viewport.llx
        var lly = viewport.lly

        var width = Global.getSettings().screenWidth * 2
        var height = Global.getSettings().screenHeight * 2

        viewport.set(0f-width/2, 0f-height/2, width, height)
    }

}