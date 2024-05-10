package assortment_of_things.abyss.boss

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalStormParticleManager
import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.abyss.items.cores.officer.CosmosCore
import assortment_of_things.abyss.shipsystem.activators.PrimordialSeaActivator
import assortment_of_things.misc.GraphicLibEffects
import assortment_of_things.misc.StateBasedTimer
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.combat.MineStrikeStats
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.apache.log4j.Level
import org.dark.shaders.distortion.RippleDistortion
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import org.magiclib.subsystems.MagicSubsystem
import org.magiclib.subsystems.MagicSubsystemsManager
import java.awt.Color
import java.util.*
import kotlin.math.log
import kotlin.math.max

class GenesisBossScript(var ship: ShipAPI) : CombatLayeredRenderingPlugin, HullDamageAboutToBeTakenListener {

    var phase = Phases.P1
    var transitionTimer = StateBasedTimer(1.5f, 2f, 4f)
    var transitionDone = false

    var empInterval = IntervalUtil(2f, 2f)
    var activateZone = false

    var darken = Global.getSettings().getSprite("graphics/fx/rat_black.png")
    var vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette_reversed.png")
    var vignetteLevel = 0f

    var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    var particles = ArrayList<AbyssalStormParticleManager.AbyssalLightParticle>()

    var particleInterval = IntervalUtil(0.2f, 0.2f)
    var halo = Global.getSettings().getSprite("rat_terrain", "halo")

    var ripple: RippleDistortion? = null

    var startedMusic = false

    var systemGlow: SpriteAPI = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow.png")
    var fader = FaderUtil(1f, 2f, 1.5f, false, false)
    var lastJitterLocations = ArrayList<Vector2f>()
    var lastSecondJitterLocations = ArrayList<Vector2f>()

    var hasSeenBoss = false
    var healthBar = GenesisHealthBar(this, ship)

    var apparations = ArrayList<ShipAPI>()

    var logger = Global.getLogger(this::class.java)

    var azazel1: ShipAPI? = null
    var azazel2: ShipAPI? = null
    var azazel3: ShipAPI? = null
    var azazel4: ShipAPI? = null
    var phase3TransitionTimer = StateBasedTimer(2f, 1f, 0f)
    var phase3HealthLevel = 1f
    var maxPhas3HealthLevel = 1f

    enum class Phases {
        P1, P2, P3
    }


    init {
        logger.level = Level.ALL

        ship.maxHitpoints *= 1.75f
        ship.mutableStats.armorBonus.modifyMult("rat_genesis_hp_for_more_armor_dmg", 1.4f)
        ship.hitpoints = ship.maxHitpoints

        var stats = ship.mutableStats
        stats.weaponDamageTakenMult.modifyMult("rat_genesis_permanent_boss_buff", 0.25f)
        stats.engineDamageTakenMult.modifyMult("rat_genesis_permanent_boss_buff", 0.25f)
        stats.empDamageTakenMult.modifyMult("rat_genesis_permanent_boss_buff", 0.25f)

        stats.energyWeaponRangeBonus.modifyMult("rat_genesis_permanent_boss_buff", 1.10f)
        stats.ballisticWeaponRangeBonus.modifyMult("rat_genesis_permanent_boss_buff", 1.10f)

       /* stats.maxSpeed.modifyMult("rat_genesis_permanent_boss_buff", 1.1f)
        stats.systemCooldownBonus.modifyMult("rat_genesis_permanent_boss_buff", 0.9f)*/

    }



    override fun init(entity: CombatEntityAPI?) {
        ship.addListener(this)

        Global.getCombatEngine().addPlugin(healthBar)
    }

    override fun cleanup() {

    }

    override fun isExpired(): Boolean {
        return false
    }



