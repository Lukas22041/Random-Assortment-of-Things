package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine
import java.awt.Color

class AbyssTitleScreen : EveryFrameCombatPlugin {

    override fun init(engine: CombatEngineAPI?) {
        Global.getCombatEngine().addLayeredRenderingPlugin(IonicStormCombatRenderer())

        Global.getCombatEngine().addLayeredRenderingPlugin(CombatTitlePhotosphereRenderer(150f))
        Global.getCombatEngine().addLayeredRenderingPlugin(AbyssTitleCombatHueApplier(AbyssUtils.ABYSS_COLOR))

        Global.getCombatEngine().backgroundColor = Color(125, 25, 25)
        CombatEngine.replaceBackground("graphics/backgrounds/abyss/Abyss2.jpg", true)

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