package assortment_of_things.exotech

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.state.AppDriver
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.util.*

class ExoshipStateScript(var exoship: SectorEntityToken) : EveryFrameScript, FleetEventListener {

    var data = ExoUtils.getExoshipData(exoship)

    var destination: SectorEntityToken? = null

    var departureTimestamp = Global.getSector().clock.timestamp
    var daysSinceDeparture = 0f

    var previousVelocity = Vector2f()
    var daysForTransfer = 3.5f

    var maxFleets = 4
    var remainingFleetBudget = maxFleets
    var launchDelayDays = 0.25f
    var delayTimestamp = Global.getSector().clock.timestamp

    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }


    override fun advance(amount: Float) {

        daysSinceDeparture = Global.getSector().clock.getElapsedDaysSince(departureTimestamp)

        var daysTilMove = data.getTimeTilNextMove()

        var maxDist = 2
        val distFromSource = Misc.getDistanceLY(Global.getSector().playerFleet.getLocationInHyperspace(), exoship.locationInHyperspace)

        for (fleet in ArrayList(data.fleets)) {
            if (fleet.isDespawning || fleet.isExpired ) {
                data.fleets.remove(fleet)
            }
            if (distFromSource >= maxDist && Global.getSector().playerFleet.containingLocation != fleet.containingLocation) {
                fleet.despawn()
            }
        }

        if (daysTilMove <= 20) {
            for (fleet in data.fleets) {
                if (fleet.currentAssignment?.target != exoship && data.state == ExoShipData.State.Idle  && fleet.containingLocation == exoship.containingLocation) {
                    fleet.clearAssignments()
                    fleet.removeScriptsOfClass(RemnantAssignmentAI::class.java)
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, exoship, 999999f, "Returning to Exoship")
                }
                if (fleet.currentAssignment?.target == exoship && (fleet.containingLocation != exoship.containingLocation || data.state != ExoShipData.State.Idle)) {
                    fleet.clearAssignments()
                }
            }
        }
        else if (remainingFleetBudget != 0){
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
            departureTimestamp = Global.getSector().clock.timestamp
            daysSinceDeparture = Global.getSector().clock.getElapsedDaysSince(departureTimestamp)
        }
        if (data.state == ExoShipData.State.Travelling) {
            travelToSystem(amount)
        }
        if (data.state == ExoShipData.State.Arriving) {
            arriveInSystem(amount)
        }
    }

    fun findSystemToMoveTo() : StarSystemAPI {
        var systems = Global.getSector().starSystems.filter {!it.hasTag(
            Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag( Tags.THEME_HIDDEN)}

        var system = systems.random()


        return system

    }

    fun travelToSystem(amount: Float) {
      //  (AppDriver.getInstance().currentState as CampaignState).isHideUI = true

       /* var playerFleet = Global.getSector().playerFleet*/

        var startLevel = (daysSinceDeparture - 0) / (daysForTransfer * 0.5f - 0)
        startLevel = MathUtils.clamp(startLevel, 0f, 1f)

        var direction = MathUtils.getPointOnCircumference(Vector2f(0f, 0f), 300 * amount * startLevel, exoship.facing)
        exoship.velocity.set(exoship.velocity.plus(direction))

    /*    playerFleet.setLocation(exoship.location.x, exoship.location.y)
        playerFleet.setVelocity(0f, 0f)

        (playerFleet as CampaignFleet).setInJumpTransition(true)
        playerFleet.setNoEngaging(5f)

        playerFleet.stats.addTemporaryModMult(0.05f, "", "", 0f, playerFleet.stats.fleetwideMaxBurnMod)*/

        if (daysSinceDeparture > daysForTransfer * 0.4f) {

            data.state = ExoShipData.State.Arriving
            //CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

            var destinationSystem = destination!!.containingLocation
            var currentLocation = exoship.containingLocation

            currentLocation.removeEntity(exoship)
            destinationSystem.addEntity(exoship)

            var point = MathUtils.getPointOnCircumference(destination!!.location, 10000f, exoship.facing + 180)
            exoship.location.set(point)
            var angle = Misc.getAngleInDegrees(destination!!.location, exoship.location) + 90
           // var pointInOrbit = MathUtils.getPointOnCircumference(destination!!.location, destination!!.radius + 200f, angle)
            var destinationPoint = MathUtils.getPointOnCircumference(destination!!.location, 10000f, exoship.facing + 180)
            exoship.location.set(destinationPoint)

            //finalDestination = pointInOrbit
            previousVelocity = Vector2f(exoship.velocity)

           /* currentLocation.removeEntity(playerFleet)
            destinationSystem.addEntity(playerFleet)
            Global.getSector().setCurrentLocation(destinationSystem)
            playerFleet.location.set(destinationPoint)*/
        }
    }

    fun arriveInSystem(amount: Float) {
       // (AppDriver.getInstance().currentState as CampaignState).isHideUI = true


      /*  var playerFleet = Global.getSector().playerFleet

        (playerFleet as CampaignFleet).setInJumpTransition(true)*/


        var distance = MathUtils.getDistance(exoship.location, destination!!.location)
        var level = (distance - 0) / (10000f - 0)
        level = MathUtils.clamp(level, 0f, 1f)

        var overwriteLevel = (daysSinceDeparture - 0) / (daysForTransfer - 0)
        overwriteLevel = 1 - overwriteLevel
        overwriteLevel = MathUtils.clamp(overwriteLevel, 0f, 1f)

        if (overwriteLevel <= level) {
            level = overwriteLevel
        }

        exoship.velocity.set(Vector2f(previousVelocity.x * level, previousVelocity.y * level))
       /* playerFleet.setLocation(exoship.location.x, exoship.location.y)
        playerFleet.setVelocity(0f, 0f)
        playerFleet.stats.addTemporaryModMult(0.05f, "", "", 0f, playerFleet.stats.fleetwideMaxBurnMod)


        playerFleet.setNoEngaging(5f)*/


        if (level <= 0.02) {
            var angle = Misc.getAngleInDegrees(exoship.location, destination!!.starSystem.center.location) + 180
            exoship.setCircularOrbit(destination!!.starSystem.center, angle, MathUtils.getDistance(exoship.location, destination!!.starSystem.center.location), 360f)
            data.state = ExoShipData.State.Idle

            /*(playerFleet as CampaignFleet).setInJumpTransition(false)

            playerFleet.setLocation(exoship.location.x, exoship.location.y)
            playerFleet.setVelocity(0f, 0f)*/

            (AppDriver.getInstance().currentState as CampaignState).isHideUI = false

            exoship.removeTag(Tags.NON_CLICKABLE)

           /* playerFleet.setMoveDestinationOverride(exoship.location.x, exoship.location.y)
            playerFleet.setNoEngaging(1.0f)*/


            data.daysBetweenMoves = MathUtils.getRandomNumberInRange(180f, 240f)
            data.lastMoveTimestamp = Global.getSector().clock.timestamp

            //Allow spawning new fleets
            remainingFleetBudget = maxFleets
        }
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