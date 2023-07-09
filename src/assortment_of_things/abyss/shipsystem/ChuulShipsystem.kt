package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript

class ChuulShipsystem : BaseShipSystemScript() {

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

        if (AbyssalsCoreHullmod.isChronosCore(ship!!))
        {
            stats.ballisticRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))
            stats.energyRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))
            stats.missileRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))

            stats.energyWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel));
            stats.ballisticWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel))
            stats.missileWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel))
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            stats.ballisticWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
            stats.energyWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
            stats.missileWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
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
            return "Temporal Burst"
        }
        else if ( AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            return "Cosmal Burst"
        }
        return "Inactive Shipsystem"
    }
}