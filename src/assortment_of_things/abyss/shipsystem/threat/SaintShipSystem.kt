package assortment_of_things.abyss.shipsystem.threat

import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.MineStrikeStats
import com.fs.starfarer.api.impl.combat.threat.*
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class SaintShipSystem : BaseShipSystemScript() {

    var afterimageInterval = IntervalUtil(0.15f, 0.15f)

    var addedRenderer = false

    var activated = false

    //var constructionSwarms = ArrayList<RoilingSwarmEffect>()

    override fun apply(stats: MutableShipStatsAPI?, id: String,  state: ShipSystemStatsScript.State?,  effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        var ship = stats!!.entity
        if (ship !is ShipAPI) return
        var player = ship == Global.getCombatEngine().getPlayerShip();
        var id = id + "_" + ship!!.id

        var system = ship.system

        //Causes swarm launchers to not fire below 100 fragments
        var swarm = RoilingSwarmEffect.getSwarmFor(ship) ?: return
        if (swarm.members.count() <= 149) {
            ship.addTag(ThreatShipConstructionScript.SHIP_UNDER_CONSTRUCTION)
        } else {
            ship.removeTag(ThreatShipConstructionScript.SHIP_UNDER_CONSTRUCTION)
        }


        //Stop Firing Fragment weapons if below 100 fragments, only allow fire from manual selections
        var count = 130
        if (ship.system.state == ShipSystemAPI.SystemState.ACTIVE) count = 50 //Allow it to use more while the system is active
        if (/*ship.shipAI != null || Global.getCombatEngine()?.combatUI?.isAutopilotOn == true && */swarm.members.count() <= count) {

            var selected = ReflectionUtils.get("selected", ship)

            for (group in ship.weaponGroupsCopy) {
                if (group != selected) {
                    for (weapon in group.weaponsCopy) {
                        if (weapon.effectPlugin is FragmentWeapon && weapon.spec.weaponId != "voltaic_discharge") {
                            weapon.isForceNoFireOneFrame = true
                        }
                    }
                }
            }

            /*for (weapon in ship.allWeapons) {
                if (weapon.effectPlugin is BaseFragmentMissileEffect)    {
                    if (weapon.)
                    weapon.isForceNoFireOneFrame = true
                }
            }*/
        }


        if (!addedRenderer) {
            addedRenderer = true
            Global.getCombatEngine().addLayeredRenderingPlugin(SaintGlowRenderer(ship))
        }

        var color = Color(196, 20, 35, 255)

        if (!system.isActive && activated) {
            activated = false

        }

        if (system.isActive && !activated) {
            activated = true

            //var locLeft = MathUtils.getPointOnCircumference(ship.location, ship.facing-90f-70f, ship.collisionRadius + 600f)
            //var locRight = MathUtils.getPointOnCircumference(ship.location, ship.facing-90f+70f, ship.collisionRadius + 600f)

            var angleLeft = ship.facing + 90f + 30f
            var angleRight = ship.facing - 90f - 30f

            //Always pick one of the two, just randomise which side they spawn on
            var variants = ArrayList<String>()
            variants.add("rat_prayer_Type3000")
            variants.add("rat_prayer_Type3100")

            spawnSwarm(ship, variants.randomAndRemove(), angleLeft)
            spawnSwarm(ship, variants.randomAndRemove(), angleRight)

            ship.currentCR -= 0.075f
            ship.currentCR = MathUtils.clamp(ship.currentCR, 0f, 1f)
        }
    }

    fun spawnSwarm(ship: ShipAPI, variantId: String, angle: Float) {

        //Spawn Wing
        val wingId = "saint_construction_swarm_wing"

        val engine = Global.getCombatEngine()
        val manager: CombatFleetManagerAPI = engine.getFleetManager(ship.getOwner())
        manager.isSuppressDeploymentMessages = true

        var facing = angle
        var startLoc = MathUtils.getPointOnCircumference(ship.location, ship.collisionRadius, facing)

        val fighter = manager.spawnShipOrWing(wingId, startLoc, facing, 0f, null)
        //fighter.wing.sourceShip = ship

        manager.isSuppressDeploymentMessages = false

        fighter.mutableStats.maxSpeed.modifyMult("construction_swarm", 0.75f)

        //Configure Wing and add velocity
        val takeoffVel = Misc.getUnitVectorAtDegreeAngle(facing)
        takeoffVel.scale(fighter.maxSpeed*50)
        //var takeoffVel = MathUtils.getPointOnCircumference(Vector2f(), ship.maxSpeed*10, facing)
        //fighter.velocity.set(takeoffVel)


        fighter.isDoNotRender = true
        fighter.explosionScale = 0f
        fighter.hulkChanceOverride = 0f
        fighter.impactVolumeMult = SwarmLauncherEffect.IMPACT_VOLUME_MULT
        fighter.armorGrid.clearComponentMap() // no damage to weapons/engines
        fighter.captain.setPersonality(Personalities.STEADY)
       /* fighter.collisionRadius = 500f
        fighter.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP, 5f, ship)
        fighter.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_RANGE_MODIFIER, 5f, 1500f)*/


        //fighter.shipAI = create

        Vector2f.add(fighter.velocity, takeoffVel, fighter.velocity)


        val sourceSwarm = RoilingSwarmEffect.getSwarmFor(ship) ?: return

        val swarm = FragmentSwarmHullmod.createSwarmFor(fighter)
      /*  swarm.params.flashFringeColor = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR
        RoilingSwarmEffect.getFlockingMap().remove(swarm.params.flockingClass, swarm)
        swarm.params.flockingClass = FragmentSwarmHullmod.CONSTRUCTION_SWARM_FLOCKING_CLASS
        RoilingSwarmEffect.getFlockingMap().add(swarm.params.flockingClass, swarm)*/

        sourceSwarm.transferMembersTo(swarm, 50)

        Global.getCombatEngine().addPlugin(SaintConstructionScript(variantId, fighter, ship, swarm, this))

        //constructionSwarms.add(sourceSwarm)
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        var swarm = RoilingSwarmEffect.getSwarmFor(ship) ?: return false
        if (swarm.numActiveMembers < 100 && !ship!!.system.isActive) return false
        return system!!.state != ShipSystemAPI.SystemState.IN
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String? {
        var swarm = RoilingSwarmEffect.getSwarmFor(ship)
        if (swarm != null) {
            if (swarm.numActiveMembers < 100 && !ship!!.system.isActive) {
                return "Not enough fragments"
            }
        }
        return null
    }



    class SaintConstructionScript(var variantId: String, var fighter: ShipAPI, var source: ShipAPI, var swarm: RoilingSwarmEffect, var plugin: SaintShipSystem) : BaseEveryFrameCombatPlugin() {

        var particleInterval = IntervalUtil(0.05f, 0.05f)

        var spawned = false

        var fade = 0f

        var newShip: ShipAPI? = null

        var enableSuicideBurn = false
        //var suicideBurnDur = 0f

        override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

            if (Global.getCombatEngine().isPaused) return

            //if the shipsystem gets canceled before it becomes active
            if (!source.system.isActive) {
                if (fighter.isAlive) {
                    Global.getCombatEngine().removeEntity(swarm.entity)
                    Global.getCombatEngine().removeEntity(fighter)
                    Global.getCombatEngine().removePlugin(this)
                    return
                }
            }

            //Remove script
            if (newShip != null && !newShip!!.isAlive) {
                Global.getCombatEngine().removePlugin(this)
                return
            }

            if (newShip != null) {
                fade += 2 * amount
                fade = MathUtils.clamp(fade, 0f, 1f)
                newShip!!.alphaMult = fade

                if (fade <= 0.6f) {
                    newShip!!.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
                    newShip!!.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)
                   /* newShip!!.blockCommandForOneFrame(ShipCommand.TURN_LEFT)
                    newShip!!.blockCommandForOneFrame(ShipCommand.TURN_RIGHT)*/
                    newShip!!.blockCommandForOneFrame(ShipCommand.ACCELERATE)
                    newShip!!.blockCommandForOneFrame(ShipCommand.DECELERATE)
                    newShip!!.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
                }

                //Set to same target as ship, if there isnt one, set to escort instead.
                var target = source.shipTarget
                if (target != null) {
                    newShip!!.shipTarget = target
                } else if (!enableSuicideBurn) {
                    newShip!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP, 1f, source)
                }

                //Enable suicide burn if inactive.
                if (source.system.state == ShipSystemAPI.SystemState.OUT || !source.system.isActive) {
                    enableSuicideBurn = true
                    newShip!!.addTag("rat_do_not_use_symbiosis")
                    //newShip!!.addTag("rat_do_not_disable_system")
                }


                if (enableSuicideBurn) {

                    if (!newShip!!.system.isActive) {
                        //newShip!!.system.cooldownRemaining = -1f
                        newShip!!.useSystem()
                    }

                    if (newShip!!.system.state == ShipSystemAPI.SystemState.ACTIVE) {
                        newShip!!.system.forceState(ShipSystemAPI.SystemState.ACTIVE, 0f)
                    }

                    if (newShip!!.system.state == ShipSystemAPI.SystemState.OUT) {
                        newShip!!.system.forceState(ShipSystemAPI.SystemState.IN, newShip!!.system.effectLevel)
                    }

                    /*if (newShip!!.system.state == ShipSystemAPI.SystemState.OUT) {
                        var lv = newShip!!.system.effectLevel + 1f * amount
                        lv = MathUtils.clamp(lv, 0f, 1f)
                        newShip!!.system.forceState(ShipSystemAPI.SystemState.OUT, lv)

                    }*/

                   /* if (newShip!!.system.state == ShipSystemAPI.SystemState.OUT) {

                    }*/

                    var nearTarget = false

                    if (newShip!!.shipTarget == null) {
                        var newTarget: ShipAPI? = null
                        var shortestDistance = Float.MAX_VALUE
                        var iter = Global.getCombatEngine().shipGrid.getCheckIterator(newShip!!.location, 2000f, 2000f)
                        for (target in iter) {
                            if (target !is ShipAPI) continue
                            if (target.owner == newShip!!.owner) continue

                            var dist = MathUtils.getDistance(newShip!!, target)
                            if (dist <= shortestDistance) {
                                shortestDistance = dist
                                newTarget = target
                            }

                            //Set (maneuver) target if the ship
                        }
                        newShip!!.shipTarget = newTarget
                    }



                    newShip!!.aiFlags.setFlag(AIFlags.DO_NOT_BACK_OFF, 5f)
                    newShip!!.aiFlags.setFlag(AIFlags.DO_NOT_VENT, 5f)
                    newShip!!.aiFlags.setFlag(AIFlags.DO_NOT_USE_SHIELDS, 5f) //Prevent Overloading
                    newShip!!.aiFlags.setFlag(AIFlags.DO_NOT_USE_SHIELDS, 5f) //Prevent Overloading
                    newShip!!.aiFlags.setFlag(AIFlags.IGNORES_ORDERS, 5f) //Prevent Overloading

                    newShip!!.aiFlags.removeFlag(AIFlags.ESCORT_OTHER_SHIP)

                    if (newShip!!.shipTarget != null) {
                        var angle = Misc.getAngleInDegrees(newShip!!.location, newShip!!.shipTarget!!.location)
                        newShip!!.aiFlags.setFlag(AIFlags.FACING_OVERRIDE_FOR_MOVE_AND_ESCORT_MANEUVERS, 5f, angle)

                        var dist = MathUtils.getDistance(newShip!!, newShip!!.shipTarget)
                        if (dist <= 15f) {
                            nearTarget = true
                        }
                    }

                    /*suicideBurnDur += 1f * amount
                    if (suicideBurnDur >= 10f) {
                        nearTarget = true
                    }*/

                    if (nearTarget || source.system.isCoolingDown) {
                        //newShip.explosionScale
                        //newShip!!.hitpoints = 0f

                        Global.getCombatEngine().applyDamage(newShip!!, newShip!!.location, 100000f, DamageType.ENERGY, 0f, true, false, null)

                        newShip!!.mutableStats.dynamic.getStat(Stats.EXPLOSION_RADIUS_MULT).modifyFlat("rat_explosion_radius_increase", 1.5f)
                        newShip!!.mutableStats.dynamic.getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyFlat("rat_explosion_radius_increase", 0.8f)
                        Global.getCombatEngine().removePlugin(this)
                        return
                    }
                }
            }


            if (!spawned || fade <= 0.20f) {
                //Delayed Start
                var effectLevel = source.system.effectLevel.levelBetween(0.15f, 0.4f)

                var color = Color(130,155,145, 155 + (100 * effectLevel).toInt())
                particleInterval.advance(amount*effectLevel) //Only advance past threshold
                if (particleInterval.intervalElapsed()) {
                    var vel = Vector2f(fighter.velocity.x * 0.5f, fighter.velocity.y * 0.5f)
                    //var loc = MathUtils.getRandomPointInCircle(swarm.entity.location, 100f * effectLevel)
                    var loc = swarm.entity.location
                    loc = loc.plus(MathUtils.getRandomPointInCircle(Vector2f(), 75f * effectLevel))
                    if (newShip != null) loc = newShip!!.location

                    for (i in 0 until 5) {

                        Global.getCombatEngine().addNebulaParticle(loc, vel, MathUtils.getRandomNumberInRange(140f * effectLevel, 160 * effectLevel),
                            1f, 0.3f * effectLevel, 0f, MathUtils.getRandomNumberInRange(0.5f + (1.5f * effectLevel), 1.5f + (3f * effectLevel))
                            , color)
                    }
                }
            }



            if (!spawned && source.system.state == ShipSystemAPI.SystemState.ACTIVE) {
                spawned = true


                val engine = Global.getCombatEngine()
                val manager: CombatFleetManagerAPI = engine.getFleetManager(source.getOwner())
                manager.isSuppressDeploymentMessages = true

                var facing = fighter.facing
                var loc = fighter.location

                //newShip = manager.spawnShipOrWing("rat_prayer_Type3000", loc, facing, 0f, null)
                var variant = Global.getSettings().getVariant(variantId)
                newShip = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, source.owner, source.currentCR, loc, facing)
                newShip!!.velocity.set(fighter.velocity)
                newShip!!.alphaMult = 0f
                //fighter.wing.sourceShip = ship

                manager.isSuppressDeploymentMessages = false

                //plugin.spawnedShips.add(newShip!!)

                var newSwarm = RoilingSwarmEffect.getSwarmFor(newShip)
                //swarm.shouldDespawnAll()
                swarm.transferMembersTo(newSwarm, 50)
                Global.getCombatEngine().removeEntity(swarm.entity)
                Global.getCombatEngine().removeEntity(fighter)


                newShip!!.captain.setPersonality(Personalities.RECKLESS)

                //Assign the same AI core.
                newShip!!.captain = source.captain

                //HullSize size = ship.getHullSize();
                val config = ShipAIConfig()
                config.alwaysStrafeOffensively = true
                config.backingOffWhileNotVentingAllowed = false
                config.turnToFaceWithUndamagedArmor = false
                config.burnDriveIgnoreEnemies = true
                config.personalityOverride = Personalities.RECKLESS

                newShip!!.shipAI = Global.getSettings().createDefaultShipAI(newShip, config)
                newShip!!.shipTarget = source.shipTarget

                //newShip!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP, 999999f, source)
                //newShip!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.ESCORT_RANGE_MODIFIER, 999999f, 1000f)

                newShip!!.mutableStats.armorDamageTakenMult.modifyMult("rat_weaker_prayers", 1.3f)
                newShip!!.mutableStats.hullDamageTakenMult.modifyMult("rat_weaker_prayers", 1.3f)
                newShip!!.mutableStats.shieldDamageTakenMult.modifyMult("rat_weaker_prayers", 1.2f)


            }


        }

        fun spawnShipOrWingDirectly(variant: ShipVariantAPI?, type: FleetMemberType?, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI? {
            val member = Global.getFactory().createFleetMember(type, variant)
            member.owner = owner
            member.crewComposition.addCrew(member.neededCrew)
            member.stats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat("rat_saint_spawn", -12f)
            member.repairTracker.cr = combatReadiness

            val ship = Global.getCombatEngine().getFleetManager(owner).spawnFleetMember(member, location, facing, 0f)
            ship.crAtDeployment = combatReadiness
            ship.currentCR = combatReadiness
            ship.owner = owner
            ship.shipAI.forceCircumstanceEvaluation()

            return ship
        }
    }




    private fun findClearLocation(dest: Vector2f): Vector2f {
        if (isLocationClear(dest)) return dest

        val incr = 50f

        val tested = WeightedRandomPicker<Vector2f>()
        var distIndex = 1f
        while (distIndex <= 32f) {
            val start = Math.random().toFloat() * 360f
            var angle = start
            while (angle < start + 360) {
                val loc = Misc.getUnitVectorAtDegreeAngle(angle)
                loc.scale(incr * distIndex)
                Vector2f.add(dest, loc, loc)
                tested.add(loc)
                if (isLocationClear(loc)) {
                    return loc
                }
                angle += 60f
            }
            distIndex *= 2f
        }

        if (tested.isEmpty) return dest // shouldn't happen

        return tested.pick()
    }

    private fun isLocationClear(loc: Vector2f): Boolean {
        for (other in Global.getCombatEngine().ships) {
            if (other.isShuttlePod) continue
            if (other.isFighter) continue

            var otherLoc = other.shieldCenterEvenIfNoShield
            var otherR = other.shieldRadiusEvenIfNoShield
            if (other.isPiece) {
                otherLoc = other.location
                otherR = other.collisionRadius
            }

            val dist = Misc.getDistance(loc, otherLoc)
            val r = otherR
            var checkDist = MineStrikeStats.MIN_SPAWN_DIST
            if (other.isFrigate) checkDist = MineStrikeStats.MIN_SPAWN_DIST_FRIGATE
            if (dist < r + checkDist) {
                return false
            }
        }
        for (other in Global.getCombatEngine().asteroids) {
            val dist = Misc.getDistance(loc, other.location)
            if (dist < other.collisionRadius + MineStrikeStats.MIN_SPAWN_DIST) {
                return false
            }
        }

        return true
    }


    class SaintGlowRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

        var sprite = Global.getSettings().getAndLoadSprite("graphics/ships/rat_saint_glow.png")

        override fun getRenderRadius(): Float {
            return 100000f
        }

        var layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return layers
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

            if (!ship.isAlive) return

            sprite.angle = ship.facing -90f
            sprite.alphaMult = ship.system.effectLevel
            sprite.setNormalBlend()
            sprite.renderAtCenter(ship.location.x, ship.location.y)

            sprite.alphaMult = ship.system.effectLevel * 0.5f
            sprite.setAdditiveBlend()
            sprite.renderAtCenter(ship.location.x, ship.location.y)
        }
    }


}