    override fun advance(amount: Float) {

        var hard = ship.variant.hasTag("rat_challenge_mode")

        if (hard && phase == Phases.P3 && ship.hitpoints <= 0) {
            Global.getSector().memoryWithoutUpdate.set("\$defeated_singularity_on_hard", true, 1f)
        }

        handleParticles(amount)

        var soundplayer = Global.getSoundPlayer()

        var subsystems = MagicSubsystemsManager.getSubsystemsForShipCopy(ship)
        var primSea = subsystems!!.find { it is PrimordialSeaActivator }

        var isPrimSeaActive = false
        if (primSea != null) {
            isPrimSeaActive = primSea.state == MagicSubsystem.State.ACTIVE
        }

        var viewport = Global.getCombatEngine().viewport
        if ((viewport.isNearViewport(ship.location, ship.collisionRadius + 100f) || isPrimSeaActive) && phase == Phases.P1 && !startedMusic && Global.getCombatEngine().getTotalElapsedTime(false) >= 1f) {
            logger.debug("Starting Genesis Phase 1 Music. If the game freezes past this point, please make sure to increase the allocated ram, or change from Java23 to Java8 instead to fix the issue from happening again.")
            soundplayer.playCustomMusic(1, 1, "rat_abyss_genesis1", true)
            startedMusic = true

            hasSeenBoss = true
        }


        var skipToPhase3 = false
        if (hard && azazel1 != null) {
            var count = 0
            if (!azazel1!!.isAlive) count += 1
            if (!azazel2!!.isAlive) count += 1
            if (!azazel3!!.isAlive) count += 1
            if (!azazel4!!.isAlive) count += 1

            if (count >= 2) {
                skipToPhase3 = true
            }
        }


        if (((azazel1?.isAlive == false && azazel2?.isAlive == false) || skipToPhase3) && phase == Phases.P2) {
            phase = Phases.P3
            ship.hitpoints = ship.maxHitpoints
            ship.fluxTracker.currFlux = 0f
            ship.fluxTracker.hardFlux = 0f

            var armorGrid = ship.armorGrid
            for (x in armorGrid.grid.indices) {
                for (y in armorGrid.grid.indices) {
                    armorGrid.setArmorValue(x, y, armorGrid.maxArmorInCell)
                }
            }

            ship.syncWithArmorGridState()

            GraphicLibEffects.CustomRippleDistortion(Vector2f(ship.location), Vector2f(), 3000f, 10f, true, ship.facing, 360f, 1f
                ,1f, 1f, 1f, 1f, 1f)

            GraphicLibEffects.CustomBubbleDistortion(Vector2f(ship.location), Vector2f(), 1000f + ship.collisionRadius, 25f, true, ship.facing, 360f, 1f
                ,0.1f, 0.1f, 1f, 0.3f, 1f)
        }

        if (phase == Phases.P3) {


            phase3HealthLevel -= 0.1f * amount
            var healthLevel = (ship.hitpoints / ship.maxHitpoints)
            phase3HealthLevel = MathUtils.clamp(phase3HealthLevel, 0f, 1f)
            phase3HealthLevel = MathUtils.clamp(phase3HealthLevel, healthLevel, maxPhas3HealthLevel)
            maxPhas3HealthLevel = phase3HealthLevel

            var level = 1f
            if (phase3TransitionTimer.state == StateBasedTimer.TimerState.In) {
                phase3TransitionTimer.advance(amount)
                level = phase3TransitionTimer.level
            }


            Global.getCombatEngine().timeMult.modifyMult("genesis_phase_3_transition", 0.2f + (0.8f * level))

            var shield = ship.shield
            shield.arc = 270 + (90 * (1-level))

            ship.mutableStats.timeMult.modifyMult("genesis_phase3", 1 + (0.1f * level))
            ship.mutableStats.fluxDissipation.modifyMult("genesis_phase3", 1 + (0.10f * level))

            ship.mutableStats.shieldDamageTakenMult.modifyMult("genesis_phase_2", 0.75f * level)
            ship.mutableStats.hullDamageTakenMult.modifyMult("genesis_phase_2", 0.75f * level)
            ship.mutableStats.armorDamageTakenMult.modifyMult("genesis_phase_2", 0.75f * level)

            ship.mutableStats.maxSpeed.modifyMult("genesis_phase_2", 0.5f + (0.55f * level))
            ship.mutableStats.turnAcceleration.modifyMult("genesis_phase_2", 0.50f  + (0.55f * level))

            ship.mutableStats.energyRoFMult.modifyMult("genesis_phase_2", 0.75f + (0.35f * level))
            ship.mutableStats.ballisticRoFMult.modifyMult("genesis_phase_2", 0.75f + (0.35f * level))
            ship.mutableStats.missileRoFMult.modifyMult("genesis_phase_2", 0.75f + (0.35f * level))

            ship.mutableStats.energyWeaponRangeBonus.modifyMult("genesis_phase_2", 0.60f + (0.40f * level))
            ship.mutableStats.ballisticWeaponRangeBonus.modifyMult("genesis_phase_2", 0.60f + (0.40f * level))
            ship.mutableStats.missileWeaponRangeBonus.modifyMult("genesis_phase_2", 0.60f + (0.40f * level))

            var color = AbyssUtils.GENESIS_COLOR.setAlpha(75)
            var jitterColor = color.setAlpha(55)
            var jitterUnderColor = color.setAlpha(150)

            ship!!.setJitter(this, jitterColor, (1- transitionTimer.level), 3, 0f, 0f )
            ship!!.setJitterUnder(this, jitterUnderColor, (1- transitionTimer.level), 25, 0f, 10f)
        }

        var realAmount = amount / Global.getCombatEngine().timeMult.modifiedValue

        if (phase == Phases.P2 ) {



            //Stats
            var shield = ship.shield
            ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON)
            if (shield.isOff) {
                shield.toggleOn()
            }
            shield.arc = 360f
            shield.activeArc = 360f * (1- transitionTimer.level)

            ship.system.cooldownRemaining = 10f

            ship.mutableStats.shieldDamageTakenMult.modifyMult("genesis_phase_2", 0f)
            ship.mutableStats.hullDamageTakenMult.modifyMult("genesis_phase_2", 0f)
            ship.mutableStats.armorDamageTakenMult.modifyMult("genesis_phase_2", 0f)

            ship.mutableStats.maxSpeed.modifyMult("genesis_phase_2", 0.5f)
            ship.mutableStats.turnAcceleration.modifyMult("genesis_phase_2", 0.50f)

            ship.mutableStats.energyRoFMult.modifyMult("genesis_phase_2", 0.75f)
            ship.mutableStats.ballisticRoFMult.modifyMult("genesis_phase_2", 0.75f)
            ship.mutableStats.missileRoFMult.modifyMult("genesis_phase_2", 0.75f)

            ship.mutableStats.energyWeaponRangeBonus.modifyMult("genesis_phase_2", 0.60f)
            ship.mutableStats.ballisticWeaponRangeBonus.modifyMult("genesis_phase_2", 0.60f)
            ship.mutableStats.missileWeaponRangeBonus.modifyMult("genesis_phase_2", 0.60f)

            var color = AbyssUtils.GENESIS_COLOR.setAlpha(75)
            var jitterColor = color.setAlpha(55)
            var jitterUnderColor = color.setAlpha(150)

            ship!!.setJitter(this, jitterColor, (1- transitionTimer.level), 3, 0f, 0f )
            ship!!.setJitterUnder(this, jitterUnderColor, (1- transitionTimer.level), 25, 0f, 10f)


            if (!transitionDone && ship.isAlive) {

                ship.system.forceState(ShipSystemAPI.SystemState.COOLDOWN, 1f)
                ship.isHoldFireOneFrame = true

                for (weapon in ship.allWeapons) {
                    weapon.setForceNoFireOneFrame(true)
                }

                transitionTimer.advance(realAmount)
                var level = transitionTimer.level
                var timeMult = 50f

                var color = AbyssUtils.GENESIS_COLOR

                ship!!.setJitter(this, color.setAlpha(150), level, 3, 0f, 0f)
                ship!!.setJitterUnder(this, color.setAlpha(255), level, 25, 0f, 60f)

                val realTimeMult = 1f + (timeMult - 1f) * level
                ship.mutableStats.timeMult.modifyMult("rat_boss_timemult", realTimeMult)
                Global.getCombatEngine().timeMult.modifyMult("rat_boss_timemult", 1f / realTimeMult)

                PostProcessShader.setNoise(false, 0.3f * level)
                PostProcessShader.setSaturation(false, 1f + (0.2f * level))
                Global.getSoundPlayer().applyLowPassFilter(1f, 1 - (0.3f * level))

                if (transitionTimer.state == StateBasedTimer.TimerState.Out) {
                    ship.hitpoints += (ship.maxHitpoints * 0.33f) * realAmount
                    ship.hitpoints = MathUtils.clamp(ship.hitpoints, 0f, ship.maxHitpoints)
                }

                var percentPerSecond = 0.1f
                if (ship.fluxLevel > 0f) {
                    ship.fluxTracker.decreaseFlux((ship.fluxTracker.maxFlux * percentPerSecond) * realAmount)
                }

                if (transitionTimer.state == StateBasedTimer.TimerState.Out && !activateZone) {
                    activateZone = true
                    Global.getSoundPlayer().playSound("rat_genesis_system_sound", 0.7f, 1.4f, ship.location, ship.velocity)
                    logger.debug("Starting Genesis Phase 2 Music. If the game freezes past this point, please make sure to increase the allocated ram, or change from Java23 to Java8 instead to fix the issue from happening again.")
                    //Global.getSoundPlayer().resumeCustomMusic()
                    Global.getSoundPlayer().playCustomMusic(1, 1, "rat_abyss_genesis2", true)

                    ripple = GraphicLibEffects.CustomRippleDistortion(ship!!.location, Vector2f(), ship.collisionRadius + 500, 75f, true, ship!!.facing, 360f, 1f
                        ,0.5f, 3f, 1f, 1f, 1f)


                    //Work around as spawning moduled ships can randomly cause ConcurrentModificationExceptions when called from advances and listeners attached to ships
                    Global.getCombatEngine().addPlugin(AzazelSpawnPlugin(this))

                    /*azazel1 = spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))
                    azazel2 = spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))*/

                }

                if (ripple != null) {
                    ripple!!.advance(realAmount)
                }

                if (transitionTimer.done && ship.hitpoints >= ship.maxHitpoints) {
                    transitionDone = true
                    ship.mutableStats.timeMult.modifyMult("rat_boss_timemult", 1f)
                    Global.getCombatEngine().timeMult.modifyMult("rat_boss_timemult", 1f)
                    PostProcessShader.resetDefaults()
                }
            }




            //handle worms
            var range = getPhase2Range()
            for (apparation in apparations) {





                for (weapon in apparation.allWeapons) {

                    if (MathUtils.getDistance(weapon.location, ship.location) <= range && apparation.isAlive && activateZone) {
                        weapon.sprite.color = Color(255, 255, 255, 255)
                        weapon.barrelSpriteAPI?.color =  Color(255, 255, 255, 255)
                        weapon.glowSpriteAPI?.color = weapon.glowSpriteAPI?.color!!.setAlpha(255)

                    }
                    else {
                        weapon.sprite.color = Color(0, 0, 0, 0)
                        weapon.barrelSpriteAPI?.color = Color(0, 0, 0, 0)
                        weapon.glowSpriteAPI?.color =  weapon.glowSpriteAPI?.color!!.setAlpha(0)

                    }
                }

                for (engine in apparation.engineController.shipEngines) {

                    if (MathUtils.getDistance(engine.location, ship.location) <= range && apparation.isAlive && activateZone)  {
                        //engine.repair()
                        engine.engineSlot.color = Color(178, 36, 69, 255)
                        engine.engineSlot.glowAlternateColor = Color(178, 36, 69,255)
                        engine.engineSlot.glowSizeMult = 0.8f
                    }
                    else {
                        engine.engineSlot.color = Color(0, 0, 0, 0)
                        engine.engineSlot.glowAlternateColor = Color(0, 0, 0, 0)
                        engine.engineSlot.glowSizeMult = 0f

                        //engine.disable()
                    }
                }

                //Hides that square that appears around opposing ships
                apparation.isForceHideFFOverlay = true

                if (MathUtils.getDistance(apparation.location, ship.location) <= range - apparation.collisionRadius) {
                    apparation.isPhased = false
                    apparation.isHoldFire = false
                    apparation.mutableStats.hullDamageTakenMult.modifyMult("rat_construct", 1f)
                    apparation.alphaMult = 1f
                }
                else {
                    apparation.isPhased = true
                    apparation.isHoldFireOneFrame = true
                    apparation.allWeapons.forEach { it.stopFiring() }
                    apparation.mutableStats.hullDamageTakenMult.modifyMult("rat_construct", 0f)

                    for (weapon in apparation.allWeapons) {
                        weapon.stopFiring()
                        weapon.setRemainingCooldownTo(0.5f)
                    }
                }
            }


        }

