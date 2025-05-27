package assortment_of_things.abyss.combat.title

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.combat.CombatBackgroundWarper
import assortment_of_things.abyss.combat.SolitudeStormParticleCombatRenderer
import assortment_of_things.abyss.combat.title.CombatTitlePhotosphereRenderer
import assortment_of_things.misc.ReflectionUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.campaign.WarpingSpriteRenderer
import com.fs.starfarer.combat.CombatEngine
import lunalib.lunaTitle.BaseLunaTitleScreenPlugin
import java.awt.Color

class AbyssTitleScreen : BaseLunaTitleScreenPlugin() {




    override fun pickBasedOnSystemCondition(lastSystemID: String, lastSystemTags: ArrayList<String>): Boolean {
        if (lastSystemTags.contains(AbyssUtils.SYSTEM_TAG)) return true
        return false
    }

    override fun init(engine: CombatEngineAPI?) {

        var c = Color(125, 25, 25)

        Global.getCombatEngine().addLayeredRenderingPlugin(SolitudeStormParticleCombatRenderer(c, c.darker()))

        Global.getCombatEngine().addLayeredRenderingPlugin(CombatTitlePhotosphereRenderer(150f, 600f))
        Global.getCombatEngine().addLayeredRenderingPlugin(AbyssTitleCombatHueApplier(AbyssUtils.ABYSS_COLOR))

        Global.getCombatEngine().backgroundColor = Color(125, 25, 25)
        CombatEngine.replaceBackground("graphics/backgrounds/abyss/Abyss2.jpg", true)

        var warper = CombatBackgroundWarper(8, 0.25f)
        ReflectionUtils.set(null, Global.getCombatEngine(), warper, WarpingSpriteRenderer::class.java)
        warper.overwriteColor = c

        Global.getSoundPlayer().playCustomMusic(1, 1, "rat_music_abyss", true)
    }



    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {

    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }

    override fun renderInUICoords(viewport: ViewportAPI?) {

        var viewport = Global.getCombatEngine().viewport
        viewport.isExternalControl = true

        var llx = viewport.llx
        var lly = viewport.lly

        var width = Global.getSettings().screenWidth * 2
        var height = Global.getSettings().screenHeight * 2

        viewport.set(0f-width/2, 0f-height/2, width, height)

    }

}