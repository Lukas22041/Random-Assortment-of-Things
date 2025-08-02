package assortment_of_things.abyss.combat.title

import assortment_of_things.RATModPlugin
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.combat.SolitudeStormParticleCombatRenderer
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

        var c = Color(194, 88, 0)
        var dark = Color(33, 15, 0)

       /* var c = Color(150, 60, 0)
       var dark = Color(20, 10, 0)*/

        /*var c = Color(70, 10, 10)
        var dark = Color(25, 7, 0)*/


        Global.getCombatEngine().addLayeredRenderingPlugin(SolitudeStormParticleCombatRenderer(c, c.darker()))

        Global.getCombatEngine().addLayeredRenderingPlugin(CombatTitlePhotosphereRenderer(150f, 0f).apply { color = c })
        Global.getCombatEngine().addLayeredRenderingPlugin(AbyssTitleCombatHueApplier(dark).apply { alpha = 140 })

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