package assortment_of_things.abyss.shipsystem

import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class TemporalStasisShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null
    var target: ShipAPI? = null
    var afterimageInterval = IntervalUtil(0.1f, 0.1f)
    var afterimageIntervalFighter = IntervalUtil(0.1f, 0.1f)

    var color = Color(196, 20, 35, 255)

    companion object {

        fun getMaxRange() : Float {
            if (Global.getSettings().isDevMode) return 2000f
            var maxRange = 900f
            return maxRange
        }
    }


    override fun apply(stats: MutableShipStatsAPI?, id: String?,  state: ShipSystemStatsScript.State?, effectLevel: Float) {
        ship = stats!!.entity as ShipAPI
        var id = id + "_" + ship!!.getId()
        var system = ship!!.system

        if (!system.isActive) {

            if (target != null) {
                target!!.isPhased = false
                target!!.mutableStats.hullDamageTakenMult.unmodify(id)
                target!!.alphaMult = 1f

                for (wing in target!!.allWings) {
                    for (fighter in wing.wingMembers) {
                        fighter.isPhased = false
                    }
                }

                target = null
            }


        }

        if (system.isActive) {
            if (target == null) {
                target = findTarget()
                if (target != null) {
                    Global.getCombatEngine().addFloatingText(target!!.location, system.displayName, 22f, color, target, 0f, 0f)
                }
            }

            var effectLevel = easeInOutSine(effectLevel)

            if (target != null) {
                Global.getSoundPlayer().playLoop("rat_temporal_prison_loop", target, 1f, 1f, target!!.location, target!!.velocity)


                if (effectLevel >= 0.5f) {

                    for (weapon in target!!.allWeapons) {
                        weapon.setRemainingCooldownTo(weapon.cooldown)
                    }

                    target!!.isHoldFireOneFrame = true
                    target!!.mutableStats.hullDamageTakenMult.modifyMult(id, 0f)
                    target!!.isPhased = true
                }
                else {
                    target!!.isPhased = false
                    target!!.mutableStats.hullDamageTakenMult.unmodify(id)
                }

                var player = target == Global.getCombatEngine().playerShip
                var targetStats = target!!.mutableStats

                target!!.alphaMult = 1f - (0.95f * effectLevel)

                //target!!.setJitter(this, color.setAlpha(25), 1f * effectLevel, 3, 0f, 5 * effectLevel)
                target!!.setJitterUnder(this, color, 1f * effectLevel, 25, 0f, 6 * effectLevel)

                val shipTimeMult = 1f - (0.666f * effectLevel)
                targetStats.timeMult.modifyMult(id, shipTimeMult)
                if (player) {
                    Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
                } else {
                    Global.getCombatEngine().timeMult.unmodify(id)
                }


                afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
                if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
                {
                    var decrease = 0f
                    if (state == ShipSystemStatsScript.State.OUT) {
                        decrease += 2 * (1-effectLevel)
                    }
                    AfterImageRenderer.addAfterimage(target!!, color.setAlpha(75), color.setAlpha(25), 2+ 3f * effectLevel, 1f, Vector2f().plus(target!!.location))
                }

                target!!.engineController.fadeToOtherColor(this, color, color.setAlpha(5) ,effectLevel,1f)
                target!!.engineController.extendFlame(this, -0.25f, -0.25f, -0.25f)


                var addFighterAftershadow = false
                afterimageIntervalFighter.advance(Global.getCombatEngine().elapsedInLastFrame)
                if (afterimageIntervalFighter.intervalElapsed() && !Global.getCombatEngine().isPaused) {
                    addFighterAftershadow = true
                }


                var others = mutableListOf<ShipAPI>()
                others.addAll(target!!.childModulesCopy)

                for (wing in target!!.allWings) {
                    var combined = mutableListOf<ShipAPI>()
                    combined.addAll(wing.wingMembers)
                    combined.addAll(wing.returning.map { it.fighter })
                    combined.distinct()

                    others.addAll(combined)
                }

                others.distinct()


                for (targetComponent in others) {
                    if (effectLevel >= 0.2f) {

                        for (weapon in target!!.allWeapons) {
                            weapon.setRemainingCooldownTo(weapon.cooldown)
                        }

                        targetComponent!!.isHoldFireOneFrame = true
                        targetComponent!!.mutableStats.hullDamageTakenMult.modifyMult(id, 0f)
                        targetComponent!!.isPhased = true
                    }
                    else {
                        targetComponent!!.isPhased = false
                        targetComponent!!.mutableStats.hullDamageTakenMult.unmodify(id)
                    }

                    targetComponent!!.alphaMult = 1f - (0.95f * effectLevel)

                    targetComponent!!.setJitterUnder(this.toString() + targetComponent.toString(), color, 1f * effectLevel, 25, 0f, 6 * effectLevel)

                    targetComponent.mutableStats.timeMult.modifyMult(id, shipTimeMult)

                    if (addFighterAftershadow)
                    {
                        AfterImageRenderer.addAfterimage(targetComponent!!, color.setAlpha(75), color.setAlpha(25), 2+ 3f * effectLevel, 1f, Vector2f().plus(targetComponent!!.location))

                    }

                    targetComponent!!.engineController.fadeToOtherColor(this, color, color.setAlpha(5) ,effectLevel,1f)
                    targetComponent!!.engineController.extendFlame(this, -0.25f, -0.25f, -0.25f)
                }
            }
        }
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }

    fun findTarget() : ShipAPI? {
        if (ship == null) return null

        var target = ship!!.shipTarget
        if (ship!!.shipAI != null && ship!!.aiFlags.hasFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)) {

            target = ship!!.aiFlags.getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM) as ShipAPI?
        }


        return target
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        var target = findTarget()
        if (target != null) {
            var distance = MathUtils.getDistance(ship, target)
            if (distance <= getMaxRange() && !target.isStation && target.parentStation == null && target.owner != ship!!.owner) {
                return true
            }
        }

        return false
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String? {
        if (ship == null) return ""

        var target = findTarget() ?: return "No Target"

        if (target.owner == ship.owner) return "Can not target friendly ships"
        if (target.isStation) return "Does not work on Stations"
        if (target.parentStation != null) return "Only works on the core part of the ship"

        var distance = MathUtils.getDistance(ship, target)
        if (distance >= getMaxRange()) return "Target out of Range"

        return super.getInfoText(system, ship)
    }




}