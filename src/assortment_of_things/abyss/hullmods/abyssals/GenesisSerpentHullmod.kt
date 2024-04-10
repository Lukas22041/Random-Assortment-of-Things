package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.util.IntervalTracker
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList


class GenesisSerpentHullmod : BaseHullMod() {



    override fun applyEffectsBeforeShipCreation(hullSize: HullSize, stats: MutableShipStatsAPI, id: String) {
        var member = stats.fleetMember

        if (!stats!!.variant.hasHullMod("rat_abyssal_conversion") && !stats!!.variant.hasHullMod("rat_chronos_conversion") && !stats!!.variant.hasHullMod("rat_cosmos_conversion") && !stats!!.variant.hasHullMod("rat_seraph_conversion")  && !stats.variant.hasHullMod(
                HullMods.AUTOMATED)) {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
        }

        if (stats.fleetMember?.fleetData?.fleet?.faction?.id == "rat_abyssals_primordials") {
            stats.crewLossMult.modifyMult("test", 0f)
            stats.crLossPerSecondPercent.modifyMult("test", 0f)
        } else {
            if (Global.getSector()?.characterData?.person != null) {
                if (Global.getSector().characterData.person!!.stats.hasSkill(Skills.AUTOMATED_SHIPS)
                    || stats!!.variant.hasHullMod("rat_abyssal_conversion") ||
                    stats!!.variant.hasHullMod("rat_chronos_conversion") || stats!!.variant.hasHullMod("rat_cosmos_conversion") || stats!!.variant.hasHullMod("rat_seraph_conversion")) {
                    stats!!.variant.removeTag(Tags.VARIANT_UNBOARDABLE)
                }
                else {
                    stats!!.variant.addTag(Tags.VARIANT_UNBOARDABLE)
                }
            }
        }
    }

