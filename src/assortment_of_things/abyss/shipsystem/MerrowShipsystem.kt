package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.magiclib.kotlin.setAlpha

class MerrowShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        ship = stats!!.entity as ShipAPI

        if (ship!!.system.isActive)
        {
            AbyssalsCoreHullmod.getRenderer(ship!!).enableBlink()
        }
        else
        {
            AbyssalsCoreHullmod.getRenderer(ship!!).disableBlink()
        }

        if (ship!!.system.isActive)
        {
            ship!!.setJitterUnder(this, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(100), 1f, 25, 0f, 4 * effectLevel)
        }

        if (AbyssalsCoreHullmod.isChronosCore(ship!!))
        {
            stats.fluxDissipation.modifyMult(id, 1f + (0.5f * effectLevel))
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            stats.hardFluxDissipationFraction.modifyFlat(id, 1f * effectLevel)
            stats.fluxDissipation.modifyMult(id, 1f + (0.1f * effectLevel))
        }
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) return true
        if (AbyssalsCoreHullmod.isChronosCore(ship!!)) return true
        return false
    }

    override fun getDisplayNameOverride(state: ShipSystemStatsScript.State?, effectLevel: Float): String {
        if (ship == null) return "Inactive Shipsystem"
        if (AbyssalsCoreHullmod.isChronosCore(ship!!))
        {
            return "Temporal Flow"
        }
        else if ( AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            return "Cosmal Flow"
        }
        return "Inactive Shipsystem"
    }
}