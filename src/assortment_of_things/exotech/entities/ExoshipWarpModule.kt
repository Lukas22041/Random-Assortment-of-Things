package assortment_of_things.exotech.entities

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc

class ExoshipWarpModule(var exoship: ExoshipEntity, var exoshipEntity: SectorEntityToken) {

    var destinationEntity: SectorEntityToken? = null
    var destinationSystem: StarSystemAPI? = null

    fun getMovementModule() = exoship.movement

    fun advance(amount: Float) {

    }

    //warp to specific orbit
    fun warp(entity: SectorEntityToken) {
        warp(entity, entity.starSystem)
    }

    //Warps to starsystem, selects random orbit
    fun warp(starSystem: StarSystemAPI) {

    }

    private fun warp(entity: SectorEntityToken, starSystem: StarSystemAPI) {

    }

    private fun findParkingOrbit() {
        val minDist = 4000f
        val maxDist = 8000f
        parkingOrbit = null
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
            parkingOrbit = destination.createToken(loc)
            val orbitRadius = found.radius + 250f
            val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
            parkingOrbit.setCircularOrbit(found, Misc.random.nextFloat() * 360f, orbitRadius, orbitDays)
        } else {
            val gaps =
                BaseThemeGenerator.findGaps(destination.getCenter(), minDist, maxDist, gateHauler.getRadius() + 50f)
            if (!gaps.isEmpty()) {
                val gap = gaps[0]
                val orbitRadius = (gap.start + gap.end) * 0.5f
                val loc = Misc.getPointAtRadius(destination.getCenter().getLocation(), orbitRadius)
                parkingOrbit = destination.createToken(loc)
                if (!destination.isNebula()) {
                    val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
                    parkingOrbit.setCircularOrbit(destination.getCenter(),
                        Misc.random.nextFloat() * 360f,
                        orbitRadius,
                        orbitDays)
                }
            }
        }
        if (parkingOrbit == null) {
            val orbitRadius = minDist + (maxDist - minDist) * Misc.random.nextFloat()
            val loc = Misc.getPointAtRadius(destination.getCenter().getLocation(), orbitRadius)
            parkingOrbit = destination.createToken(loc)
            if (!destination.isNebula()) {
                val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
                parkingOrbit.setCircularOrbit(destination.getCenter(),
                    Misc.random.nextFloat() * 360f,
                    orbitRadius,
                    orbitDays)
            }
        }
        destination.addEntity(parkingOrbit)
    }

}