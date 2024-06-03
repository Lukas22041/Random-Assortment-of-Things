package assortment_of_things.exotech.entities

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class ExoshipWarpModule(var exoship: ExoshipEntity, var exoshipEntity: SectorEntityToken) {

    enum class State {
        Inactive, Departure, Arrival
    }

    var destinationEntity: SectorEntityToken? = null
    var destinationSystem: StarSystemAPI? = null

    var playerJoined = false
    var parkingOrbit: SectorEntityToken? = null
    var departureAngle: Float = 0f
    var state = State.Inactive

    fun getMovementModule() = exoship.movement


    //warp to specific entity to orbit
    fun warp(entity: SectorEntityToken) {

        val loc = MathUtils.getRandomPointOnCircumference(entity.location, entity.radius + 400f)
        var orbit = entity.containingLocation.createToken(Vector2f())

        val orbitRadius = entity.radius + 250f
        val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
        orbit.setCircularOrbit(entity, Misc.random.nextFloat() * 360f, orbitRadius, orbitDays)

        warp(orbit, entity.starSystem)
    }

    //Warps to starsystem, selects random orbit
    fun warp(starSystem: StarSystemAPI) {
        var orbit = findParkingOrbit(starSystem)
        warp(orbit, starSystem)
    }

    private fun warp(orbit: SectorEntityToken?, starSystem: StarSystemAPI?) {
        if (starSystem == null || starSystem === exoshipEntity.containingLocation || orbit == null) return
        if (state != State.Inactive) return

        if (parkingOrbit != null) {
            exoshipEntity.containingLocation.removeEntity(parkingOrbit)
            parkingOrbit = null
        }

        parkingOrbit = orbit
        departureAngle = Misc.getAngleInDegrees(exoshipEntity.locationInHyperspace, starSystem.location)

        getMovementModule().moveInDirection(45f)
        getMovementModule().setFaceInOppositeDirection(false)
        getMovementModule().setTurnThenAccelerate(true)
        exoship.longBurn = true
        exoship.isInTransit = true

        state = State.Departure
    }

    fun advance(amount: Float) {

        if (state == State.Departure) {
            handleDeparture(amount)
        }

        if (state == State.Arrival) {
            handleArrival(amount)
        }

    }

    fun handleDeparture(amount: Float) {

    }

    fun handleArrival(amount: Float) {

    }

    private fun findParkingOrbit(destination: StarSystemAPI) : SectorEntityToken {
        val minDist = 4000f
        val maxDist = 8000f
        var orbit: SectorEntityToken? = null
        var found: SectorEntityToken? = null
        for (curr in destination.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
            val dist = curr.location.length()
            if (dist >= minDist && dist <= 8000f) {
                found = curr
                break
            }
        }
        if (found == null) {
            for (curr in destination.getPlanets()) {
                if (curr.isMoon) continue
                val dist = curr.location.length()
                if (dist >= minDist && dist <= 8000f) {
                    found = curr
                    break
                }
            }
        }
        if (found != null) {
            val loc = Misc.getPointAtRadius(found.location, found.radius + 400f)
            orbit = destination.createToken(loc)
            val orbitRadius = found.radius + 250f
            val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
            orbit!!.setCircularOrbit(found, Misc.random.nextFloat() * 360f, orbitRadius, orbitDays)
        } else {
            val gaps =
                BaseThemeGenerator.findGaps(destination.getCenter(), minDist, maxDist, exoshipEntity.getRadius() + 50f)
            if (!gaps.isEmpty()) {
                val gap = gaps[0]
                val orbitRadius = (gap.start + gap.end) * 0.5f
                val loc = Misc.getPointAtRadius(destination.getCenter().getLocation(), orbitRadius)
                orbit = destination.createToken(loc)
                if (!destination.isNebula()) {
                    val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
                    orbit!!.setCircularOrbit(destination.getCenter(),
                        Misc.random.nextFloat() * 360f,
                        orbitRadius,
                        orbitDays)
                }
            }
        }
        if (orbit == null) {
            val orbitRadius = minDist + (maxDist - minDist) * Misc.random.nextFloat()
            val loc = Misc.getPointAtRadius(destination.getCenter().getLocation(), orbitRadius)
            orbit = destination.createToken(loc)
            if (!destination.isNebula()) {
                val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
                orbit!!.setCircularOrbit(destination.getCenter(),
                    Misc.random.nextFloat() * 360f,
                    orbitRadius,
                    orbitDays)
            }
        }
        destination.addEntity(orbit)
        return orbit!!
    }

}