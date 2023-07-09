package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.PlasmaJetsStats
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.magiclib.kotlin.setAlpha

class MorkothShipsystem : BaseShipSystemScript() {

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
            if (ship!!.system.isActive) {
                ship!!.engineController.extendFlame(this, 0.5f * effectLevel, 0.5f * effectLevel, 0.5f * effectLevel)
                ship!!.setJitterUnder(this, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(100), 1f, 25, 0f, 10 * effectLevel)
            }

            if (state == ShipSystemStatsScript.State.OUT) {
                stats.maxSpeed.unmodify(id)
                stats.maxTurnRate.unmodify(id)
            } else {
                stats.maxSpeed.modifyMult(id, 3f)
                stats.acceleration.modifyMult(id, 4f * effectLevel)
                stats.deceleration.modifyMult(id, 4f * effectLevel)
                stats.turnAcceleration.modifyMult(id, 3f * effectLevel)
                stats.maxTurnRate.modifyMult(id, 3f)
            }
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            if (ship!!.system.isActive) {
                ship!!.setJitterUnder(this, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(100), 1f, 25, 0f, 10 * effectLevel)
            }

            stats.shieldDamageTakenMult.modifyMult(id, 1f - (0.5f * effectLevel))
        }
    }

    override fun getActiveOverride(ship: ShipAPI?): Float {
        if (AbyssalsCoreHullmod.isChronosCore(ship!!)) return 2f
        if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) return 4f

        return super.getActiveOverride(ship)
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        super.unapply(stats, id)

        stats!!.shieldDamageTakenMult.unmodify(id)

        stats.maxSpeed.unmodify(id)
        stats.maxTurnRate.unmodify(id)
        stats.turnAcceleration.unmodify(id)
        stats.acceleration.unmodify(id)
        stats.deceleration.unmodify(id)
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
            return "Temporal Matric"
        }
        else if ( AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            return "Cosmal Matrix"
        }
        return "Inactive Shipsystem"
    }
}