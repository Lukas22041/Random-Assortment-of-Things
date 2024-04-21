package assortment_of_things.exotech.shipsystems

import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.misc.GraphicLibEffects
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.CombatFleetManager
import com.fs.starfarer.combat.entities.Ship
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*


class TylosShipsystem : BaseShipSystemScript(), HullDamageAboutToBeTakenListener {

    var ship: ShipAPI? = null
    val color = Color(248,172,44, 255)

    var module: ShipAPI? = null
    var newVariant: ShipVariantAPI? = null

    var activated = false

    var SPEED_BONUS = 200f
    var TURN_BONUS = 15f

    var afterimageInterval = IntervalUtil(0.05f, 0.05f)
    var empInterval = IntervalUtil(0.1f, 0.2f)

    var killedOther = false

    var moduleDespawnInterval = IntervalUtil(0.1f, 0.1f)

    override fun apply(stats: MutableShipStatsAPI, id: String?, state: ShipSystemStatsScript.State, effectLevel: Float) {
        ship = stats.entity as ShipAPI? ?: return
        var system = ship!!.system
        var player = ship == Global.getCombatEngine().playerShip

        if (!ship!!.hasListenerOfClass(this::class.java)) {
            ship!!.addListener(this)
        }

        for (module in ship!!.childModulesCopy) {
            module.alphaMult = 0f
            module.collisionClass = CollisionClass.NONE

            module.shipAI = null
            //module.location.set(ship!!.location)
            module.location.set(Vector2f(100000f + ship!!.location.x, 100000f + ship!!.location.y))
            module.extraAlphaMult = 0f
            module.extraAlphaMult2 = 0f
            module.spriteAPI.color = Color(0, 0, 0,0)
            module.mutableStats.hullDamageTakenMult.modifyMult("rat_module_to_be_despawned", 0f)
            module.mutableStats.armorDamageTakenMult.modifyMult("rat_module_to_be_despawned", 0f)
            module.addTag("rat_module_to_be_despawned")


            //module.isPhased = true
            module.isHoldFireOneFrame = true

            for (weapon in module.allWeapons) {
                weapon.sprite?.color = Color(0, 0, 0, 0)
                weapon.barrelSpriteAPI?.color = Color(0, 0, 0, 0)
                weapon.glowSpriteAPI?.color = Color(0, 0, 0, 0)
                weapon.underSpriteAPI?.color = Color(0, 0, 0, 0)

                weapon.setRemainingCooldownTo(999f)
            }

            for (engine in module.engineController.shipEngines) {
                engine.engineSlot.color = Color(0, 0, 0, 0)
                engine.engineSlot.contrailColor = Color(0, 0, 0, 0)
                engine.engineSlot.glowAlternateColor = Color(0, 0, 0, 0)
            }

            if (!Global.getCombatEngine().combatUI.isShowingCommandUI) {
                moduleDespawnInterval.advance(Global.getCombatEngine().elapsedInLastFrame)
            }


            if (module.hasTag("copied_variant")) continue
            if (!moduleDespawnInterval.intervalElapsed()) continue

            module.addTag("copied_variant")

            module.addListener(object: AdvanceableListener {
                override fun advance(amount: Float) {
                    for (weapon in module.allWeapons) {
                        weapon.setRemainingCooldownTo(999f)
                        //module.location.set(ship!!.location)
                    }
                }
            })

            var variant = module.variant.clone()
            variant.addTag("tylos_no_refit_sprite")
            variant.addTag(Tags.UNRECOVERABLE)
            variant.addTag(Tags.VARIANT_UNBOARDABLE)

            //Global.getCombatEngine().removeEntity(module)
            //var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)
           /* var obfManager = manager as CombatFleetManager
            obfManager.removeDeployed(module as Ship, true)*/
           // Global.getCombatEngine().removeEntity(module)
          /*  module.isPhased = true
            module.isHoldFire = true
            module.alphaMult = 0f
            module.mutableStats.hullDamageTakenMult.modifyMult("rat_tylos_mod", 0f)*/

            newVariant = variant

            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true
            var newModule = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, ship!!.currentCR, ship!!.location, ship!!.facing)
            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false
            this.module = newModule
            newModule!!.setCustomData("rat_tylos_parent", ship)
            newModule.captain = ship!!.captain

            ship!!.setCustomData("rat_tylos_child", newModule)

            Global.getCombatEngine().removeEntity(newModule)
            //obfManager.removeDeployed(newModule as Ship, true)
        }

        if (ship!!.hasTag("rat_module_to_be_despawned")) return

        var parent = ship!!.customData.get("rat_tylos_parent") as ShipAPI?

        //Kill parent if its copy dies
        //Went for another solution//
        /*if (ship!!.hitpoints <= 0 && parent != null) {
            if (!killedOther) {
                killedOther = true
                Global.getCombatEngine().addEntity(parent)
                parent.location.set(Vector2f(100000f, 100000f))
                parent.splitShip()
                var manager = Global.getCombatEngine().getFleetManager(parent!!.owner)
                var obfManager = manager as CombatFleetManager
                obfManager.removeDeployed(parent as Ship, true)
            }
        }*/

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

        if (ship!!.hitpoints <= 0f && module != null) {
            var manager = Global.getCombatEngine().getFleetManager(module!!.owner)
            manager.removeDeployed(module, true)
            module = null
        }

