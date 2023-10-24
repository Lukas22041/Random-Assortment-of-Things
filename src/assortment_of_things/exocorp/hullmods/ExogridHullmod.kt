package assortment_of_things.exocorp.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI

class ExogridHullmod : BaseHullMod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        Global.getCombatEngine().addLayeredRenderingPlugin(ExogridRenderer(ship))
    }
}