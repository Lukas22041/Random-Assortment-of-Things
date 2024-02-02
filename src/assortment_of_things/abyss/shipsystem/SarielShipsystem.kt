package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.HullmodUtils
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class SarielShipsystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.2f, 0.2f)

    override fun apply(stats: MutableShipStatsAPI?, id: String,  state: ShipSystemStatsScript.State?,  effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        var ship = stats!!.entity
        if (ship !is ShipAPI) return

        var system = ship.system


        var color = Color(196, 20, 35, 255)

        if (system.isActive) {
            HullmodUtils.negateStatIncreases(id, stats.energyWeaponRangeBonus, stats)
            HullmodUtils.negateStatIncreases(id, stats.missileWeaponRangeBonus, stats)
            HullmodUtils.negateStatIncreases(id, stats.ballisticWeaponRangeBonus, stats)
            HullmodUtils.negateStatIncreases(id, stats.beamWeaponRangeBonus, stats)

            ship!!.setJitterUnder(this, color, 1f, 25, 0f, 6 * effectLevel)

            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(75), color.setAlpha(75), 2f, 2f, Vector2f().plus(ship!!.location))

                ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                var from = Vector2f(ship!!.exactBounds.segments.random().p1)
                var to = Vector2f(ship!!.exactBounds.segments.random().p1)

                Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, color.setAlpha(75), color.setAlpha(75))
            }
        }
        else {
            HullmodUtils.removeNegation(id, stats.energyWeaponRangeBonus, stats)
            HullmodUtils.removeNegation(id, stats.missileWeaponRangeBonus, stats)
            HullmodUtils.removeNegation(id, stats.ballisticWeaponRangeBonus, stats)
            HullmodUtils.removeNegation(id, stats.beamWeaponRangeBonus, stats)
        }

        stats.maxSpeed.modifyFlat(id, 20f * effectLevel)
        stats.maxTurnRate.modifyMult(id, 1 + (0.5f * effectLevel))
        stats.turnAcceleration.modifyMult(id, 1 + (0.5f * effectLevel))
        stats.acceleration.modifyMult(id, 1 + (0.5f * effectLevel))
        stats.deceleration.modifyMult(id, 1 + (0.5f * effectLevel))


        stats.shieldDamageTakenMult.modifyMult(id, 1 - (0.4f * effectLevel))
        stats.armorDamageTakenMult.modifyMult(id, 1 - (0.4f * effectLevel))
        stats.hullDamageTakenMult.modifyMult(id, 1 - (0.4f * effectLevel))
        ship.mutableStats.empDamageTakenMult.modifyMult(id, 1 - (0.4f * effectLevel))

        stats.ballisticRoFMult.modifyMult(id, 1 + (0.33f * effectLevel))
        stats.energyRoFMult.modifyMult(id, 1 + (0.33f * effectLevel))
        stats.ballisticRoFMult.modifyMult(id, 1 + (0.33f * effectLevel))

        stats.ballisticAmmoRegenMult.modifyMult(id, 1 + (0.33f * effectLevel))
        stats.energyAmmoRegenMult.modifyMult(id, 1 + (0.33f * effectLevel))

        stats.ballisticWeaponFluxCostMod.modifyMult(id, 1 - (0.33f * effectLevel))
        stats.energyWeaponFluxCostMod.modifyMult(id, 1 - (0.33f * effectLevel))
        stats.missileWeaponFluxCostMod.modifyMult(id, 1 - (0.33f * effectLevel))

        stats.ballisticWeaponDamageMult.modifyMult(id, 1 + (0.10f * effectLevel))
        stats.energyWeaponDamageMult.modifyMult(id, 1 + (0.10f * effectLevel))
        stats.missileWeaponDamageMult.modifyMult(id, 1 + (0.10f * effectLevel))



    }


}