        if (ship!!.hasTag("rat_tylos_deathtrigger") && !system.isActive && module != null) {

            ship!!.phaseCloak.forceState(ShipSystemAPI.SystemState.COOLDOWN, 1f)
            ship!!.isDefenseDisabled = true

            var manager = Global.getCombatEngine().getFleetManager(module!!.owner)
            manager.removeDeployed(module, true)
            module = null

          /*  Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true
            module!!.addTag("do_not_trigger")
            module!!.location.set(Vector2f(10000f, 10000f))
            Global.getCombatEngine().applyDamage(module, module!!.location, 100000f, DamageType.ENERGY, 1000f, true, false, true )
            Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false
*/
            Global.getCombatEngine().applyDamage(ship, ship!!.location, 100000f, DamageType.ENERGY, 1000f, true, false, true )

            //ship!!.splitShip()
            ship!!.tags.remove("rat_tylos_deathtrigger")
        }

        if (system.isActive) {

            var cloaked = ship!!.phaseCloak.state == ShipSystemAPI.SystemState.ACTIVE || ship!!.phaseCloak.state == ShipSystemAPI.SystemState.IN

            for (weapon in ship!!.allWeapons) {
                weapon.setForceNoFireOneFrame(true)
            }

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

        if (module == null && ship!!.baseOrModSpec().hullId != "rat_tylos_double" && (ship!!.system.isActive || ship!!.shipTarget != null)) {
           // createInitialCopy()
        }

        if (!activated && system.state == ShipSystemAPI.SystemState.ACTIVE) {
            activated = true



            var viewport = Global.getCombatEngine().viewport
            var oldX = viewport.llx
            var oldY = viewport.lly


            for (wing in ship!!.allWings) {
                for (fighter in wing.wingMembers) {
                    fighter.splitShip()
                    fighter.hitpoints = 0f
                }
            }

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

    /*fun createInitialCopy() {
        if (newVariant == null) return
        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true
        var newModule = spawnShipOrWingDirectly(newVariant!!, FleetMemberType.SHIP, ship!!.owner, ship!!.currentCR, ship!!.location, ship!!.facing)
        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false
        this.module = newModule
        newModule!!.setCustomData("rat_tylos_parent", ship)
        newModule.captain = ship!!.captain

        ship!!.setCustomData("rat_tylos_child", newModule)

        Global.getCombatEngine().removeEntity(newModule)
    }*/


    fun syncStats(current: ShipAPI, other: ShipAPI) {
        other.facing = current.facing
        other.velocity.set(current.velocity)
        other.location.set(current.location)
       // other.hitpoints = current.hitpoints

        if (!ship!!.hasTag("rat_tylos_deathtrigger")) {
            var hpPercent = current.hitpoints / current.maxHitpoints
            other.hitpoints = other.maxHitpoints * hpPercent
        }

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

        //Copy over assignments from previous ship & remove it from the old one
        var fleetMananger = engine.getFleetManager(ship!!.owner)
        var taskManager = fleetMananger.getTaskManager(false)
        var assignment = taskManager.getAssignmentFor(ship)
        if (assignment != null) {
           // var new = taskManager.createAssignment(assignment.type, assignment.target, false)
            taskManager.giveAssignment(fleetMananger.getDeployedFleetMember(module), assignment, false)
            ReflectionUtils.invoke("cancelDirectOrdersForMember", taskManager, fleetMananger.getDeployedFleetMember(ship))
        }


        if (ship == Global.getCombatEngine().playerShip) {
            engine.setPlayerShipExternal(module)
        }

        var cloaked = ship!!.phaseCloak.state == ShipSystemAPI.SystemState.ACTIVE || ship!!.phaseCloak.state == ShipSystemAPI.SystemState.IN
        if (cloaked) {
            setPhase(module!!, ship!!)
        }

        for (weapon in module!!.allWeapons) {
            weapon.setForceNoFireOneFrame(true)
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

        //Copy over assignments from previous ship & remove it from the old one
        var fleetMananger = engine.getFleetManager(ship!!.owner)
        var taskManager = fleetMananger.getTaskManager(false)
        var assignment = taskManager.getAssignmentFor(ship)
        if (assignment != null) {
            //var new = taskManager.createAssignment(assignment.type, assignment.target, false)
            taskManager.giveAssignment(fleetMananger.getDeployedFleetMember(parent), assignment, false)
            ReflectionUtils.invoke("cancelDirectOrdersForMember", taskManager, fleetMananger.getDeployedFleetMember(ship))
        }


        if (ship == Global.getCombatEngine().playerShip) {
            engine.setPlayerShipExternal(parent)
        }

        var cloaked = ship!!.phaseCloak.state == ShipSystemAPI.SystemState.ACTIVE || ship!!.phaseCloak.state == ShipSystemAPI.SystemState.IN
        if (cloaked) {
            setPhase(parent, ship!!)
        }

        for (weapon in parent.allWeapons) {
            weapon.setForceNoFireOneFrame(true)
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

    fun spawnShipOrWingDirectly(variant: ShipVariantAPI, type: FleetMemberType, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI? {
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


    override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?,  point: Vector2f?, damageAmount: Float): Boolean {

        if (ship!!.hasTag("rat_tylos_deathtrigger") && ship.system.isActive) {
            return true
        }


        var parent = ship!!.customData.get("rat_tylos_parent") as ShipAPI? ?: return false

        if (ship.hitpoints - damageAmount <= 0 && !ship.hasTag("do_not_trigger")) {
            parent.addTag("rat_tylos_deathtrigger")
            ship.phaseCloak.forceState(ShipSystemAPI.SystemState.COOLDOWN, 1f)
            ship.isDefenseDisabled = true

            if (ship.system.state != ShipSystemAPI.SystemState.IN) {
                ship.system.forceState(ShipSystemAPI.SystemState.IN, 0f)
            }
            return true
        }

        return false
    }

}

