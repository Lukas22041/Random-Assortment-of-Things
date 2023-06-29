package assortment_of_things.combat

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.scripts.ModularWeaponCombatHandler
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.CombatEngine
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.MathUtils
import java.awt.Color
import java.util.*


class CombatHandler : EveryFrameCombatPlugin
{

    var modularHandler = ModularWeaponCombatHandler()

    override fun init(engine: CombatEngineAPI?)  {

    }

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?)
    {
        if (Global.getCurrentState() != GameState.TITLE && Global.getSector() != null)
        {
            var system = Global.getSector().playerFleet.containingLocation
            if (system.hasTag(AbyssUtils.SYSTEM_TAG))
            {
                CombatEngine.getBackground().color = AbyssUtils.getSystemColor(system)
            }
        }

        modularHandler.advance(amount)
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {

    }


    override fun renderInUICoords(viewport: ViewportAPI?) {

    }
}