package assortment_of_things.exotech.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ThestiaShipsystem : BaseShipSystemScript() {

    var activated = false
    var ship: ShipAPI? = null
    val color = Color(248,172,44, 255)

    var aftershadows = ArrayList<ShipAPI>()

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)
        ship = stats!!.entity as ShipAPI? ?: return

        var system = ship!!.system

        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false

            for (aftershadow in aftershadows) {
                aftershadow.hitpoints = 0f
            }
        }

        if (!activated && system.state == ShipSystemAPI.SystemState.IN) {
            activated = true

            aftershadows.clear()

            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true

            var activeWings = ship!!.allWings
            for (wing in activeWings) {
                for (fighter in  wing.wingMembers) {
                    if (!fighter.isAlive) continue
                    var leftShadow = spawnShipOrWingDirectly(fighter, fighter.variant, FleetMemberType.SHIP, fighter.owner, 1f, fighter.location, fighter.facing)
                    leftShadow.setCustomData("rat_shadow_original", fighter)
                    leftShadow.velocity.set(fighter.velocity)
                    leftShadow.location.set(MathUtils.getPointOnCircumference(fighter.location, 10f, fighter.facing + 120))
                    aftershadows.add(leftShadow)

                    var rightShadow = spawnShipOrWingDirectly(fighter, fighter.variant, FleetMemberType.SHIP, fighter.owner, 1f, fighter.location, fighter.facing)
                    rightShadow.setCustomData("rat_shadow_original", fighter)
                    rightShadow.velocity.set(fighter.velocity)
                    rightShadow.location.set(MathUtils.getPointOnCircumference(fighter.location, 10f, fighter.facing - 120))
                    aftershadows.add(rightShadow)

                    /*var leftVelocity = MathUtils.getPointOnCircumference(Vector2f(), 300f, fighter.facing + 130)
                    leftShadow.velocity.set(fighter.velocity)
                    leftShadow.velocity.set(leftShadow.velocity.plus(leftVelocity))*/

                }
            }



            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false

        }

        for (aftershadow in aftershadows) {

            var original = aftershadow.customData.get("rat_shadow_original") as ShipAPI

            aftershadow.collisionClass = CollisionClass.NONE
            aftershadow.alphaMult = effectLevel * 0.2f
            aftershadow.setJitterUnder(this, color, 1f * effectLevel, 10, 2f, 8f)

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

                var distance = aftershadow.customData.get("rat_shadow_distance") as Float
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


        }
    }

    fun spawnShipOrWingDirectly(originalFighter: ShipAPI, variant: ShipVariantAPI?, type: FleetMemberType?, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI {
        val member = Global.getFactory().createFleetMember(type, variant)
        member.owner = owner
        member.crewComposition.addCrew(member.neededCrew)

        member.captain = ship!!.captain

        val fighter = Global.getCombatEngine().getFleetManager(owner).spawnFleetMember(member, location, facing, 0f)
        fighter.crAtDeployment = combatReadiness
        fighter.currentCR = combatReadiness
        fighter.owner = owner

        var stats = fighter.mutableStats
        stats.damageToCapital.modifyMult("rat_shadow", 0.5f)
        stats.damageToCruisers.modifyMult("rat_shadow", 0.5f)
        stats.damageToDestroyers.modifyMult("rat_shadow", 0.5f)
        stats.damageToFrigates.modifyMult("rat_shadow", 0.5f)

        if (originalFighter.shipTarget != null) {
            fighter.shipTarget = originalFighter.shipTarget
        }

        fighter.shipAI = null
       /* fighter.shipAI = Global.getSettings().createDefaultShipAI(fighter, ShipAIConfig())
        fighter.shipAI.forceCircumstanceEvaluation()*/

        fighter.alphaMult = 0f
        return fighter
    }

}