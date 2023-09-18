package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class MorkothShipsystem : BaseShipSystemScript(), DamageListener {

    var ship: ShipAPI? = null
    var afterimageInterval = IntervalUtil(0.2f, 0.2f)


    var addedDamageListener = false
    var min = 0f
    var max = 7500f

    var damageTaken = 0f
    var activated = false

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)


        ship = stats!!.entity as ShipAPI

        if (!activated && ship!!.system.isActive) {
            if (AbyssalsCoreHullmod.isChronosCore(ship!!)) {
                Global.getSoundPlayer().playSound("system_maneuvering_jets", 1f, 1f, ship!!.location, ship!!.velocity)
            }
            if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) {
                Global.getSoundPlayer().playSound("system_damper", 1f, 1f, ship!!.location, ship!!.velocity)
                if (ship!!.shield != null) {
                    ship!!.shield.toggleOn()
                }
            }
            activated = true
        }

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
                ship!!.setJitterUnder(this, AbyssalsCoreHullmod.getColorForCore(ship!!).setAlpha(100), 1f * effectLevel, 10, 0f, 10f)
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
                stats.maxSpeed.modifyFlat(id, 75f)
                stats.acceleration.modifyMult(id, 4f * effectLevel)
                stats.deceleration.modifyMult(id, 4f * effectLevel)
                stats.turnAcceleration.modifyMult(id, 2f * effectLevel)
                stats.maxTurnRate.modifyMult(id, 2f)
            }
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            if (!addedDamageListener) {
                ship!!.addListener(this)
                addedDamageListener = true
            }





            var level = (damageTaken - min) / (max - min)
            level = MathUtils.clamp(level, 0f, 1f)

            var path = "graphics/icons/hullsys/high_energy_focus.png"
            Global.getSettings().loadTexture(path)

            if (ship == Global.getCombatEngine().playerShip) {
                Global.getCombatEngine().maintainStatusForPlayerShip("rat_morkoth_cosmos",
                    path,
                    "Event Horizon",
                    "Strength: ${(level * 100).toInt()}%",
                    false)
            }

            if (ship!!.shield != null)
            {
                var ringColor = Misc.interpolateColor(ship!!.hullSpec.shieldSpec.ringColor, AbyssalsCoreHullmod.getColorForCore(ship!!), level * effectLevel)
                var innerColor = Misc.interpolateColor(ship!!.hullSpec.shieldSpec.innerColor, AbyssalsCoreHullmod.getColorForCore(ship!!), level * effectLevel)

                ship!!.shield.ringColor = ringColor.setAlpha(255)
                ship!!.shield.innerColor = innerColor.setAlpha(75)

                ship!!.isJitterShields = ship!!.system.isActive

                if (ship!!.system.isActive) {
                    afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
                    if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
                    {
                        AfterImageRenderer.addAfterimage(ship!!, innerColor.setAlpha(75), innerColor.setAlpha(75), 3f, 2f, Vector2f().plus(ship!!.location))

                        ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                        var from = Vector2f(ship!!.exactBounds.segments.random().p1)
                        var to = Vector2f(ship!!.exactBounds.segments.random().p1)

                        Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, innerColor, innerColor)
                    }
                }
            }

            if (ship!!.system.isActive) {
                ship!!.isJitterShields = true

            }

            stats.shieldUnfoldRateMult.modifyMult(id, 1f + (3f * effectLevel))

            stats.ballisticWeaponDamageMult.modifyMult(id, 1 + (0.2f * level))
            stats.energyWeaponDamageMult.modifyMult(id, 1 + (0.2f * level))
            stats.missileWeaponDamageMult.modifyMult(id, 1 + (0.2f * level))

            stats.ballisticRoFMult.modifyMult(id, 1 + (0.25f * level))
            stats.energyRoFMult.modifyMult(id, 1 + (0.25f * level))
            stats.missileRoFMult.modifyMult(id, 1 + (0.25f * level))

            stats.ballisticWeaponFluxCostMod.modifyMult(id, 1 - (0.25f * level))
            stats.energyWeaponFluxCostMod.modifyMult(id, 1 - (0.25f * level))

            stats.fluxDissipation.modifyMult(id, 1 + (0.20f * level))
            stats.shieldDamageTakenMult.modifyMult(id, 1 - (0.20f * level))
        }
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

        stats.shieldUnfoldRateMult.unmodify(id)



        stats.shieldUnfoldRateMult.unmodify(id)

        stats.ballisticWeaponDamageMult.unmodify(id)
        stats.energyWeaponDamageMult.unmodify(id)
        stats.missileWeaponDamageMult.unmodify(id)

        stats.ballisticRoFMult.unmodify(id)
        stats.energyRoFMult.unmodify(id)
        stats.missileRoFMult.unmodify(id)

        stats.ballisticWeaponFluxCostMod.unmodify(id)
        stats.energyWeaponFluxCostMod.unmodify(id)

        stats.fluxDissipation.unmodify(id)
        stats.shieldDamageTakenMult.unmodify(id)

        damageTaken = 0f
        activated = false
    }

    override fun getActiveOverride(ship: ShipAPI): Float {
        if (AbyssalsCoreHullmod.isChronosCore(ship)) return 2f
        if (AbyssalsCoreHullmod.isCosmosCore(ship)) return 12f

        return super.getActiveOverride(ship)
    }

    override fun getOutOverride(ship: ShipAPI): Float {
        if (AbyssalsCoreHullmod.isChronosCore(ship)) return 0.25f
        if (AbyssalsCoreHullmod.isCosmosCore(ship)) return 1f

        return super.getOutOverride(ship)
    }

    override fun getUsesOverride(ship: ShipAPI): Int {
        if (AbyssalsCoreHullmod.isChronosCore(ship)) return 2
        if (AbyssalsCoreHullmod.isCosmosCore(ship)) return 1

        return super.getUsesOverride(ship)
    }

    override fun getRegenOverride(ship: ShipAPI): Float {
        if (AbyssalsCoreHullmod.isChronosCore(ship)) return 0.04f
        if (AbyssalsCoreHullmod.isCosmosCore(ship)) return 0.02f

        return super.getRegenOverride(ship)
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
            return "Temporal Jets"
        }
        else if ( AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            return "Event Horizon"
        }
        return "Inactive Shipsystem"
    }

    override fun reportDamageApplied(source: Any?, target: CombatEntityAPI?, result: ApplyDamageResultAPI?) {
        if (ship != null && ship!!.system.state == ShipSystemAPI.SystemState.IN || ship!!.system.state == ShipSystemAPI.SystemState.ACTIVE) {
            damageTaken += result!!.damageToShields
        }
    }
}