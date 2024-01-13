package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.WeaponAPI
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
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!).enableBlink()
        }
        else
        {
            AbyssalsAdaptabilityHullmod.getRenderer(ship!!).disableBlink()
        }



        var color = AbyssalsAdaptabilityHullmod.getColorForCore(ship!!).setAlpha(100)

        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!))
        {
            stats.energyAmmoRegenMult.modifyMult(id, 1f + (0.5f * effectLevel))
            stats.ballisticAmmoRegenMult.modifyMult(id, 1f + (0.5f * effectLevel))

            stats.ballisticRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))
            stats.ballisticWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel))

            stats.energyRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))
            stats.energyWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel))

            if (ship!!.system.isActive) {
                ship!!.allWeapons.forEach {
                    if (it.type != WeaponAPI.WeaponType.BALLISTIC && it.type != WeaponAPI.WeaponType.ENERGY) return@forEach
                    it.setGlowAmount(1.5f * effectLevel, color.setAlpha(75))
                }
            }
            else {
                ship!!.allWeapons.forEach {
                    if (it.type != WeaponAPI.WeaponType.BALLISTIC && it.type != WeaponAPI.WeaponType.ENERGY) return@forEach
                    it.setGlowAmount(0f, color)
                }
            }
        }

        if (AbyssalsAdaptabilityHullmod.isCosmosCore(ship!!))
        {
            if (ship!!.system.isActive)
            {
                ship!!.setJitterUnder(this, AbyssalsAdaptabilityHullmod.getColorForCore(ship!!).setAlpha(100), 1f, 25, 0f, 4 * effectLevel)
            }

            stats.hardFluxDissipationFraction.modifyFlat(id, 1f * effectLevel)
            stats.fluxDissipation.modifyMult(id, 1f + (0.2f * effectLevel))
        }
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        if (AbyssalsAdaptabilityHullmod.isCosmosCore(ship!!)) return true
        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) return true
        return false
    }

    override fun getDisplayNameOverride(state: ShipSystemStatsScript.State?, effectLevel: Float): String {
        if (ship == null) return "Inactive Shipsystem"
        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!))
        {
            return "Accelerated Barrels"
        }
        else if ( AbyssalsAdaptabilityHullmod.isCosmosCore(ship!!))
        {
            return "Abyssal Rift"
        }
        return "Inactive Shipsystem"
    }
}