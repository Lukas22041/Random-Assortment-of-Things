package assortment_of_things.combat

import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.MathUtils
import java.awt.Polygon
import java.awt.Shape
import java.util.*


class CombatHandler : EveryFrameCombatPlugin
{



    override fun init(engine: CombatEngineAPI?)
    {
    }

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?)
    {

    }

    override fun renderInWorldCoords(viewport: ViewportAPI?)
    {

    }

    var sprite = Global.getSettings().getSprite("rat", "skeleton")
    var timer = 160
    var mult = 0f
    var musicStarted = false

    companion object {
        var enabled: Boolean? = null
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {

        if (enabled == null)
        {
            enabled = LunaSettings.getBoolean(RATSettings.modID, "rat_theSkeletonAppears")
            if (Random().nextFloat() < 0.5f)
            {
                enabled = false
            }
        }

        if (Global.getCurrentState() == GameState.TITLE && sprite != null && enabled != null && enabled!!)
        {
            timer--
            timer = MathUtils.clamp(timer, -1, 120)
            if (timer < 1)
            {
                mult = MathUtils.clamp(mult + 0.007f, 0f, 1f)
                sprite.alphaMult = mult
                sprite.setSize(Global.getSettings().screenWidth * mult, Global.getSettings().screenHeight * mult)
                sprite.renderAtCenter(Global.getSettings().screenWidth / 2,Global.getSettings().screenHeight / 2)
            }
            else
            {
                sprite.alphaMult = 0f
                mult = 0f
            }
        }
    }
}