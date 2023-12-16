package assortment_of_things.exotech

import assortment_of_things.misc.instantTeleport
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.starfarer.campaign.fleet.CampaignFleetMemberView
import com.fs.starfarer.campaign.fleet.CampaignFleetView
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.caresAboutPlayerTransponder
import org.magiclib.kotlin.getDistance
import java.awt.Color
import java.util.*

class ExoshipStateScript(var exoship: SectorEntityToken) : EveryFrameScript, FleetEventListener {

    var data = ExoUtils.getExoshipData(exoship)

    var destination: SectorEntityToken? = null


    var previousVelocity = Vector2f()
    var daysForTransfer = 1f

    var maxFleets = 4
    var remainingFleetBudget = maxFleets
    var launchDelayDays = 0.25f
    var delayTimestamp = Global.getSector().clock.timestamp


    var maxSecondsTravel = 8f
    var maxSecondsArrival = 5f

    var secondsTravel = 0f
    var secondsArrival = 0f

    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }


    override fun advance(amount: Float) {

        var daysTilMove = data.getTimeTilNextMove()

        var maxDist = 2
        val distFromSource = Misc.getDistanceLY(Global.getSector().playerFleet.getLocationInHyperspace(), exoship.locationInHyperspace)

        for (fleet in ArrayList(data.fleets)) {
            if (fleet.isDespawning || fleet.isExpired) {
                data.fleets.remove(fleet)
            }
            if (distFromSource >= maxDist && Global.getSector().playerFleet.containingLocation != fleet.containingLocation
                && fleet.memoryWithoutUpdate.get("\$do_not_despawn") != true) {
                fleet.despawn()
            }
        }

        if (daysTilMove <= 20) {
            for (fleet in data.fleets) {
                if (fleet.currentAssignment?.target != exoship && data.state == ExoShipData.State.Idle  && fleet.containingLocation == exoship.containingLocation) {
                    fleet.clearAssignments()
                    fleet.removeScriptsOfClass(RemnantAssignmentAI::class.java)
                    if (Random().nextFloat() >= 0.5f) {
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, exoship, 30f, "Returning to Exoship")
                    }
                    else {
                        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, exoship, 30f, "Returning to Exoship")
                    }
                }
                if (fleet.currentAssignment?.target == exoship && (fleet.containingLocation != exoship.containingLocation || data.state != ExoShipData.State.Idle)) {
                    fleet.clearAssignments()
                }
            }
        }
        else if (remainingFleetBudget != 0 && data.fleets.size <= maxFleets){
            if (Global.getSector().clock.getElapsedDaysSince(delayTimestamp!!) > launchDelayDays && distFromSource <= maxDist) {
                var fleet = spawnFleet()
            }
        }

        if (daysTilMove <= 0 && data.state == ExoShipData.State.Idle) {
            var system = findSystemToMoveTo() ?: return
            var location = BaseThemeGenerator.getLocations(Random(), system, MathUtils.getRandomNumberInRange(400f, 600f), linkedMapOf(
                /*BaseThemeGenerator.LocationType.STAR_ORBIT to 5f,*/ BaseThemeGenerator.LocationType.OUTER_SYSTEM to 1f)).pick() ?: return

            exoship.orbit = null
            exoship.velocity.set(Vector2f(0f, 0f))
            exoship.addTag(Tags.NON_CLICKABLE)

            var token = system.createToken(Vector2f())
            if (location.orbit == null) return
            token.orbit = location.orbit
            destination = token
            data.state = ExoShipData.State.Travelling

            secondsArrival = maxSecondsArrival
            secondsTravel = 0f
        }




        if (data.state == ExoShipData.State.Travelling) {

          /*  var width = Global.getSettings().screenWidth
            var height = Global.getSettings().screenHeight

            Global.getSector().viewport.isExternalControl = true
            Global.getSector().viewport.set(exoship.location.x - (width / 2), exoship.location.y - (height / 2), width , height)
*/


            if (!Global.getSector().isPaused) {
                secondsTravel += 1 * amount
            }

            var level = (secondsTravel - 0) / ((maxSecondsTravel) - 0)
            level = MathUtils.clamp(level, 0f, 1f)
            data.moveLevel = level

            if (Global.getSector().playerFleet.containingLocation == exoship.containingLocation) {
                Global.getSoundPlayer().playLoop("ui_emergency_burn_loop", exoship, 1.5f, 1f * level, exoship.location, exoship.velocity)
                if (level >= 0.7) {
                    Global.getSoundPlayer().playLoop("disintegrator_loop", exoship, 1f, 4f * level, exoship.location, exoship.velocity)
                }
            }

          /*  var dest = MathUtils.getPointOnCircumference(exoship.location, 200f, exoship.facing + 45)
            if (level >= 0.2f) {
                Global.getSector().playerFleet.setVelocity(exoship.velocity.x, exoship.velocity.y)
                dest = MathUtils.getPointOnCircumference(exoship.location, 250f, exoship.facing)
            }
            Global.getSector().playerFleet.setMoveDestination(dest.x, dest.y)*/



            travelToSystem(amount, level)
        }
        if (data.state == ExoShipData.State.Arriving) {

            if (!Global.getSector().isPaused) {
                secondsArrival -= 1 * amount
            }

            var level = (secondsArrival - 0) / ((maxSecondsArrival) - 0)
            level = MathUtils.clamp(level, 0f, 1f)
            data.moveLevel = level

            if (Global.getSector().playerFleet.containingLocation == exoship.containingLocation) {
                Global.getSoundPlayer().playLoop("ui_emergency_burn_loop", exoship, 1.5f, 1f * level, exoship.location, exoship.velocity)
                if (level >= 0.7) {
                    Global.getSoundPlayer().playLoop("disintegrator_loop", exoship, 1f, 4f * level, exoship.location, exoship.velocity)
                }
            }


            arriveInSystem(amount, level)
        }
    }

    fun findSystemToMoveTo() : StarSystemAPI {
        var systems = Global.getSector().starSystems.filter {!it.hasTag(Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag( Tags.THEME_HIDDEN)}


        var picker = WeightedRandomPicker<StarSystemAPI>()
        for (system in systems) {
            var weight = 1f

            if (system == exoship.containingLocation) continue
            if (system == Global.getSector().playerFleet.containingLocation) weight += 1
            if (Misc.getDistanceLY(system.location, Global.getSector().playerFleet.locationInHyperspace) <= 5) weight += 1
            if (system.hasBlackHole()) weight += 2
            if (system.customEntities.any { it.customEntitySpec.id == "rat_exoship" }) weight *= 0.5f

            picker.add(system, weight)
        }

        if (picker.isEmpty) {
            return systems.random()
        }

        var system = picker.pick()

        return system

    }

    fun travelToSystem(amount: Float, level: Float) {

        var direction = MathUtils.getPointOnCircumference(Vector2f(0f, 0f), 200 * amount * level, exoship.facing)
        exoship.velocity.set(exoship.velocity.plus(direction))

        for (fleet in data.fleets) {
            if (MathUtils.getDistance(fleet, exoship) >= 500) continue

            var angle = Misc.getAngleInDegrees(exoship.location, fleet.location)
            var dest = MathUtils.getPointOnCircumference(exoship.location, 200f, angle)
            if (level >= 0.2f) {
                fleet.setVelocity(exoship.velocity.x, exoship.velocity.y)
                dest = MathUtils.getPointOnCircumference(exoship.location, 250f, exoship.facing)
            }
            fleet.setMoveDestination(dest.x, dest.y)

            if (level >= 0.7f) {
                var views = fleet.views
                for (view in views) {
                    if (view is CampaignFleetMemberView) {
                        if (!view.isJittering) {
                            view.setJitter(secondsArrival + 1, 1f, Color(248, 149, 44, 155), 7, 20f)
                        }
                    }
                }
            }
        }

        if (level >= 1f) {
            triggerTeleport()
        }
    }

    fun triggerTeleport() {
        data.state = ExoShipData.State.Arriving

        var token = exoship.containingLocation.createToken(exoship.location)

        var ping = CampaignPingSpec()
        ping.color = Color(248, 149, 44)
        ping.range = 4000f
        ping.minRange = exoship.radius
        ping.duration = 2f
        ping.alphaMult = 1f
        ping.width = 10f
        ping.num = 5
        ping.delay = 0.1f
        Global.getSector().addPing(token, ping)

       // Global.getSoundPlayer().playSound("system_phase_teleporter", 1f, 0.5f, exoship.location, exoship.velocity)
        if (Global.getSector().playerFleet.containingLocation == exoship.containingLocation) {
            Global.getSoundPlayer().playSound("ui_interdict_off", 0.75f, 0.5f, exoship.location, exoship.velocity)
            Global.getSoundPlayer().playSound("ui_interdict_off", 1.25f, 0.5f, exoship.location, exoship.velocity)
        }

        var teleportedFleets = data.fleets.filter { it.getDistance(exoship) <= 600 }

        var destinationSystem = destination!!.containingLocation
        var currentLocation = exoship.containingLocation

        currentLocation.removeEntity(exoship)
        destinationSystem.addEntity(exoship)



        var point = MathUtils.getPointOnCircumference(destination!!.location, 2000f, exoship.facing + 180)
        exoship.location.set(point)

        previousVelocity = Vector2f(exoship.velocity)

        for (fleet in teleportedFleets) {
            currentLocation.removeEntity(fleet)
            destinationSystem.addEntity(fleet)

            fleet.memoryWithoutUpdate.set("\$do_not_despawn", true, 1f)

            var fleetPoint = MathUtils.getRandomPointOnCircumference(exoship.location, MathUtils.getRandomNumberInRange(200f, 300f))
            fleet.setLocation(fleetPoint.x, fleetPoint.y)
            fleet.setVelocity(exoship.velocity.x, exoship.velocity.y)

            var views = fleet.views
            for (view in views) {
                if (view is CampaignFleetMemberView) {
                    if (!view.isJittering) {
                        view.setJitter(0f, maxSecondsArrival, Color(248, 149, 44, 155), 7, 20f)
                    }
                }
            }
        }

       // Global.getSector().addPing(exoship, ping)
        callNewSystemPing()
        callSystemPing()

       // Global.getSector().instantTeleport(exoship)
    }

    fun callNewSystemPing() {
        var ping = CampaignPingSpec()
        ping.color = Color(248, 149, 44)
        ping.range = 15000f
        ping.minRange = exoship.radius
        ping.duration = 10f
        ping.alphaMult = 1f
        ping.width = 10f
        ping.num = 5
        ping.delay = 0.2f
        Global.getSector().addPing(exoship, ping)

        if (Global.getSector().playerFleet.containingLocation == exoship.containingLocation) {
            Global.getSoundPlayer().playUISound("ui_interdict_off", 0.75f, 0.25f)
            Global.getSoundPlayer().playUISound("ui_interdict_off", 1.25f, 0.25f)
        }
        // Global.getSoundPlayer().playSound("system_phase_teleporter", 1f, 0.5f, exoship.location, exoship.velocity)

    }

    fun callSystemPing() {
        var ping = CampaignPingSpec()
        ping.color = Color(248, 149, 44)
        ping.range = 6000f
        ping.minRange = exoship.radius
        ping.duration = 8f
        ping.alphaMult = 1f
        ping.width = 10f
        ping.num = 5
        ping.delay = 0.2f
        Global.getSector().addPing(exoship.starSystem.hyperspaceAnchor, ping)
    }

    fun arriveInSystem(amount: Float, level: Float) {
       // (AppDriver.getInstance().currentState as CampaignState).isHideUI = true

        var distance = MathUtils.getDistance(exoship.location, destination!!.location)
        exoship.velocity.set(Vector2f(previousVelocity.x * level, previousVelocity.y * level))

        if (level <= 0.01) {
            stopFlight()
        }
    }

    fun stopFlight() {
        var angle = Misc.getAngleInDegrees(exoship.location, destination!!.starSystem.center.location) + 180
        exoship.setCircularOrbit(destination!!.starSystem.center, angle, MathUtils.getDistance(exoship.location, destination!!.starSystem.center.location), 360f)
        data.state = ExoShipData.State.Idle

        exoship.removeTag(Tags.NON_CLICKABLE)


        data.daysBetweenMoves = MathUtils.getRandomNumberInRange(180f, 240f)
        data.lastMoveTimestamp = Global.getSector().clock.timestamp

        //Allow spawning new fleets
        remainingFleetBudget = maxFleets
    }

    fun spawnFleet() : CampaignFleetAPI? {
        var market = exoship.market

        var basePoints = MathUtils.getRandomNumberInRange(30f, 40f)

        var points = MathUtils.getRandomNumberInRange(54f, 172f)

        val random = Random()

        var type = FleetTypes.PATROL_SMALL
        if (points > 80) type = FleetTypes.PATROL_MEDIUM
        if (points > 140) type = FleetTypes.PATROL_LARGE

        val params = FleetParamsV3(null,
            exoship.locationInHyperspace,
            exoship.faction.id,
            4f,
            type,
            points,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )

        params.random = random
        params.maxShipSize = 3

        val fleet = FleetFactoryV3.createFleet(params) ?: return null
        val location = exoship.containingLocation
        location.addEntity(fleet)

        initExoProperties(random, fleet, false)

        fleet.setLocation(exoship.location.x, exoship.location.y)
        fleet.facing = random.nextFloat() * 360f
        fleet.addScript(RemnantAssignmentAI(fleet, exoship.containingLocation as StarSystemAPI, exoship))

        fleet.setFaction(market.factionId)

      /*  var assignments = WeightedRandomPicker<FleetAssignment>()
        assignments.add(FleetAssignment.PATROL_SYSTEM, 1f)
        assignments.add(FleetAssignment.ORBIT_PASSIVE, 1f)
        assignments.add(FleetAssignment.ORBIT_AGGRESSIVE, 1f)*/

        delayTimestamp = Global.getSector().clock.timestamp
        launchDelayDays = MathUtils.getRandomNumberInRange(0.25f, 0.75f)

        remainingFleetBudget -= 1

        fleet.memoryWithoutUpdate["\$sourceId"]
        fleet.addEventListener(this)

        data.fleets.add(fleet)
        return fleet
    }

    fun initExoProperties(random: Random?, fleet: CampaignFleetAPI, dormant: Boolean) {
        var random = random
        if (random == null) random = Random()

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_PATROL_FLEET] = true
       // fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT] = true
       // fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true
fleet.caresAboutPlayerTransponder()
        //RemnantSeededFleetManager.addRemnantInteractionConfig(fleet)
        val salvageSeed = random.nextLong()
        fleet.memoryWithoutUpdate[MemFlags.SALVAGE_SEED] = salvageSeed
    }

    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?, param: Any?) {
        if (reason != CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE) {

            remainingFleetBudget += 1
            remainingFleetBudget = MathUtils.clamp(remainingFleetBudget, 0, maxFleets)
        }
    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {

    }
}