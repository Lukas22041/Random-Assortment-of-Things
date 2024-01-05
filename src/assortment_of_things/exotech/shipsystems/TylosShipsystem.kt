package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.misc.GraphicLibEffects
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.TemporalShellStats
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.CombatFleetManager
import com.fs.starfarer.combat.entities.Ship
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.DefenseUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.input.Mouse
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*


class TylosShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null
    val color = Color(248,172,44, 255)

    var module: ShipAPI? = null
    var activated = false

    var SPEED_BONUS = 200f
    var TURN_BONUS = 15f

    var afterimageInterval = IntervalUtil(0.05f, 0.05f)
    var empInterval = IntervalUtil(0.1f, 0.2f)

    var killedParent = false

    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {
        ship = stats.entity as ShipAPI? ?: return
        var system = ship!!.system
        var player = ship == Global.getCombatEngine().playerShip

        for (module in ship!!.childModulesCopy) {
            if (!module.isAlive) continue
            var variant = module.variant.clone()
            variant.addTag("tylos_no_refit_sprite")
            variant.addTag(Tags.UNRECOVERABLE)
            variant.addTag(Tags.VARIANT_UNBOARDABLE)

            //Global.getCombatEngine().removeEntity(module)
            var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)
            var obfManager = manager as CombatFleetManager
            obfManager.removeDeployed(module as Ship, true)

            var newModule = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, 1f, ship!!.location, ship!!.facing)
            this.module = newModule
            newModule!!.setCustomData("rat_tylos_parent", ship)
            newModule.captain = ship!!.captain

            ship!!.setCustomData("rat_tylos_child", newModule)

            //Global.getCombatEngine().removeEntity(newModule)
            obfManager.removeDeployed(newModule as Ship, true)
        }



        var parent = ship!!.customData.get("rat_tylos_parent") as ShipAPI?
        //Kill parent if its copy dies
        if (ship!!.hitpoints <= 0 && parent != null) {
            if (!killedParent) {
                Global.getCombatEngine().addEntity(parent)
                parent.location.set(Vector2f(100000f, 100000f))
                parent.splitShip()
            }
        }

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

            var cloaked = ship!!.phaseCloak.state == ShipSystemAPI.SystemState.ACTIVE || ship!!.phaseCloak.state == ShipSystemAPI.SystemState.IN

            afterimageInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (afterimageInterval.intervalElapsed() && !Global.getCombatEngine().isPaused)
            {
                var alpha = 75
                if (cloaked) alpha = 75 - ((50 * ship!!.phaseCloak.effectLevel).toInt())

                //AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(alpha), color.setAlpha(alpha), 1.5f * effectLevel, 2f, Vector2f().plus(ship!!.location))
                AfterImageRenderer.addAfterimage(ship!!, color.setAlpha(alpha), Color(130,4,189, 0), 0.5f, 2f, Vector2f().plus(ship!!.location))
            }

            empInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            if (empInterval.intervalElapsed() && !Global.getCombatEngine().isPaused) {
                ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                var from = Vector2f(ship!!.exactBounds.segments.random().p1)

                var angle = Misc.getAngleInDegrees(ship!!.location, from)
                //var to = MathUtils.getPointOnCircumference(ship!!.location, MathUtils.getRandomNumberInRange(20f, 50f) + ship!!.collisionRadius, angle + MathUtils.getRandomNumberInRange(-30f, 30f))

                var to = Vector2f(ship!!.exactBounds.segments.random().p1)

                var empColor = Misc.interpolateColor(color, Color(130,4,189, 255), Random().nextFloat())
                Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, empColor.setAlpha(75), empColor.setAlpha(75))
            }

            if (player) {
                PostProcessShader.setNoise(false, 0.8f * effectLevel)
                PostProcessShader.setSaturation(false, 1f + (0.2f * effectLevel))

                Global.getSoundPlayer().applyLowPassFilter(1f, 1 - (0.3f * effectLevel))
            }


            var intensity = 1f - (0.2f * ship!!.phaseCloak.effectLevel)
            ship!!.setJitterUnder(this, color, intensity * effectLevel, 20, 2f, 25f)



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

            var viewport = Global.getCombatEngine().viewport
            var oldX = viewport.llx
            var oldY = viewport.lly


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
                /*Mouse.setCursorPosition(Global.getSettings().screenWidthPixels.toInt() / 2,
                    Global.getSettings().screenHeightPixels.toInt() / 2)*/

               /* viewport.isExternalControl = true
                viewport.set(oldX, oldY, viewport.visibleWidth, viewport.visibleHeight)*/

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

        module!!.isHoldFireOneFrame = true
        engine.addEntity(module)
        if (ship == Global.getCombatEngine().playerShip) {
            engine.setPlayerShipExternal(module)
        }

        var cloaked = ship!!.phaseCloak.state == ShipSystemAPI.SystemState.ACTIVE || ship!!.phaseCloak.state == ShipSystemAPI.SystemState.IN
        if (cloaked) {
            setPhase(module!!, ship!!)
        }

        engine.removeEntity(ship)
        /*var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)
        var obfManager = manager as CombatFleetManager
        obfManager.removeDeployed(ship as Ship, true)*/
    }

    fun doModuleToParent(effectLevel: Float, parent: ShipAPI) {
        var engine = Global.getCombatEngine()

        //parent.system.cooldownRemaining = 5f
        parent.system.forceState(ShipSystemAPI.SystemState.OUT, 0f)

        parent.isHoldFireOneFrame = true
        engine.addEntity(parent)
        if (ship == Global.getCombatEngine().playerShip) {
            engine.setPlayerShipExternal(parent)
        }

        var cloaked = ship!!.phaseCloak.state == ShipSystemAPI.SystemState.ACTIVE || ship!!.phaseCloak.state == ShipSystemAPI.SystemState.IN
        if (cloaked) {
            setPhase(parent, ship!!)
        }

        engine.removeEntity(ship)
       /* var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)
        var obfManager = manager as CombatFleetManager
        obfManager.removeDeployed(ship as Ship, true)*/
    }

    fun setPhase(target: ShipAPI, current: ShipAPI) {
        var cloak = target.phaseCloak
        target.isPhased = true
        cloak.forceState(ShipSystemAPI.SystemState.ACTIVE, 1f)
        current.phaseCloak.forceState(ShipSystemAPI.SystemState.IDLE, 0f)
        current.alphaMult = 0f
        target.alphaMult = 0f
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

