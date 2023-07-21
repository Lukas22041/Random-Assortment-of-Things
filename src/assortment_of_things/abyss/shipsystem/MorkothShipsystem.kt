package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class MorkothShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null
    var afterimageInterval = IntervalUtil(0.2f, 0.2f)

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
                ship!!.engineController.extendFlame(this, 1f * effectLevel, 0.2f * effectLevel, 0.5f * effectLevel)
                ship!!.setJitterUnder(this, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(50), 1f, 10, 0f, 5 * effectLevel)
                afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
                if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
                {
                    AfterImageRenderer.addAfterimage(ship!!, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(100), Color(150, 0 ,255).setAlpha(100), 2f, 2f, Vector2f().plus(ship!!.location))
                }
            }

            if (state == ShipSystemStatsScript.State.OUT) {
                stats.maxSpeed.unmodify(id)
                stats.maxTurnRate.unmodify(id)
            } else {
               // stats.maxSpeed.modifyMult(id, 3f)
                stats.maxSpeed.modifyFlat(id, 50f)
                stats.acceleration.modifyMult(id, 4f * effectLevel)
                stats.deceleration.modifyMult(id, 4f * effectLevel)
                stats.turnAcceleration.modifyMult(id, 3f * effectLevel)
                stats.maxTurnRate.modifyMult(id, 3f)
            }
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {

            if (ship!!.shield != null)
            {

                var test = effectLevel

                var ringColor = Misc.interpolateColor(ship!!.hullSpec.shieldSpec.ringColor, AbyssalsCoreHullmod.getColorForCore(ship!!), effectLevel )
                var innerColor = Misc.interpolateColor(ship!!.hullSpec.shieldSpec.innerColor, AbyssalsCoreHullmod.getColorForCore(ship!!), effectLevel )

                ship!!.shield.ringColor = ringColor.setAlpha(255)
                ship!!.shield.innerColor = innerColor.setAlpha(75)

                if (ship!!.system.isActive) {

                    ship!!.isJitterShields = true
                   // ship!!.shield.toggleOn()
                }
                else
                {
                   ship!!.isJitterShields = false
                }
            }

            if (ship!!.system.isActive) {
                //ship!!.setJitterUnder(this, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(100), 1f, 25, 0f, 10 * effectLevel)

                ship!!.isJitterShields = true

            }

            stats.shieldDamageTakenMult.modifyMult(id, 1f - (0.5f * effectLevel))
        }
    }

    override fun getActiveOverride(ship: ShipAPI?): Float {
        if (AbyssalsCoreHullmod.isChronosCore(ship!!)) return 2f
        if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) return 5f

        return super.getActiveOverride(ship)
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        super.unapply(stats, id)

        if (ship != null) {
            AbyssalsCoreHullmod.getRenderer(ship!!).disableBlink()
        }

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