    override fun getDisplaySortOrder(): Int {
        return 0
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {
        if (ship.shield != null) {
            ship.shield.setRadius(ship.shieldRadiusEvenIfNoShield, "graphics/fx/rat_primordial_shields256.png", "graphics/fx/rat_primordial_shields256ring.png")
        }

        Global.getCombatEngine()?.addLayeredRenderingPlugin(GenesisSerpentRenderer(ship))
        ship.addListener(GenesisSerpentWormBehavior(ship))
    }

    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        var flamer = ship.allWeapons.find { it.spec.weaponId == "rat_genesis_construct_flamer_large" }

        if (flamer != null) {
            flamer.ammoTracker.ammoPerSecond = flamer.spec.ammoPerSecond / ship.mutableStats.energyAmmoRegenMult.modified

            flamer.maxAmmo = 80
            flamer.ammo = MathUtils.clamp(flamer!!.ammo, 0, 80)

        }


        var flags = ship.aiFlags
        flags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF)


    }

    override fun shouldAddDescriptionToTooltip(hullSize: HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight, AbyssUtils.GENESIS_COLOR.brighter())
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("The ship operates based on a link of several flexible modules. " +
                "Each module has its own layer of armor, however its hitpoints are shared across all parts. \n\n" +
                "Additionaly, due to its method of construction, the ship is unable to strafe left or right and can only turn forwards, backwards and turn sideways.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "modules", "layer of armor", "hitpoints are shared across all parts.", "unable to strafe")

        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
        }

    }



    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return true
    }


    class GenesisSerpentRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

        var weaponGlow = Global.getSettings().getAndLoadSprite("graphics/weapons/abyss/genesis/rat_genesis_flamer_large.png")
        var weaponAlpha = 0f
        var lastWeaponJitterLocation = ArrayList<Vector2f>()

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
        }

        override fun getRenderRadius(): Float {
            return 100000f
        }

        override fun advance(amount: Float) {
            var flamer = ship.allWeapons.find { it.spec.weaponId == "rat_genesis_construct_flamer_large" } ?: return
            if (flamer!!.isFiring) {
                weaponAlpha += 10f * amount
            }
            else {
              weaponAlpha -= 1f  * amount
            }
            weaponAlpha = weaponAlpha.coerceIn(0f, 1f)
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

            var flamer = ship.allWeapons.find { it.spec.weaponId == "rat_genesis_construct_flamer_large" } ?: return


            var loc = MathUtils.getPointOnCircumference(flamer.location, 12f, ship.facing)

            var mult = 1f
            if (ship.isPhased) mult = 0f

            weaponGlow.color = flamer.sprite.color
            weaponGlow.alphaMult = weaponAlpha * mult
            weaponGlow.angle = ship.facing - 90
            weaponGlow.setAdditiveBlend()
            weaponGlow.renderAtCenter(loc.x , loc.y)

            doJitter(weaponGlow, weaponAlpha, lastWeaponJitterLocation, loc, 5, 4f)

            renderSystemGlow()
        }

        fun renderSystemGlow() {


            var level = ship.system.effectLevel

            var modules = listOf(ship) + ship.childModulesCopy
            for (module in modules) {

                if (!module.isAlive) continue


                var moduleGlow = module.customData.get("rat_ship_glow") as SpriteAPI?
                if (moduleGlow == null) {
                    moduleGlow = Global.getSettings().getAndLoadSprite(module.hullSpec.spriteName.replace(".png", "") + "_glow.png")
                    module.setCustomData("rat_ship_glow", moduleGlow)
                }

                var lastLocation = module.customData.get("rat_apparation_glow_locations") as ArrayList<Vector2f>?
                if (lastLocation == null) {
                    lastLocation = ArrayList<Vector2f>()
                    module.setCustomData("rat_apparation_glow_locations", lastLocation)
                }

                val sprite = module.spriteAPI
                val offsetX = sprite.width / 2 - sprite.centerX
                val offsetY = sprite.height / 2 - sprite.centerY
                val trueOffsetX = FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble()))
                    .toFloat() * offsetX - FastTrig.sin(Math.toRadians((module.facing - 90f).toDouble()))
                    .toFloat() * offsetY
                val trueOffsetY = FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble()))
                    .toFloat() * offsetX + FastTrig.cos(Math.toRadians((module.facing - 90f).toDouble()))
                    .toFloat() * offsetY

                var trueLoc = Vector2f(trueOffsetX, trueOffsetY)
                trueLoc = module.location.plus(trueLoc)

                moduleGlow.setAdditiveBlend()
                moduleGlow.alphaMult = 1f * level
                moduleGlow.angle = module.facing - 90
                moduleGlow.renderAtCenter(trueLoc.x, trueLoc.y)



                doJitter(moduleGlow, level * 0.5f, lastLocation, trueLoc, 15, 8f)
            }
        }

        fun doJitter(sprite: SpriteAPI, level: Float, lastLocations: ArrayList<Vector2f>, loc: Vector2f, jitterCount: Int, jitterMaxRange: Float) {

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
                sprite.renderAtCenter(loc.x + jitterLoc.x, loc.y + jitterLoc.y)
            }
        }
    }

    //Based on Code from ED-Shipyards, which is based on KT_SinuousBody by Sinosauropteryx in Kingdom of Terra
    class GenesisSerpentWormBehavior(var ship: ShipAPI) : AdvanceableListener {

        private val parentInterval = IntervalTracker(.15f, .25f)
        private val repulseInterval = IntervalTracker(.1f, .1f)

        val NUMBER_OF_SEGMENTS = 4
        var RANGE = 60f // Flexibility constant. Range of movement of each segment.
        var REALIGNMENT_CONSTANT = 100f // Elasticity constant. How quickly the body unfurls after being curled up.
        private val SEGMENT_NAMES = arrayOf("SEGMENT1", "SEGMENT2", "SEGMENT3", "SEGMENT4")

        //NOTE! Careful trying to optimize this code, things can easily stop working

        override fun advance(amount: Float) {


            ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
            ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)

            val children = ship.childModulesCopy
            advanceParent(ship, children, amount)
            for (s in children) {
                advanceChild(s, ship)
            }
            var wagonSegments = getTrainWagon(children)
            var hitpoints = ship.hitpoints
            for (child in children) {
                if (child.hitpoints < hitpoints) {
                    hitpoints = child.hitpoints
                }
            }



            ship.hitpoints = hitpoints
            for (child in children) {
                child.hitpoints = hitpoints

            }

            wagonSegments = removeDeadSegments(wagonSegments);

            // Iterates through each SinuousSegment
            for (tw in wagonSegments) {
                try {
                    // First segment is "vanilla" / attached to mothership. Rest are pseudo-attached to previous segment's SEGMENT slot
                    if (!tw!!.isFirst) {
                        tw.ship!!.location.set(tw.previousSegment!!.ship!!.hullSpec.getWeaponSlotAPI("SEGMENT")
                            .computePosition(tw.previousSegment!!.ship))
                    }

                    val difference = ship.angularVelocity * amount
                    var angle = tw!!.ship!!.stationSlot.angle - difference

                    // angle of module is offset by angle of previous module, normalized to between 180 and -180
                    var angleOffset = normalizeAngle(tw.ship!!.facing - tw.previousSegment!!.ship!!.facing)
                    //float angleOffset = normalizeAngle(90f - tw.previousSegment.ship.getFacing());
                    if (angleOffset > 180f) angleOffset -= 360f

                    // angle of range check is offset by angle of previous segment in relation to mothership
                    val localMod = normalizeAngle(tw.previousSegment!!.ship!!.facing - tw.ship!!.parentStation.facing)

                    // range limit handler. If the tail is outside the max range, it won't swing any farther.
                    if (angleOffset < RANGE * -0.5) angle = normalizeAngle(RANGE * -0.5f + localMod)
                    if (angleOffset > RANGE * 0.5) angle = normalizeAngle(RANGE * 0.5f + localMod)

                    // Tail returns to straight position, moving faster the more bent it is - spring approximation
                    angle -= angleOffset / RANGE * 0.5f * (REALIGNMENT_CONSTANT * amount)
                    tw.ship!!.stationSlot.angle = angle
                } catch (ignored: Exception) {
                    // This covers the gap between when a segment and its dependents die
                }
                try {
                    // parent vents, children vent
                    if (ship.fluxTracker.isVenting && !tw!!.ship!!.fluxTracker.isVenting && tw.ship!!.fluxTracker.currFlux > 1000 && tw!!.ship!!.isAlive) {
                        tw.ship!!.giveCommand(ShipCommand.VENT_FLUX, null, 0)
                        return
                    }

                    // try to shoot what the player is targeting
                    if (ship.shipTarget != null && ship.shipTarget !== ship.shipTarget) {
                        tw!!.ship!!.shipTarget = ship.shipTarget
                    }

                    //propagate fighter commands
                    if (tw!!.ship!!.hasLaunchBays()) {
                        if (tw.ship!!.isPullBackFighters != tw.ship!!.isPullBackFighters) {
                            tw.ship!!.isPullBackFighters = tw.ship!!.isPullBackFighters
                        }
                        if (tw.ship!!.aiFlags != null) {
                            if (Global.getCombatEngine().playerShip === ship || ship.aiFlags == null && ship.shipTarget != null) {
                                tw.ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET,
                                    1f,
                                    ship.shipTarget)
                            } else if ((ship.aiFlags != null && ship.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET)) && ship.aiFlags.getCustom(
                                    ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET) != null) {
                                tw.ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET,
                                    1f,
                                    ship.aiFlags.getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET))
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
            repulseNearbyShips(wagonSegments, amount)
        }

        private fun repulseNearbyShips(wagonSegments: Array<TrainWagon?>, amount: Float) {
            repulseInterval.advance(amount)
            if (!repulseInterval.intervalElapsed()) {
                return
            }

            // to try and prevent collisions, "push" nearby ships away tangentially from this segment
            try {
                for (tw in wagonSegments) {
                    for (near in CombatUtils.getShipsWithinRange(tw!!.ship!!.location, 150f)) {
                        if (near.isShuttlePod || near.isFighter || near.isDrone || !near.isAlive) {
                            continue
                        }
                        if (near.isPhased || near.owner != tw.ship!!.owner) {
                            continue
                        }

                        // exempt both the front and the wagons
                        if (near.hullSpec.baseHullId.startsWith("rat_genesis_serpent")) {
                            continue
                        }

                        // some ships have insane collision radii, protect against that
                        if (MathUtils.getDistance(near.location, tw.ship!!.location) > 350) {
                            continue
                        }

                        // don't make them go crazy fast, that causes other problems
                        if (near.velocity.length() < 100) {
                            var force = 300
                            // if they are getting really close, push harder
                            if (MathUtils.isWithinRange(tw.ship!!.location, near.location, 25f)) {
                                force *= 2
                            }
                            CombatUtils.applyForce(getRoot(near),
                                VectorUtils.getDirectionalVector(tw.ship!!.location, near.location),
                                force.toFloat())
                        }


                    }
                }
            } catch (ignored: Exception) {
            }
        }

        private fun normalizeAngle(f: Float): Float {
            if (f < 0f) return f + 360f
            return if (f > 360f) f - 360f else f
        }

        private fun advanceParent(parent: ShipAPI, children: List<ShipAPI>, amount: Float) {
            parentInterval.advance(amount)
            if (!parentInterval.intervalElapsed()) {
                return
            }
            val ec = parent.engineController
            if (ec != null) {
                val originalMass = 17000f
                val originalEngines = 16
                val thrustPerEngine = originalMass / originalEngines

                /* Don't count parent's engines for this stuff - game already affects stats */
                var workingEngines = ec.shipEngines.size.toFloat()
                for (child in children) {
                    if (child.parentStation === parent && child.stationSlot != null && child.isAlive) {
                        val cec = child.engineController
                        if (cec != null) {
                            var contribution = 0f
                            for (ce in cec.shipEngines) {
                                if (ce.isActive && !ce.isDisabled && !ce.isPermanentlyDisabled && !ce.isSystemActivated) {
                                    contribution += ce.contribution
                                }
                            }
                            workingEngines += cec.shipEngines.size * contribution
                        }
                    }
                }
                val thrust = workingEngines * thrustPerEngine
                val enginePerformance = thrust / Math.max(1f, getTrainMass(parent, children))/* parent.getMutableStats().getZeroFluxSpeedBoost().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getTurnAcceleration().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getAcceleration().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getMaxTurnRate().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getMaxSpeed().modifyMult("ED_trainlocomotive", enginePerformance);*/
            }
        }

        fun getTrainWagon(childModules: List<ShipAPI>): Array<TrainWagon?> {
            // yeah, this is inefficient building this every frame, but if you cache this value,
            // sometimes the ship, or another ship in combat stops working.  Don't mess with this.
            val segments = arrayOfNulls<TrainWagon>(NUMBER_OF_SEGMENTS)
            for (f in segments.indices) {
                // Iterates through SinuousSegment array and connects them in order
                segments[f] = TrainWagon()
                if (f > 0) {
                    segments[f]!!.previousSegment = segments[f - 1]
                    segments[f - 1]!!.nextSegment = segments[f]
                }

                // Assigns each module to a segment based on its station slot name
                for (s in childModules) {
                    s.ensureClonedStationSlotSpec()
                    if (s.stationSlot != null && s.stationSlot.id == SEGMENT_NAMES[f]) {
                        segments[f]!!.ship = s

                        // First module: Assigns mothership as its previousSegment
                        if (f == 0) {
                            segments[f]!!.previousSegment = TrainWagon()
                            segments[f]!!.previousSegment!!.ship = s.parentStation
                            segments[f]!!.previousSegment!!.nextSegment = segments[f]
                        }
                    }
                }
            }
            return segments
        }

        fun removeDeadSegments(wagonSegments: Array<TrainWagon?>): Array<TrainWagon?> {
            val ret = ArrayList<TrainWagon>()
            for (f in 0 until NUMBER_OF_SEGMENTS) {
                val tw = wagonSegments[f]
                if (tw != null && tw.ship != null && tw.ship!!.isAlive) {
                    ret.add(tw)
                } else {
                    // When a segment dies, remove all dependent segments
                    for (g in f until NUMBER_OF_SEGMENTS) {
                        if (wagonSegments[g] != null && wagonSegments[g]!!.ship != null && wagonSegments[g]!!.ship!!.isAlive) {
                            wagonSegments[g]!!.ship!!.hitpoints = 1f
                            try {
                                Global.getCombatEngine().applyDamage(wagonSegments[g]!!.ship,
                                    wagonSegments[g]!!.ship!!.location,
                                    1000f,
                                    DamageType.HIGH_EXPLOSIVE,
                                    0f,
                                    true,
                                    false,
                                    null)
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                }
            }
            return ret.toArray(arrayOf())
        }

        class TrainWagon {
            //Based on KT_SinuousSegment by Sinosauropteryx (Kingdom of Terra mod)
            var ship: ShipAPI? = null // ShipAPI means we can't keep a member reference to any TrainWagon objects
            var nextSegment: TrainWagon? = null
            var previousSegment: TrainWagon? = null

            val isFirst: Boolean
                get() = previousSegment != null && previousSegment!!.ship!!.hullSpec.baseHullId == "rat_genesis_serpent_head"
        }


        //////////
        // This section of code was taken largely from the Ship and Weapon Pack mod.
        // I did not create it. Credit goes to DarkRevenant.
        //////////
        private fun advanceChild(child: ShipAPI, parent: ShipAPI) {
            val ec = parent.engineController
            if (ec != null) {
                if (parent.isAlive) {
                    if (ec.isAccelerating) {
                        child.giveCommand(ShipCommand.ACCELERATE, null, 0)
                    }
                    if (ec.isAcceleratingBackwards) {
                        child.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0)
                    }
                    if (ec.isDecelerating) {
                        child.giveCommand(ShipCommand.DECELERATE, null, 0)
                    }
                    if (ec.isStrafingLeft) {
                        child.giveCommand(ShipCommand.STRAFE_LEFT, null, 0)
                    }
                    if (ec.isStrafingRight) {
                        child.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0)
                    }
                    if (ec.isTurningLeft) {
                        child.giveCommand(ShipCommand.TURN_LEFT, null, 0)
                    }
                    if (ec.isTurningRight) {
                        child.giveCommand(ShipCommand.TURN_RIGHT, null, 0)
                    }
                }
                val cec = child.engineController
                if (cec != null) {
                    if ((ec.isFlamingOut || ec.isFlamedOut) && !cec.isFlamingOut && !cec.isFlamedOut) {
                        child.engineController.forceFlameout(true)
                    }
                }
            }
        }

        private fun getTrainMass(ship: ShipAPI, modules: List<ShipAPI>?): Float {
            var mass = ship.mass
            if (modules != null) {
                for (m in modules) {
                    if (m != null && m.isAlive) {
                        mass += m.mass
                    }
                }
            }
            return mass
        }

        fun isMultiShip(ship: ShipAPI): Boolean {
            return ship.parentStation != null || ship.isShipWithModules
        }

        fun getRoot(ship: ShipAPI): ShipAPI {
            return if (isMultiShip(ship)) {
                var root = ship
                while (root.parentStation != null) {
                    root = root.parentStation
                }
                root
            } else {
                ship
            }
        }

    }

}