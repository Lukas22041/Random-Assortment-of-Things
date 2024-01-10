package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
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
class ArkasShipsystem : BaseShipSystemScript(), HullDamageAboutToBeTakenListener {

    var ship: ShipAPI? = null

    var phantoms = ArrayList<ShipAPI>()
    //var clone: ShipAPI? = null

    var afterimageInterval = IntervalUtil(0.05f, 0.05f)

    val color = ExoUtils.color2

    var activated = false

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


            var leftPhantom = spawnPhantom()
            leftPhantom.velocity.set(Vector2f(ship!!.velocity))
            leftPhantom.location.set(MathUtils.getPointOnCircumference(ship!!.location, 10f, ship!!.facing + 110))

            var rightPhantom = spawnPhantom()
            rightPhantom.velocity.set(Vector2f(ship!!.velocity))
            rightPhantom.location.set(MathUtils.getPointOnCircumference(ship!!.location, 10f, ship!!.facing - 110))
        }

        afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
        var elapsed = afterimageInterval.intervalElapsed()
        for (phantom in phantoms) {

            phantom.phaseCloak.forceState(ShipSystemAPI.SystemState.IDLE, 0f)
            phantom.collisionClass = CollisionClass.NONE
            phantom.setShipSystemDisabled(true)
            phantom.isDefenseDisabled = true
            phantom.alphaMult = effectLevel * 0.05f
            phantom.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_VENT)
           // phantom.setJitterUnder(this, color.setAlpha(50), 1f * effectLevel, 10, 4f, 14f)

            if (phantom.shipTarget == null) {
                phantom.giveCommand(ShipCommand.ACCELERATE, null, 0)
            }

            if (MathUtils.getDistance(ship!!, phantom) <= ship!!.collisionRadius) {
                phantom.isHoldFireOneFrame = true
            }

            if (elapsed && !Global.getCombatEngine().isPaused && phantom.isAlive) {
                AfterImageRenderer.addAfterimage(phantom, color.setAlpha((30 * effectLevel).toInt()), Color(130,4,189, 0), 2f, 0f, Vector2f(phantom.location), false)
            }

            if (state == ShipSystemStatsScript.State.IN) {
                for (weapon in ship!!.allWeapons) {
                    weapon.setForceNoFireOneFrame(true)
                }

                phantom.isHoldFireOneFrame = true
                var elapsed = Global.getCombatEngine().elapsedInLastFrame
                var velocity = MathUtils.getPointOnCircumference(Vector2f(), 300 - (250f * effectLevel), Misc.getAngleInDegrees(ship!!.location, phantom.location))
                phantom.velocity.set(phantom.velocity.plus(Vector2f(velocity.x * elapsed, velocity.y * elapsed)))
            }

            if (state == ShipSystemStatsScript.State.ACTIVE) {
                var distance = MathUtils.getDistance(phantom.location, ship!!.location)
                phantom.setCustomData("rat_phantom_distance", distance)

                if (phantom!!.shipAI == null) {
                    phantom.shipAI = Global.getSettings().createDefaultShipAI(phantom, ShipAIConfig())
                    phantom.shipAI.forceCircumstanceEvaluation()
                }
            }

            if (state == ShipSystemStatsScript.State.OUT && ship!!.isAlive) {
                phantom.isHoldFireOneFrame = true
                phantom.velocity.set(Vector2f())
                phantom.shipAI = null

                var distance = phantom.customData.get("rat_phantom_distance") as Float? ?: 0f
                var angle =  Misc.getAngleInDegrees(ship!!.location, phantom.location)
                var location = MathUtils.getPointOnCircumference(ship!!.location, distance * effectLevel, angle)
                phantom.location.set(location)

                if (phantom.facing > ship!!.facing + 1) {
                    phantom.facing -= 200f * (1 - effectLevel) * Global.getCombatEngine().elapsedInLastFrame
                }
                if (phantom.facing < ship!!.facing + 1) {
                    phantom.facing += 200f * (1 - effectLevel) * Global.getCombatEngine().elapsedInLastFrame
                }
            }

            if (state == ShipSystemStatsScript.State.COOLDOWN) {
                Global.getCombatEngine().removeEntity(phantom)
            }
        }
    }

    fun spawnPhantom() : ShipAPI {
        var variant = ship!!.variant.clone()

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
        var obfManager = manager as CombatFleetManager
        obfManager.removeDeployed(phantom as Ship, true)

        Global.getCombatEngine().addEntity(phantom)
        phantom.shipAI = null

        var stats = phantom.stats
        stats.damageToCapital.modifyMult("rat_phantom", 0.33f)
        stats.damageToCruisers.modifyMult("rat_phantom", 0.33f)
        stats.damageToDestroyers.modifyMult("rat_phantom", 0.33f)
        stats.damageToFrigates.modifyMult("rat_phantom", 0.33f)
        stats.damageToFighters.modifyMult("rat_phantom", 0.33f)

        stats.maxSpeed.modifyFlat("rat_phantom", 20f)
        stats.acceleration.modifyFlat("rat_phantom", 30f)
        stats.deceleration.modifyFlat("rat_phantom", 30f)
        stats.maxTurnRate.modifyFlat("rat_phantom", 30f)
        stats.turnAcceleration.modifyFlat("rat_phantom", 30f)

        stats.timeMult.modifyMult("rat_phantom", 1.1f)

        for (i in 0 until ship!!.allWeapons.size) {
            var original = ship!!.allWeapons.getOrNull(i) ?: continue
            var new = phantom!!.allWeapons.getOrNull(i) ?: continue

            new.setRemainingCooldownTo(0.2f)
            new.setForceNoFireOneFrame(true)
            //new.ammo = original.ammo
        }

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
}