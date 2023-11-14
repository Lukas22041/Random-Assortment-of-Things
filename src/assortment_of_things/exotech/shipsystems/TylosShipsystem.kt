package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.misc.GraphicLibEffects
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.TemporalShellStats
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.input.Mouse
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color


class TylosShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null
    val color = Color(248,172,44, 255)

    var module: ShipAPI? = null
    var activated = false

    var SPEED_BONUS = 300f
    var TURN_BONUS = 10f

    var afterimageInterval = IntervalUtil(0.05f, 0.05f)

    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {
        ship = stats.entity as ShipAPI? ?: return
        var system = ship!!.system
        var player = ship == Global.getCombatEngine().playerShip

        for (module in ship!!.childModulesCopy) {
            if (!module.isAlive) continue
            var variant = module.variant
            Global.getCombatEngine().removeEntity(module)

            var newModule = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, 1f, ship!!.location, ship!!.facing)
            this.module = newModule
            newModule!!.setCustomData("rat_tylos_parent", ship)
            newModule.captain = ship!!.captain
            ship!!.setCustomData("rat_tylos_child", newModule)
            Global.getCombatEngine().removeEntity(newModule)
        }

        var parent = ship!!.customData.get("rat_tylos_parent") as ShipAPI?


        if (activated && (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE)) {
            activated = false
        }

        val shipTimeMult = 1f + (1f * effectLevel)
        stats.timeMult.modifyMult(id, shipTimeMult)
        if (player) {
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
        } else {
            Global.getCombatEngine().timeMult.unmodify(id)
        }

        stats.maxSpeed.modifyFlat(id, SPEED_BONUS * effectLevel)
        stats.acceleration.modifyPercent(id, SPEED_BONUS * 3f * effectLevel)
        stats.deceleration.modifyPercent(id, SPEED_BONUS * 3f * effectLevel)
        stats.turnAcceleration.modifyFlat(id, TURN_BONUS * effectLevel)
        stats.turnAcceleration.modifyPercent(id, TURN_BONUS * 5f * effectLevel)
        stats.maxTurnRate.modifyFlat(id, 15f * effectLevel)
        stats.maxTurnRate.modifyPercent(id, 100f * effectLevel)

        ship!!.engineController.fadeToOtherColor(this, color, Color(0, 0, 0, 0), effectLevel, 0.67f)
        ship!!.engineController.extendFlame(this, 1f * effectLevel, 1f * effectLevel, 0f * effectLevel)

        if (system.isActive) {

            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                var alpha = (75 * effectLevel).toInt()
                AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(alpha), color.setAlpha(alpha), 1.5f * effectLevel, 2f, Vector2f().plus(ship!!.location))
            }

            if (player) {
                PostProcessShader.setNoise(false, 0.8f * effectLevel)
                PostProcessShader.setSaturation(false, 1f + (0.2f * effectLevel))

                Global.getSoundPlayer().applyLowPassFilter(1f, 1 - (0.3f * effectLevel))
            }


            ship!!.setJitterUnder(this, color, 1f * effectLevel, 15, 2f, 14f)



            ship!!.alphaMult = 0.2f + ( 0.8f - effectLevel * 0.8f)
            ship!!.isPhased = true
        }
        else {
            ship!!.alphaMult = 1f
            ship!!.isPhased = false
        }

        if (parent != null) {
            syncStats(ship!!, parent)
        }

        if (module != null) {
            syncStats(ship!!, module!!)
        }

        if (!activated && system.state == ShipSystemAPI.SystemState.ACTIVE) {
            activated = true


            if (parent != null) {
                doModuleToParent(effectLevel, parent)
            }

            if (module != null) {
                doParentToModule(effectLevel)
            }

            GraphicLibEffects.CustomRippleDistortion(Vector2f(ship!!.location), ship!!.velocity, 3000f, 10f, true, ship!!.facing, 360f, 1f
                ,1f, 1f, 1f, 1f, 1f)

            GraphicLibEffects.CustomBubbleDistortion(Vector2f(ship!!.location), ship!!.velocity, 1000f + ship!!.collisionRadius, 25f, true, ship!!.facing, 360f, 1f
                ,0.1f, 0.1f, 1f, 0.3f, 1f)


            if (player) {
                Mouse.setCursorPosition(Global.getSettings().screenWidthPixels.toInt() / 2,
                    Global.getSettings().screenHeightPixels.toInt() / 2)
            }
        }
    }



    fun syncStats(current: ShipAPI, other: ShipAPI) {
        other.facing = current.facing
        other.velocity.set(current.velocity)
        other.location.set(current.location)
       // other.hitpoints = current.hitpoints

        var hpPercent = current.hitpoints / current.maxHitpoints
        other.hitpoints = other.maxHitpoints * hpPercent

        var hardFluxPercent = current.hardFluxLevel
        var softFluxPercent = current.fluxLevel

        other.fluxTracker.hardFlux = other.fluxTracker.maxFlux * hardFluxPercent
        other.fluxTracker.currFlux = other.fluxTracker.maxFlux * softFluxPercent

      /*  other.fluxTracker.hardFlux = current.fluxTracker.hardFlux
        other.fluxTracker.currFlux = current.fluxTracker.currFlux*/

        if (current.shield != null && other.shield != null) {
            if (current.shield.isOn) {
                other.shield.toggleOn()
            }

            if (current.shield.isOff) {
                other.shield.toggleOff()
            }

            other.shield.activeArc = MathUtils.clamp(current.shield.activeArc, 0f, other.shield.arc)
            other.shield.forceFacing(current.shield.facing)
        }

    }



    fun doParentToModule(effectLevel: Float) {
        var engine = Global.getCombatEngine()

        //module!!.system.cooldownRemaining = 5f
        module!!.system.forceState(ShipSystemAPI.SystemState.OUT, 0f)

        engine.addEntity(module)
        if (ship == Global.getCombatEngine().playerShip) {
            engine.setPlayerShipExternal(module)
        }
        engine.removeEntity(ship)
    }

    fun doModuleToParent(effectLevel: Float, parent: ShipAPI) {
        var engine = Global.getCombatEngine()

        //parent.system.cooldownRemaining = 5f
        parent.system.forceState(ShipSystemAPI.SystemState.OUT, 0f)

        engine.addEntity(parent)
        if (ship == Global.getCombatEngine().playerShip) {
            engine.setPlayerShipExternal(parent)
        }
        engine.removeEntity(ship)
    }


    override fun unapply(stats: MutableShipStatsAPI, id: String?) {

    }

    fun spawnShipOrWingDirectly(variant: ShipVariantAPI?, type: FleetMemberType?, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI? {
        val member = Global.getFactory().createFleetMember(type, variant)
        member.owner = owner
        member.crewComposition.addCrew(member.neededCrew)

        val ship = Global.getCombatEngine().getFleetManager(owner).spawnFleetMember(member, location, facing, 0f)
        ship.crAtDeployment = combatReadiness
        ship.currentCR = combatReadiness
        ship.owner = owner
        ship.shipAI.forceCircumstanceEvaluation()
        return ship
    }

}

