package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsAdaptabilityHullmod
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha

class ChuulShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null

    var activated = false
    var init = false

    var afterimageInterval = IntervalUtil(0.05f, 0.05f)

    var targetLocation = Vector2f()

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        ship = stats!!.entity as ShipAPI

        if (state == ShipSystemStatsScript.State.IN && !init) {
            onInit(ship!!)

            if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) {
                Global.getSoundPlayer().playSound("system_phase_skimmer", 1f, 1f, ship!!.location, ship!!.velocity)
            }
            if (AbyssalsAdaptabilityHullmod.isCosmosCore(ship!!)) {
                Global.getSoundPlayer().playSound("system_ammo_feeder", 1f, 1f, ship!!.location, ship!!.velocity)
            }

            init = true
        }
        if (state == ShipSystemStatsScript.State.IDLE) {
            init = false
        }

        if (state == ShipSystemStatsScript.State.ACTIVE && !activated) {
            if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) {
                ship!!.location.set(targetLocation)
            }
            activated = true
        }
        if (state == ShipSystemStatsScript.State.IDLE) {
            activated = false
        }


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

            if (state == ShipSystemStatsScript.State.IN) {
                ship!!.isPhased = true
            }
            if (state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.IDLE) {
                ship!!.isPhased = false
            }

            if (ship!!.system.isActive) {

                afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
                if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
                {
                    if (state == ShipSystemStatsScript.State.IN) {
                        AfterImageRenderer.addAfterimage(ship!!, color, color, 0.65f, 25f, targetLocation)
                    }
                }

                ship!!.setJitter(this, color, 1f * effectLevel , 3, 0f, 15f)
                ship!!.setJitterUnder(this, color, 1f * effectLevel, 25, 0f, 15f)

            }

            stats.timeMult.modifyMult(id, 1 + (3f * effectLevel))
            val shipTimeMult = 1f + (10f - 1f) * effectLevel
            stats.timeMult.modifyMult(id, shipTimeMult)
            if (Global.getCombatEngine().playerShip == ship) {
                Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
            } else {
                Global.getCombatEngine().timeMult.unmodify(id)
            }
        }

        if (AbyssalsAdaptabilityHullmod.isCosmosCore(ship!!))
        {
            stats.ballisticWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
            stats.energyWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
            stats.missileWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))



            if (ship!!.system.isActive) {
                ship!!.allWeapons.forEach {
                    it.setGlowAmount(2f * effectLevel, color.setAlpha(50))
                }
            }
            else {
                ship!!.allWeapons.forEach {
                    it.setGlowAmount(0f, color)
                }
            }

        }
    }

    fun onInit(ship: ShipAPI) {
        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) {
            targetLocation = findTargetLocation(ship, 5f)
        }
    }

    fun findTargetLocation(ship: ShipAPI, mult: Float) : Vector2f {
        var iter = Global.getCombatEngine().allObjectGrid.getCheckIterator(ship.location, 1000f, 1000f)

        var velocity = Vector2f(ship.velocity.x * mult, ship.velocity.y * mult)
        var velX = MathUtils.clamp(velocity.x, -200f, 200f)
        var velY = MathUtils.clamp(velocity.y, -200f, 200f)
        velocity = Vector2f(velX, velY)

        var location = Vector2f(ship.location.x + velocity.x, ship.location.y + velocity.y)

        if (mult < 0.1f) return location

        for (it in iter) {
            if (it == ship) continue
            if (it !is CombatEntityAPI) continue
            if (it is DamagingProjectileAPI) continue
            var colRadius = (it.collisionRadius / 2 + ship.collisionRadius / 2) * 1.5f

            if (MathUtils.getDistance(location, it.location) < colRadius) {
                return findTargetLocation(ship, mult - 0.2f)
            }
        }

        return location
    }


    override fun getInOverride(ship: ShipAPI?): Float {
        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) return 0.25f
        return 0.5f
    }

    override fun getOutOverride(ship: ShipAPI?): Float {
        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) return 0.75f
        return 0.5f
    }

    override fun getActiveOverride(ship: ShipAPI?): Float {
        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) return 0.1f
        return 6f
    }

    override fun getUsesOverride(ship: ShipAPI?): Int {
        if (AbyssalsAdaptabilityHullmod.isCosmosCore(ship!!)) return 1
        return 3
    }

    override fun getRegenOverride(ship: ShipAPI?): Float {
        if (AbyssalsAdaptabilityHullmod.isChronosCore(ship!!)) return 0.1f
        return 0.05f
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
            return "Temporal Skimmer"
        }
        else if ( AbyssalsAdaptabilityHullmod.isCosmosCore(ship!!))
        {
            return "Singularity"
        }
        return "Inactive Shipsystem"
    }
}