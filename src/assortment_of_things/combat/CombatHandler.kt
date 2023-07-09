package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.scripts.AbyssCombatHueApplier
import assortment_of_things.abyss.scripts.ResetBackgroundScript
import assortment_of_things.modular_weapons.scripts.ModularWeaponCombatHandler
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine
import java.awt.Color


class CombatHandler : EveryFrameCombatPlugin
{

    var modularHandler = ModularWeaponCombatHandler()

    override fun init(engine: CombatEngineAPI?)  {

        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {
            var system = Global.getSector()?.playerFleet?.containingLocation ?: return
            if (system.hasTag(AbyssUtils.SYSTEM_TAG) && Global.getCombatEngine().missionId == null)
            {
                var color = AbyssUtils.getSystemColor(system)
                var tier = AbyssUtils.getTier(system)

                Global.getCombatEngine().addLayeredRenderingPlugin(AbyssCombatHueApplier(color, tier))

                ResetBackgroundScript.resetBackground = true
                CombatEngine.getBackground().color = color.darker()
            }
        }
    }

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?)
    {
        modularHandler.advance(amount)
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

    }
}