package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.loading.WeaponGroupSpec
import com.fs.starfarer.api.loading.WeaponGroupType
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

class ThestiaShipsystem : BaseShipSystemScript() {

    var activated = false
    var ship: ShipAPI? = null
    val color = ExoUtils.color2

    var aftershadows = ArrayList<ShipAPI>()
    var afterimageInterval = IntervalUtil(0.1f, 0.1f)

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)
        ship = stats!!.entity as ShipAPI? ?: return



        var system = ship!!.system

        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false
        }

        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            aftershadows.clear()

            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true

            var activeWings = ship!!.allWings
            for (wing in activeWings) {
                var fighters = wing.wingMembers + wing.returning.map { it.fighter }
                fighters = fighters.distinct()
                for (fighter in  fighters) {
                    //if (!fighter.isAlive) continue


                    var leftShadow = spawnClone(fighter)
                        ?: continue
                    leftShadow.setCustomData("rat_shadow_original", fighter)
                    leftShadow.velocity.set(Vector2f(fighter.velocity))
                    leftShadow.location.set(MathUtils.getPointOnCircumference(fighter.location, 10f, fighter.facing + 120))
                    leftShadow.alphaMult = 0f
                    aftershadows.add(leftShadow)

                    var rightShadow = spawnClone(fighter)
                        ?: continue
                    rightShadow.setCustomData("rat_shadow_original", fighter)
                    rightShadow.velocity.set(Vector2f(fighter.velocity))
                    rightShadow.location.set(MathUtils.getPointOnCircumference(fighter.location, 10f, fighter.facing - 120))
                    rightShadow.alphaMult = 0f
                    aftershadows.add(rightShadow)




                }
            }



            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false

        }

        afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
        var elapsed = afterimageInterval.intervalElapsed()
        for (aftershadow in aftershadows) {



            aftershadow.setShipSystemDisabled(true)
            aftershadow.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
            // aftershadow.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF)
            //aftershadow.aiFlags.removeFlag(ShipwideAIFlags.AIFlags.BACK_OFF)
           // aftershadow.aiFlags.removeFlag(ShipwideAIFlags.AIFlags.BIGGEST_THREAT)
            //aftershadow.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)

            var test = aftershadow.aiFlags

            var original = aftershadow.customData.get("rat_shadow_original") as ShipAPI

            aftershadow.collisionClass = CollisionClass.NONE
            //aftershadow.alphaMult = effectLevel * 0.2f
            //aftershadow.alphaMult = effectLevel * 1f
            //aftershadow.alphaMult = effectLevel * 0f
            aftershadow.alphaMult = effectLevel * 0.05f
            aftershadow.setJitterUnder(this, color.setAlpha(255), 1f * effectLevel, 10, 2f, 8f)
          //  aftershadow.setJitterUnder("rat_system_1", Color(248,172,44, 200), 1f * effectLevel, 10, 1f, 6f)
           // aftershadow.engineController.fadeToOtherColor(this, color.setAlpha(100), color.setAlpha(0), 1f, 1f)

            if (elapsed && !Global.getCombatEngine().isPaused && aftershadow.isAlive) {
                AfterImageRenderer.addAfterimageWithSpritepath(aftershadow, color.setAlpha((125 * effectLevel).toInt()), Color(130,4,189, 0), 1f, 6f, Vector2f().plus(aftershadow.location), original.baseOrModSpec().spriteName)
            }

            if (state == ShipSystemStatsScript.State.IN) {
                var velocity = MathUtils.getPointOnCircumference(Vector2f(), 2 - (2 * effectLevel), Misc.getAngleInDegrees(original.location, aftershadow.location))
                aftershadow.velocity.set(aftershadow.velocity.plus(velocity))
            }

            if (state == ShipSystemStatsScript.State.ACTIVE) {
                var distance = MathUtils.getDistance(aftershadow.location, original.location)
                aftershadow.customData.set("rat_shadow_distance", distance)

                if (aftershadow.customData.get("rat_shadow_triggered_active") != true) {
                    aftershadow.shipAI = Global.getSettings().createDefaultShipAI(aftershadow, ShipAIConfig())
                    aftershadow.shipAI.forceCircumstanceEvaluation()
                }

                aftershadow.customData.set("rat_shadow_triggered_active", true)
            }

            if (state == ShipSystemStatsScript.State.OUT && original.isAlive) {
               // aftershadow.velocity.set(aftershadow.velocity.x * effectLevel, aftershadow.velocity.y * effectLevel)
                aftershadow.velocity.set(Vector2f())
                aftershadow.shipAI = null

                var distance = aftershadow.customData.get("rat_shadow_distance") as Float? ?: 0f
                var angle =  Misc.getAngleInDegrees(original.location, aftershadow.location)
                var location = MathUtils.getPointOnCircumference(original.location, distance * effectLevel, angle)
                aftershadow.location.set(location)

                if (aftershadow.facing > original.facing + 1) {
                    aftershadow.facing -= 200f * (1 - effectLevel) * Global.getCombatEngine().elapsedInLastFrame
                }
                if (aftershadow.facing < original.facing + 1) {
                    aftershadow.facing += 200f * (1 - effectLevel) * Global.getCombatEngine().elapsedInLastFrame
                }
            }

            if (state == ShipSystemStatsScript.State.COOLDOWN) {
                if (aftershadow.isAlive) {
                    var manager = Global.getCombatEngine().getFleetManager(aftershadow.owner)
                    aftershadow.isPhased = false
                    aftershadow.alphaMult = 0f
                    aftershadow.location.set(Vector2f(50000f, 50000f))

                    manager.removeDeployed(aftershadow, true)
                }
            }


        }
    }

    fun spawnClone(originalFighter: ShipAPI) : ShipAPI {

        val member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_aftershadow_drone_Hull")
        member.owner = originalFighter.owner
        member.crewComposition.addCrew(member.neededCrew)

        member.captain = originalFighter.captain

        var spec = Global.getSettings().getHullSpec("rat_aftershadow_drone")
        var variant = Global.getSettings().createEmptyVariant("dem_drone", spec)
        var originalVariant = originalFighter.variant

        var id = 0
        for (slotID in originalVariant.fittedWeaponSlots) {
            var ogSlot = originalVariant.getSlot(slotID)
            if (ogSlot.weaponType == WeaponAPI.WeaponType.DECORATIVE) continue
            if (ogSlot.isDecorative) continue
            if (ogSlot.isStationModule) continue
            if (ogSlot.isSystemSlot) continue

            id+=1
            var stringID = id.toString()
            var slot = variant.getSlot(stringID) ?: break
            slot.arc = ogSlot.arc
            slot.angle = ogSlot.angle
           /* slot.renderOrderMod = ogSlot.renderOrderMod
            slot.location.set(ogSlot.location)*/

            variant.addWeapon(stringID, originalVariant.getWeaponId(slotID))
            val group = WeaponGroupSpec(WeaponGroupType.LINKED)
            group.addSlot(stringID)
            group.isAutofireOnByDefault = true
            variant.addWeaponGroup(group)
        }

       /* if (originalVariant.hasHullMod("rat_exo_fighter_ai")) {
            variant.addPermaMod("rat_exo_fighter_ai")
        }*/

        member.setVariant(variant, true, true)


        applyStats(member.stats, originalFighter.mutableStats)


        var clone = Global.getCombatEngine().getFleetManager(originalFighter.owner).spawnFleetMember(member, originalFighter.location, originalFighter.facing, 0f)
        var stats = clone.mutableStats
        var ogStats = originalFighter.mutableStats



        applyStats(stats, ogStats)

        clone.mass = originalFighter.mass
        clone.collisionRadius = originalFighter.collisionRadius

        clone.maxHitpoints = originalFighter.maxHitpoints
        clone.hitpoints = originalFighter.hitpoints




        if (originalFighter.shield != null && originalFighter.shield.type != ShieldAPI.ShieldType.NONE) {
            clone.setShield(originalFighter.shield.type, originalFighter.shield.upkeep, originalFighter.shield.fluxPerPointOfDamage, originalFighter.shield.arc)
            clone.shield.radius = originalFighter.shield.radius
        }
        else {
            clone.setShield(ShieldAPI.ShieldType.NONE, 0f, 0f, 0f)
        }

        if (originalFighter.shipTarget != null) {
            clone.shipTarget = originalFighter.shipTarget
        }

        for (weapon in clone.allWeapons) {
            weapon.ensureClonedSpec()
            weapon.spec.aiHints.remove(WeaponAPI.AIHints.USE_LESS_VS_SHIELDS)
        }

        clone.shipAI = null
        clone.shipAI = Global.getSettings().createDefaultShipAI(clone, ShipAIConfig().apply { personalityOverride = Personalities.RECKLESS })
        clone.shipAI.forceCircumstanceEvaluation()
        clone.alphaMult = 0f

        return clone
    }

    fun applyStats(stats: MutableShipStatsAPI, ogStats: MutableShipStatsAPI) {
        stats.maxSpeed.baseValue = ogStats.maxSpeed.baseValue
        stats.acceleration.baseValue = ogStats.acceleration.baseValue
        stats.deceleration.baseValue = ogStats.deceleration.baseValue
        stats.turnAcceleration.baseValue = ogStats.turnAcceleration.baseValue
        stats.maxTurnRate.baseValue = ogStats.maxTurnRate.baseValue

       /* stats.ballisticWeaponRangeBonus.applyMods(ogStats.ballisticWeaponRangeBonus)
        stats.energyWeaponRangeBonus.applyMods(ogStats.energyWeaponRangeBonus)
        stats.missileWeaponRangeBonus.applyMods(ogStats.missileWeaponRangeBonus)*/

       /* stats.damageToCapital.modifyMult("rat_shadow", 0.6f)
        stats.damageToCruisers.modifyMult("rat_shadow", 0.6f)
        stats.damageToDestroyers.modifyMult("rat_shadow", 0.6f)
        stats.damageToFrigates.modifyMult("rat_shadow", 0.6f)*/

        stats.ballisticWeaponDamageMult.modifyMult("rat_shadow", 0.6f)
        stats.energyWeaponDamageMult.modifyMult("rat_shadow", 0.6f)
        stats.missileWeaponDamageMult.modifyMult("rat_shadow", 0.6f)

       /* stats.fluxDissipation.baseValue = ogStats.fluxDissipation.baseValue
        stats.fluxCapacity.baseValue = ogStats.fluxCapacity.baseValue*/
    }


}