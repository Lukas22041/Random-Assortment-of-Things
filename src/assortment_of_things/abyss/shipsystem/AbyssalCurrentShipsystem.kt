package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import com.fs.starfarer.api.combat.BeamAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.magiclib.kotlin.setAlpha

class AbyssalCurrentShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        ship = stats!!.entity as ShipAPI

        if (ship!!.system.isActive)
        {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.enableBlink()
        }
        else
        {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!)?.disableBlink()
        }

        var weapon = ship!!.allWeapons.find { it.spec?.weaponId == "rat_merrow_beam" } ?: return

        if (state == ShipSystemStatsScript.State.ACTIVE || state == ShipSystemStatsScript.State.IN) {
            weapon.setForceFireOneFrame(true)
        }
        else {
            weapon.setForceNoFireOneFrame(true)
        }
    }
}