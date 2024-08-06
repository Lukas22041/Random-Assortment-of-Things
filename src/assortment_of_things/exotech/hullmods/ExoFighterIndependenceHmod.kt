package assortment_of_things.exotech.hullmods

import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.FighterLaunchBayAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils

//Only really works on single fighter wings
//You still want to
class ExoFighterIndependenceHmod : BaseHullMod() {

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        Global.getCombatEngine().addPlugin(ExoFighterIndependenceHmodScript(ship))
    }

}

class ExoFighterIndependenceHmodScript(var ship: ShipAPI) : BaseEveryFrameCombatPlugin() {

    var source: FighterLaunchBayAPI? = null
    var initated = false



    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        var wing = ship.wing
        if (wing != null && !initated) {
            initated = true
            source = wing.source
            wing.setSourceBay(null)
        }

        if (!ship.isAlive && source != null && wing != null) {
            wing.setSourceBay(source)
            Global.getCombatEngine().removePlugin(this)
        }
    }
}