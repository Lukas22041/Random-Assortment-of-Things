package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.CombatFleetManager
import com.fs.starfarer.combat.entities.Ship
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color


//Only here to test funny stuff
class ArkasShipsystem : BaseShipSystemScript(), HullDamageAboutToBeTakenListener, AdvanceableListener {

    var ship: ShipAPI? = null

    var phantoms = ArrayList<ShipAPI>()
    //var clone: ShipAPI? = null

    var afterimageInterval = IntervalUtil(0.05f, 0.05f)

    val color = ExoUtils.color2

    var activated = false

    var timer = 0f
    var isCountingTimer = false
    var actualIn = 1f
    var actualActive = 11f
    var maxTime = actualActive + actualIn


    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        if (stats!!.entity == null) return

        ship = stats!!.entity as ShipAPI
        var system = ship!!.system

        if (ship!!.variant.hasTag("Arkas-Phantom")) return

        if (!ship!!.hasListenerOfClass(ArkasShipsystem::class.java)) {
            ship!!.addListener(this)
        }

        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false
        }

        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            phantoms.clear()

            isCountingTimer = true

            var leftPhantom = spawnPhantom()
            leftPhantom.velocity.set(Vector2f(ship!!.velocity))
            leftPhantom.location.set(MathUtils.getPointOnCircumference(ship!!.location, 10f, ship!!.facing + 110))

            var rightPhantom = spawnPhantom()
            rightPhantom.velocity.set(Vector2f(ship!!.velocity))
            rightPhantom.location.set(MathUtils.getPointOnCircumference(ship!!.location, 10f, ship!!.facing - 110))
        }



        var stateLevel = timer / actualIn
        stateLevel = MathUtils.clamp(stateLevel, 0f, 1f)
        if (state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.ACTIVE) {
            stateLevel = effectLevel
        }

        ship!!.setCustomData("rat_exogrid_level_override", stateLevel)

        afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
        var elapsed = afterimageInterval.intervalElapsed()
        for (phantom in ArrayList(phantoms)) {

            //phantom.phaseCloak.forceState(ShipSystemAPI.SystemState.IDLE, 0f)
            phantom.collisionClass = CollisionClass.NONE
            phantom.setShipSystemDisabled(true)
            phantom.isDefenseDisabled = true
            phantom.alphaMult = stateLevel * 0.05f
            phantom.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_VENT)
           // phantom.setJitterUnder(this, color.setAlpha(50), 1f * effectLevel, 10, 4f, 14f)

            if (phantom.shipTarget == null) {
                phantom.giveCommand(ShipCommand.ACCELERATE, null, 0)
            }

            /*if (MathUtils.getDistance(ship!!, phantom) <= ship!!.collisionRadius) {
                phantom.isHoldFireOneFrame = true
            }*/

            if (elapsed && !Global.getCombatEngine().isPaused && phantom.isAlive) {
                AfterImageRenderer.addAfterimage(phantom, color.setAlpha((30 * stateLevel).toInt()), Color(130,4,189, 0), 2f, 0f, Vector2f(phantom.location), false)
            }

            if (state == ShipSystemStatsScript.State.IN && timer <= actualIn && !Global.getCombatEngine().isPaused) {
                for (weapon in ship!!.allWeapons) {
                    weapon.setForceNoFireOneFrame(true)
                }

                phantom.isHoldFireOneFrame = true
                var lastFrame = Global.getCombatEngine().elapsedInLastFrame
                var timeMod = ship!!.mutableStats.timeMult.modifiedValue
                var velocity = MathUtils.getPointOnCircumference(Vector2f(), 300  - (250f * stateLevel / ship!!.mutableStats.timeMult.modifiedValue), Misc.getAngleInDegrees(ship!!.location, phantom.location))
                phantom.velocity.set(phantom.velocity.plus(Vector2f(velocity.x * lastFrame / timeMod, velocity.y * lastFrame / timeMod)))
            }

            if (state == ShipSystemStatsScript.State.IN && timer > actualIn && !Global.getCombatEngine().isPaused) {
                var distance = MathUtils.getDistance(phantom.location, ship!!.location)
                phantom.setCustomData("rat_phantom_distance", distance)

                if (phantom!!.shipAI == null && phantom != Global.getCombatEngine().playerShip) {
                    phantom.shipAI = Global.getSettings().createDefaultShipAI(phantom, ShipAIConfig())
                    phantom.shipAI.forceCircumstanceEvaluation()
                }
            }

            if (state == ShipSystemStatsScript.State.OUT && ship!!.isAlive && !Global.getCombatEngine().isPaused) {
                phantom.isHoldFireOneFrame = true
                phantom.velocity.set(Vector2f())
                phantom.shipAI = null

                var distance = phantom.customData.get("rat_phantom_distance") as Float? ?: 0f
                var angle =  Misc.getAngleInDegrees(ship!!.location, phantom.location)
                var location = MathUtils.getPointOnCircumference(ship!!.location, distance * stateLevel, angle)
                phantom.location.set(location)

                if (phantom.facing > ship!!.facing + 1) {
                    phantom.facing -= 200f * (1 - stateLevel) * Global.getCombatEngine().elapsedInLastFrame
                }
                if (phantom.facing < ship!!.facing + 1) {
                    phantom.facing += 200f * (1 - stateLevel) * Global.getCombatEngine().elapsedInLastFrame
                }
            }

            if (state == ShipSystemStatsScript.State.COOLDOWN) {
                Global.getCombatEngine().removeEntity(phantom)
                for (wing in phantom.allWings) {
                    for (fighter in wing.wingMembers) {
                        fighter.splitShip()
                        fighter.hitpoints = 0f
                    }
                }
                phantoms.remove(phantom)

                isCountingTimer = false
                timer = 0f
            }
        }
    }

    fun spawnPhantom() : ShipAPI {
        var variant = ship!!.variant.clone()

        var spec = Global.getSettings().getHullSpec("rat_arkas_phantom")
        variant.setHullSpecAPI(spec)

        for (slotID in variant.fittedWeaponSlots) {
            var slot = variant.getSlot(slotID) ?: continue
            if (slot.isBuiltIn) {
                variant.clearSlot(slotID)
            }
        }

        variant.addTag("Arkas-Phantom")
        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true
        var phantom = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, 1f, Vector2f(100000f, 100000f), ship!!.facing)
        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false

        var manager = Global.getCombatEngine().getFleetManager(phantom!!.owner)
        manager.removeDeployed(phantom, true)

        Global.getCombatEngine().addEntity(phantom)
        phantom.shipAI = null

        var stats = phantom.mutableStats
       /* stats.damageToCapital.modifyMult("rat_phantom", 0.50f)
        stats.damageToCruisers.modifyMult("rat_phantom", 0.50f)
        stats.damageToDestroyers.modifyMult("rat_phantom", 0.50f)
        stats.damageToFrigates.modifyMult("rat_phantom", 0.50f)
        stats.damageToFighters.modifyMult("rat_phantom", 0.50f)*/

        stats.ballisticWeaponDamageMult.modifyMult("rat_phantom", 0.5f)
        stats.energyWeaponDamageMult.modifyMult("rat_phantom", 0.5f)
        stats.missileWeaponDamageMult.modifyMult("rat_phantom", 0.5f)

        stats.ballisticWeaponRangeBonus.modifyFlat("rat_phantom", 50f)
        stats.energyWeaponRangeBonus.modifyFlat("rat_phantom", 50f)
        stats.missileWeaponRangeBonus.modifyFlat("rat_phantom", 50f)

        stats.maxSpeed.modifyFlat("rat_phantom", 20f)
        stats.acceleration.modifyFlat("rat_phantom", 30f)
        stats.deceleration.modifyFlat("rat_phantom", 30f)
        stats.maxTurnRate.modifyFlat("rat_phantom", 30f)
        stats.turnAcceleration.modifyFlat("rat_phantom", 30f)

        stats.timeMult.modifyMult("rat_phantom", 1.1f)

        //Should make AI ignore it
        stats.hullDamageTakenMult.modifyMult("rat_phantom", 0f)

        for (i in 0 until ship!!.allWeapons.size) {
            var original = ship!!.allWeapons.getOrNull(i) ?: continue
            var new = phantom!!.allWeapons.getOrNull(i) ?: continue

            new.setRemainingCooldownTo(0.2f)
            new.setForceNoFireOneFrame(true)
            //new.ammo = original.ammo
        }

        for (weapon in phantom.allWeapons) {
            weapon.ensureClonedSpec()
            weapon.spec.aiHints.add(WeaponAPI.AIHints.DO_NOT_CONSERVE)
            weapon.spec.aiHints.remove(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS)
        }

        phantom.setCustomData("rat_phantom_parent", ship)

        phantoms.add(phantom!!)
        return phantom
    }

    fun spawnShipOrWingDirectly(variant: ShipVariantAPI?, type: FleetMemberType?, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI? {
        val member = Global.getFactory().createFleetMember(type, variant)
        member.owner = owner
        member.crewComposition.addCrew(member.neededCrew)
        member.captain = ship!!.captain

        val clone = Global.getCombatEngine().getFleetManager(owner).spawnFleetMember(member, location, facing, 0f)
        clone.crAtDeployment = combatReadiness
        clone.currentCR = combatReadiness
        clone.owner = owner
        clone.shipAI.forceCircumstanceEvaluation()
        return clone
    }


    override fun notifyAboutToTakeHullDamage(param: Any?,  ship: ShipAPI?, point: Vector2f?,damageAmount: Float): Boolean {
        
        if (param is ShipAPI) {
            if (param.variant.hasTag("Arkas-Phantom")) {
                return false
            }
        }

        return false
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        for (phantom in ArrayList(phantoms)) {
            Global.getCombatEngine().removeEntity(phantom)
            for (wing in phantom.allWings) {
                for (fighter in wing.wingMembers) {
                    fighter.splitShip()
                    fighter.hitpoints = 0f
                }
            }
            phantoms.remove(phantom)
        }

        isCountingTimer = false
        timer = 0f
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
}