        if (phase == Phases.P2 || phase == Phases.P3) {
            particleInterval.advance(realAmount)
            if (particleInterval.intervalElapsed()) {



                var count = 25
                var fadeInOverwrite = false

                if (particles.size <= 50) {
                    count = 1000
                    fadeInOverwrite = true
                }


                for (i in 0..count) {

                    var velocity = Vector2f(0f, 0f)
                    velocity = velocity.plus(MathUtils.getPointOnCircumference(Vector2f(), MathUtils.getRandomNumberInRange(200f, 550f), MathUtils.getRandomNumberInRange(180f, 210f)))

                    var playership = Global.getCombatEngine().playerShip
                    var spawnLocation = ship.location
                    if (Random().nextFloat() >= 0.5f && playership != null) {
                        spawnLocation = playership.location
                    }
                    //var spawnLocation = MathUtils.getPointOnCircumference(Vector2f(), 45f, entity.facing + 180)

                    var randomX = MathUtils.getRandomNumberInRange(-5000f, 5000f)
                    var randomY = MathUtils.getRandomNumberInRange(-5000f, 5000f)

                    spawnLocation = spawnLocation.plus(Vector2f(randomX, randomY))

                    var fadeIn = MathUtils.getRandomNumberInRange(1f, 1.5f)
                    if (fadeInOverwrite) fadeIn = 0.05f
                    var duration = MathUtils.getRandomNumberInRange(2f, 4f)
                    var fadeOut = MathUtils.getRandomNumberInRange(1f, 2.5f)

                    var size = MathUtils.getRandomNumberInRange(25f, 50f)

                    var alpha = MathUtils.getRandomNumberInRange(0.25f, 0.45f)

                    particles.add(AbyssalStormParticleManager.AbyssalLightParticle(fadeIn,
                        duration,
                        fadeOut,
                        AbyssUtils.GENESIS_COLOR,
                        alpha,
                        size,
                        spawnLocation,
                        velocity))
                }
            }
        }
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }

    fun spawnApparation(variantId: String, captain: PersonAPI) : ShipAPI{
        var variant = Global.getSettings().getVariant(variantId)
        var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)

        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true
        var apparation = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, ship!!.currentCR, Vector2f(100000f, 100000f), ship!!.facing)
        apparation!!.fleetMember.id = Misc.genUID()
        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false

        apparation!!.captain = captain

        apparation.mutableStats.maxSpeed.modifyMult("rat_azazel_boss_buff", 1f)
        apparation.mutableStats.fluxDissipation.modifyMult("rat_azazel_boss_buff", 1.2f)
        apparation.mutableStats.fluxCapacity.modifyMult("rat_azazel_boss_buff", 1.2f)
        apparation.mutableStats.armorBonus.modifyMult("rat_azazel_boss_buff", 1.2f)

        //manager.removeDeployed(apparation, true)



        var segments = mutableListOf(apparation)
        segments.addAll(apparation.childModulesCopy)

        apparation.system.cooldownRemaining = 10f

        for (segment in segments) {
            segment.spriteAPI.color = Color(0, 0 ,0 ,0)

            for (weapon in segment.allWeapons) {
                weapon.sprite.color = Color(0, 0, 0, 0)
                weapon.barrelSpriteAPI?.color = Color(0, 0, 0, 0)
                weapon.glowSpriteAPI?.color = weapon.glowSpriteAPI!!.color.setAlpha(0)
            }

            for (engine in segment.engineController.shipEngines) {
                engine.engineSlot.color = Color(0, 0, 0, 0)
                engine.engineSlot.glowAlternateColor = Color(0, 0, 0, 0)
                engine.engineSlot.glowSizeMult = 0f
            }

            apparations.add(segment)
        }

        //Global.getCombatEngine().addEntity(apparation)

        var hard = ship.variant.hasTag("rat_challenge_mode")

        var extraRange = 0f
        if (hard) extraRange = 250f

        var loc = MathUtils.getRandomPointOnCircumference(ship.location, MathUtils.getRandomNumberInRange(1800f + extraRange, 2500f + extraRange))
        loc = findClearLocation(apparation, loc)
        apparation.location.set(loc)


        apparation.captain.setPersonality(Personalities.RECKLESS)
        apparation.shipAI = Global.getSettings().createDefaultShipAI(apparation, ShipAIConfig().apply { alwaysStrafeOffensively = true })
        apparation.shipAI.forceCircumstanceEvaluation()
        apparation.captain.setPersonality(Personalities.RECKLESS)

        return apparation
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


    private fun findClearLocation(apparation: ShipAPI, dest: Vector2f): Vector2f? {
        if (isLocationClear(apparation, dest)) return dest
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
                if (isLocationClear(apparation, loc)) {
                    return loc
                }
                angle += 60f
            }
            distIndex *= 2f
        }
        return if (tested.isEmpty) dest else tested.pick() // shouldn't happen
    }

    private fun isLocationClear(apparation: ShipAPI, loc: Vector2f): Boolean {
        for (other in Global.getCombatEngine().ships) {
            if (other.isShuttlePod) continue
            //if (other.isFighter) continue

            var otherLoc = other.shieldCenterEvenIfNoShield
            var otherR = other.shieldRadiusEvenIfNoShield
            if (other.isPiece) {
                otherLoc = other.location
                otherR = other.collisionRadius
            }


            val dist = Misc.getDistance(loc, otherLoc)
            val r = otherR
            var checkDist = apparation.shieldRadiusEvenIfNoShield * 5
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

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS, CombatEngineLayers.UNDER_SHIPS_LAYER, CombatEngineLayers.JUST_BELOW_WIDGETS, CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    fun getPhase2Range() : Float {
        var zoneLevel = 1 - transitionTimer.level
        var radius = 10000 * (zoneLevel * zoneLevel)
        if (zoneLevel >= 0.95f && phase != Phases.P3) {
            radius = 30000 * (zoneLevel * zoneLevel)
        }

        if (phase == Phases.P3) {
            radius *= phase3HealthLevel
        }

        return radius
    }

    override fun render(layer: CombatEngineLayers, viewport: ViewportAPI) {

        var width = viewport.visibleWidth
        var height = viewport.visibleHeight

        var x = viewport.llx
        var y = viewport.lly

        var color = Color(100, 0, 255)
        color = Misc.interpolateColor(color, Color(200, 0, 50), phase3TransitionTimer.level * 0.2f)

        vignetteLevel = max(transitionTimer.level, vignetteLevel)
        vignetteLevel = MathUtils.clamp(vignetteLevel, 0f, 1f)
      /*  if (transitionTimer.level >= vignetteLevel) {
            vignetteLevel = transitionTimer.level
        }*/

        var segments = 100
        var radius = getPhase2Range()


        if (layer == CombatEngineLayers.BELOW_PLANETS) {
            darken.color = Color(0, 0, 0)
            darken.alphaMult = vignetteLevel * 0.7f
            darken.setSize(viewport!!.visibleWidth + 100, viewport!!.visibleHeight + 100)
            darken.render(viewport!!.llx - 50, viewport!!.lly - 50)

            if (activateZone) {

                var zoneLevel = 1 - transitionTimer.level

                startStencil(ship!!, radius, segments)

                sprite.setSize(width, height)
                sprite.color = color
                sprite.alphaMult = 1f
                sprite.render(x, y)

                wormhole.setSize(width * 1.3f, width *  1.3f)
                wormhole.setAdditiveBlend()
                wormhole.alphaMult = 0.2f + (0.2f * phase3TransitionTimer.level)
                if (!Global.getCombatEngine().isPaused) wormhole.angle += 0.075f
                wormhole.color = Color(200, 0, 50)
                wormhole.renderAtCenter(x + width / 2, y + height / 2)

                wormhole2.setSize(width * 1.35f, width *  1.35f)
                wormhole2.setAdditiveBlend()
                wormhole2.alphaMult = 0.2f + (0.1f * phase3TransitionTimer.level)
                if (!Global.getCombatEngine().isPaused) wormhole2.angle += 0.05f
                wormhole2.color = Color(50, 0, 255)
                wormhole2.renderAtCenter(x + width / 2, y + height / 2)

                for (particle in particles) {

                    if (viewport!!.isNearViewport(particle.location, particle.size * 2)) {
                        halo!!.alphaMult = 0 + (particle.alpha * particle.level )
                        halo!!.color = particle.color
                        halo!!.setSize(particle.size / 2, particle.size / 2)
                        halo!!.setAdditiveBlend()
                        halo!!.renderAtCenter(particle.location.x, particle.location.y)
                    }
                }

                endStencil()

                renderBorder(ship!!, radius, color, segments)
            }
        }

        if (activateZone) {
            if (layer == CombatEngineLayers.UNDER_SHIPS_LAYER) {

                startStencil(ship!!, radius, segments)

                renderShips()

                endStencil()

            }
        }




        if (layer == CombatEngineLayers.JUST_BELOW_WIDGETS) {
            vignette.color = AbyssUtils.GENESIS_COLOR.darker()
            vignette.alphaMult = (0.3f * vignetteLevel) + (0.2f * phase3TransitionTimer.level)

            var offset = 300
            vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
            vignette.render(viewport!!.llx - (offset * 0.5f), viewport!!.lly - (offset * 0.5f))
        }

        if (layer == CombatEngineLayers.ABOVE_SHIPS_LAYER && (phase == Phases.P2 || phase == Phases.P3) && ship.isAlive) {
            systemGlow.setNormalBlend()
            systemGlow.alphaMult = (0.8f + (0.2f * fader.brightness)) * vignetteLevel
            systemGlow.angle = ship.facing - 90
            systemGlow.renderAtCenter(ship.location.x, ship.location.y)

            systemGlow.setAdditiveBlend()
            systemGlow.alphaMult = ((0.5f * fader.brightness)) * vignetteLevel
            systemGlow.angle = ship.facing - 90
            systemGlow.renderAtCenter(ship.location.x, ship.location.y)

            doJitter(systemGlow, 0.5f * vignetteLevel, lastJitterLocations, 5, 2f)
            doJitter(systemGlow, 0.3f * vignetteLevel, lastSecondJitterLocations, 5, 12f)
        }
    }

    fun doJitter(sprite: SpriteAPI, level: Float, lastLocations: ArrayList<Vector2f>, jitterCount: Int, jitterMaxRange: Float) {

        var paused = Global.getCombatEngine().isPaused
        var jitterAlpha = 0.2f


        if (!paused) {
            lastLocations.clear()
        }

        for (i in 0 until jitterCount) {

            var jitterLoc = Vector2f()

            if (!paused) {
                var x = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)
                var y = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)

                jitterLoc = Vector2f(x, y)
                lastLocations.add(jitterLoc)
            }
            else {
                jitterLoc = lastLocations.getOrElse(i) {
                    Vector2f()
                }
            }

            sprite.setAdditiveBlend()
            sprite.alphaMult = level * jitterAlpha
            sprite.renderAtCenter(ship.location.x + jitterLoc.x, ship.location.y + jitterLoc.y)
        }
    }

    fun renderShips() {

        var range = getPhase2Range()
        for (apparation in apparations ) {

            if (!apparation.isAlive) continue

            var inRange = MathUtils.getDistance(apparation.location, ship.location) <= range - apparation.collisionRadius

            if (inRange) {
                apparation.spriteAPI.color = Color(255, 255, 255, 255)
            }
            else {
                apparation.spriteAPI.color = Color(255, 255, 255, 255)
                apparation.spriteAPI.renderAtCenter(apparation.location.x, apparation.location.y)
                apparation.spriteAPI.color = Color(0, 0 ,0 ,0)
            }





            /*for (weapon in apparation.allWeapons) {
                weapon.sprite?.alphaMult = 1f
                weapon.sprite?.renderAtCenter(weapon.location.x, weapon.location.y)
                weapon.sprite?.alphaMult = 0f
            }*/
        }
    }

    override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?, point: Vector2f?, damageAmount: Float): Boolean {

        //remove "transitionDone" from here at some point after testing
        if ((phase == Phases.P1 || phase == Phases.P2)/* && !transitionDone*/) {

            if (phase == Phases.P2 ) {
                return true
            }

            if (ship!!.hitpoints - damageAmount <= 100) {

                if (phase == Phases.P1) {

                    ship.hitpoints = 10f
                    ship.fluxTracker.stopOverload()

                    phase = Phases.P2
                    ship.setCustomData("rat_boss_second_phase", true)

                    var color = AbyssUtils.GENESIS_COLOR

                    for (i in 0 until 100) {
                        ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                        var from = Vector2f(ship!!.exactBounds.segments.random().p1)

                        var angle = Misc.getAngleInDegrees(ship.location, from)
                        var to = MathUtils.getPointOnCircumference(ship.location, MathUtils.getRandomNumberInRange(100f, 300f) + ship.collisionRadius, angle + MathUtils.getRandomNumberInRange(-30f, 30f))

                        Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, color, color)
                    }

                    Global.getSoundPlayer().playSound("rat_bloodstream_trigger", 1f, 2f, ship.location, ship.velocity)
                    Global.getSoundPlayer().playSound("system_entropy", 1f, 1.5f, ship.location, ship.velocity)
                    Global.getSoundPlayer().playSound("explosion_ship", 1f, 1f, ship.location, ship.velocity)

                    GraphicLibEffects.CustomRippleDistortion(Vector2f(ship.location), Vector2f(), 3000f, 10f, true, ship.facing, 360f, 1f
                        ,1f, 1f, 1f, 1f, 1f)

                    GraphicLibEffects.CustomBubbleDistortion(Vector2f(ship.location), Vector2f(), 1000f + ship.collisionRadius, 25f, true, ship.facing, 360f, 1f
                        ,0.1f, 0.1f, 1f, 0.3f, 1f)

                    //Global.getSoundPlayer().playCustomMusic(1, 1, "rat_abyss_genesis2", true)
                    Global.getSoundPlayer().pauseCustomMusic()

                    /*azazel1 = spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))
                    azazel2 = spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))*/

                    //var apparation2 = spawnApparation("rat_genesis_serpent_head_Standard", CosmosCore().createPerson(RATItems.COSMOS_CORE, "rat_abyssals_primordials", Random()))

                }

                return true

            }

        }


        return false

    }

    fun handleParticles(amount: Float) {
        for (particle in ArrayList(particles)) {

            if (particle.state == AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.FadeIn) {
                particle.fadeIn -= 1 * amount

                var level = (particle.fadeIn - 0f) / (particle.maxFadeIn - 0f)
                particle.level = 1 - level

                if (particle.fadeIn < 0) {
                    particle.state = AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.Mid
                }
            }

            if (particle.state == AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.Mid) {
                particle.duration -= 1 * amount


                particle.level = 1f

                if (particle.duration < 0) {
                    particle.state = AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.FadeOut
                }
            }

            if (particle.state == AbyssalStormParticleManager.AbyssalLightParticle.ParticleState.FadeOut) {
                particle.fadeOut -= 1 * amount

                particle.level = (particle.fadeOut - 0f) / (particle.maxFadeOut - 0f)

                if (particle.fadeOut < 0) {
                    particles.remove(particle)
                    continue
                }
            }

            particle.adjustmentInterval.advance(amount)
            if (particle.adjustmentInterval.intervalElapsed()) {
                var velocity = Vector2f(0f, 0f)
                particle.adjustment = MathUtils.getRandomNumberInRange(-1f, 1f)
            }

            particle.velocity = particle.velocity.rotate(particle.adjustment * amount)


            var x = particle.velocity.x * amount
            var y = particle.velocity.y * amount
            var velocity = Vector2f(x, y)
            particle.location = particle.location.plus(velocity)
        }
    }

    fun startStencil(ship: ShipAPI, radius: Float, circlePoints: Int) {

        GL11.glClearStencil(0);
        GL11.glStencilMask(0xff);
        //set everything to 0
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        //disable drawing colour, enable stencil testing
        GL11.glColorMask(false, false, false, false); //disable colour
        GL11.glEnable(GL11.GL_STENCIL_TEST); //enable stencil

        // ... here you render the part of the scene you want masked, this may be a simple triangle or square, or for example a monitor on a computer in your spaceship ...
        //begin masking
        //put 1s where I want to draw
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff); // Do not test the current value in the stencil buffer, always accept any value on there for drawing
        GL11.glStencilMask(0xff);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE); // Make every test succeed

        // <draw a quad that dictates you want the boundaries of the panel to be>

        GL11.glBegin(GL11.GL_POLYGON) // Middle circle

        val x = ship.location.x
        val y = ship.location.y

        for (i in 0..circlePoints) {

            val angle: Double = (2 * Math.PI * i / circlePoints)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()

        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun startBarStencil(x: Float, y: Float, width: Float, height: Float, percent: Float) {
        GL11.glClearStencil(0);
        GL11.glStencilMask(0xff);
        //set everything to 0
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        //disable drawing colour, enable stencil testing
        GL11.glColorMask(false, false, false, false); //disable colour
        GL11.glEnable(GL11.GL_STENCIL_TEST); //enable stencil

        // ... here you render the part of the scene you want masked, this may be a simple triangle or square, or for example a monitor on a computer in your spaceship ...
        //begin masking
        //put 1s where I want to draw
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff); // Do not test the current value in the stencil buffer, always accept any value on there for drawing
        GL11.glStencilMask(0xff);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE); // Make every test succeed

        // <draw a quad that dictates you want the boundaries of the panel to be>

        GL11.glRectf(x, y, x + (width * percent), y + height)

        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>
    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    fun renderBorder(ship: ShipAPI, radius: Float, color: Color, circlePoints: Int) {
        var c = color
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (1f))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        val x = ship.location.x
        val y = ship.location.y


        for (i in 0..circlePoints) {
            val angle: Double = (2 * Math.PI * i / circlePoints)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }
}