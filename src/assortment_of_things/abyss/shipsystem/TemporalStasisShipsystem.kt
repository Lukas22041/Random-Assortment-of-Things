package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.exotech.shipsystems.ArkasShipsystem
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class TemporalStasisShipsystem : BaseShipSystemScript(), AdvanceableListener {

    var ship: ShipAPI? = null
    var target: ShipAPI? = null
    var afterimageInterval = IntervalUtil(0.1f, 0.1f)
    var afterimageIntervalFighter = IntervalUtil(0.1f, 0.1f)

    var color = Color(196, 20, 35, 255)

    var timer = 0f
    var isCountingTimer = false
    var actualIn = 2f
    var actualActive = 10f
    var maxTime = actualActive + actualIn

    var changedPostProcess = false

    companion object {

        fun getMaxRange() : Float {
           // if (Global.getSettings().isDevMode) return 2000f
            var maxRange = 900f
            return maxRange
        }
    }


    override fun apply(stats: MutableShipStatsAPI?, id: String?,  state: ShipSystemStatsScript.State?, effectLevel: Float) {
        ship = stats!!.entity as ShipAPI
        var id = id + "_" + ship!!.getId()
        var system = ship!!.system

        if (!ship!!.hasListenerOfClass(TemporalStasisShipsystem::class.java)) {
            ship!!.addListener(this)
        }

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

                isCountingTimer = false
                target = null

                if (changedPostProcess) {
                    changedPostProcess = false
                    PostProcessShader.resetDefaults()
                }
            }


        }

        var levelForLens = 0f

        if (system.isActive) {
            if (target == null) {
                target = findTarget()
                if (target != null) {
                    if (target == ship!!.shipTarget) {
                        ship!!.shipTarget = null
                    }
                    Global.getCombatEngine().addFloatingText(target!!.location, system.displayName, 22f, color, target, 0f, 0f)
                    isCountingTimer = true
                }
            }

            var stateLevel = timer / actualIn
            stateLevel = MathUtils.clamp(stateLevel, 0f, 1f)
            if (state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.ACTIVE) {
                stateLevel = effectLevel
            }

            var effectLevel = easeInOutSine(stateLevel)
            levelForLens = effectLevel

            if (target != null) {
                Global.getSoundPlayer().playLoop("rat_temporal_prison_loop", target, 1f, 1f, target!!.location, target!!.velocity)


                if (effectLevel >= 0.5f) {

                    for (weapon in target!!.allWeapons) {
                        //weapon.setRemainingCooldownTo(weapon.cooldown)
                        weapon.stopFiring()
                    }

                    target!!.isHoldFireOneFrame = true
                    target!!.mutableStats.hullDamageTakenMult.modifyMult(id, 0f)
                    target!!.isPhased = true
                }
                else {
                    target!!.isPhased = false
                    target!!.mutableStats.hullDamageTakenMult.unmodify(id)
                }

                //Remove the stat mod a bit earlier than everything else so that nearby ships notice & target the ship as the duration ends
                if (state == ShipSystemStatsScript.State.OUT) {
                    target!!.mutableStats.hullDamageTakenMult.unmodify(id)
                }

                var player = target == Global.getCombatEngine().playerShip
                var targetStats = target!!.mutableStats

                target!!.alphaMult = 1f - (0.95f * effectLevel)

                //target!!.setJitter(this, color.setAlpha(25), 1f * effectLevel, 3, 0f, 5 * effectLevel)
                target!!.setJitterUnder(this, color, 1f * effectLevel, 25, 0f, 6 * effectLevel)

                if (player) {
                    var path = "graphics/icons/hullsys/high_energy_focus.png"
                    Global.getSettings().getAndLoadSprite(path)

                    Global.getCombatEngine().maintainStatusForPlayerShip("rat_temporal_stasis",
                        path,
                        "Temporal Stasis",
                        "Stuck in time",
                        true)

                    PostProcessShader.setNoise(false, 0.4f * effectLevel)

                    PostProcessShader.setSaturation(false, 1f + (0.3f * effectLevel))

                    changedPostProcess = true
                } else if (changedPostProcess) {
                    changedPostProcess = false
                    PostProcessShader.resetDefaults()
                }


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

                        for (weapon in targetComponent!!.allWeapons) {
                            weapon.setRemainingCooldownTo(weapon.cooldown * 0.5f)
                            weapon.stopFiring()
                        }

                        targetComponent!!.isHoldFireOneFrame = true
                        targetComponent!!.mutableStats.hullDamageTakenMult.modifyMult(id, 0f)
                        targetComponent!!.isPhased = true

                        for (weapon in targetComponent.allWeapons) {
                            weapon.stopFiring()
                        }
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

        var phaseLevel = ship!!.customData.get("rat_phase_level") as Float? ?: 0f

        if (phaseLevel >= levelForLens) {
            levelForLens = phaseLevel
        }
        ship!!.setCustomData("rat_lensflare_level_overwrite", levelForLens)

    }

    override fun advance(amount: Float) {
        if (isCountingTimer && ship!!.system.state != ShipSystemAPI.SystemState.OUT) {
            timer += 1f * amount / ship!!.mutableStats.timeMult.modifiedValue
            var stateLevel = timer / maxTime
            if (stateLevel >= 1) {
                //ship!!.system.forceState(ShipSystemAPI.SystemState.ACTIVE, maxTime - 1f)
                isCountingTimer = false
                timer = 0f
            }
            else {
                var level = stateLevel
                ship!!.system.forceState(ShipSystemAPI.SystemState.IN, level)
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
            if (distance <= getMaxRange() && !target.isStation && target.parentStation == null && target.owner != ship!!.owner && !target.isHulk && target.isAlive) {
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
        if (target.isHulk) return "Does not work on hulks"

        var distance = MathUtils.getDistance(ship, target)
        if (distance >= getMaxRange()) return "Target out of Range"

        return super.getInfoText(system, ship)
    }




}