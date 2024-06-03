package assortment_of_things.exotech.entities

import assortment_of_things.campaign.scripts.render.RATCampaignRenderer
import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SoundAPI
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class ExoshipWarpModule(var exoship: ExoshipEntity, var exoshipEntity: SectorEntityToken) {

    enum class State {
        Inactive, Departure, Arrival
    }

    var destinationSystem: StarSystemAPI? = null

    var playerJoined = false
    var parkingOrbit: SectorEntityToken? = null
    var departureAngle: Float = 0f
    var state = State.Inactive

    @Transient var chargeupSound: SoundAPI? = null
    var startedPlayingSound = false

    var daysInArrival = 0f

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

        destinationSystem = starSystem

        parkingOrbit = orbit
        departureAngle = Misc.getAngleInDegrees(exoshipEntity.locationInHyperspace, starSystem.location)

        getMovementModule().movementUtil.acceleration = exoship.ACCELERATION
        getMovementModule().moveInDirection(departureAngle)
        getMovementModule().setFaceInOppositeDirection(false)
        getMovementModule().setTurnThenAccelerate(true)
        exoship.longBurn = true
        exoship.isInTransit = true

        exoshipEntity.fadeOutIndicator()
        exoshipEntity.addTag(Tags.NON_CLICKABLE)

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

        if (chargeupSound != null && chargeupSound!!.isPlaying) {
            startedPlayingSound = true
            chargeupSound!!.setLocation(exoshipEntity.location.x, exoshipEntity.location.y)
        }

        var velocity = getMovementModule().movementUtil.velocity.length()



        var everPlayedSoundAndDone = chargeupSound != null && !chargeupSound!!.isPlaying && startedPlayingSound

        //If the Chargeup Sound is done playing, or the exoship isnt in the player location, or the velocity is above 400, warp.
        if (everPlayedSoundAndDone || (exoshipEntity.containingLocation != Global.getSector().playerFleet.containingLocation && velocity >= 50f) || velocity >= 400) {

            //Spawn Large Flash
            RATCampaignRenderer.getFlashRenderer().spawnFlash(Vector2f(exoshipEntity.location), exoshipEntity.containingLocation,
                ExoUtils.color1, Color(130,4,189, 255), 1000f, 18000f, 0.15f, 1f, 4f)

            //Clear up Sounds
            chargeupSound?.stop()
            chargeupSound = null
            startedPlayingSound = false

            //Spawn a line of afterimages after the exoship to give a sense of it moving at lightspeed
            var steps = 100
            var distance = 33f
            var duration = 0f
            var alphaReduction = 75f / steps
            var alpha = 75f
            for (step in 0 until steps) {

                alpha -= alphaReduction
                alpha.coerceIn(0f, 1000f)
                duration += 0.15f

                var afterimageColor1 = Color(248,172,44, 75)
                var afterimageColor2 = Color(130,4,189, 0)
                var afterimageLoc = MathUtils.getPointOnCircumference(exoshipEntity.location, distance * step, exoshipEntity.facing)

                RATCampaignRenderer.getAfterimageRenderer().addAfterimage(CampaignEngineLayers.BELOW_STATIONS, exoshipEntity.containingLocation, exoshipEntity,
                    afterimageColor1.setAlpha(alpha.toInt()) ,afterimageColor2, duration, 0f, location = afterimageLoc)
            }




            //Transfer to new system
            exoshipEntity.containingLocation.removeEntity(exoshipEntity)

            initiateArrival()


            if (Global.getSector().playerFleet.containingLocation == exoshipEntity.containingLocation) {
                Global.getSoundPlayer().playSound("exoship_warp", 1f, 1f, exoshipEntity.location, exoshipEntity.velocity)
            }

        }
        //After it accelerated enough, activate Chargeup Sound
        else if (velocity >= 150f && chargeupSound == null) {

            if (Global.getSector().playerFleet.containingLocation == exoshipEntity.containingLocation) {
                chargeupSound = Global.getSoundPlayer().playSound("exoship_warp_chargeup", 1f, 1f, exoshipEntity.location, Vector2f())
            }

        }


    }

    fun initiateArrival() {

        //val brakeTime = exoship.MAX_SPEED / exoship.DECELERATION
        val brakeTime = exoship.movement.movementUtil.velocity.length() / exoship.DECELERATION
        val brakeDist = exoship.MAX_SPEED * 0.5f * brakeTime

        val spawnLoc = Misc.getUnitVectorAtDegreeAngle(departureAngle + 180f)
        val spawnVel = Vector2f(spawnLoc)

        spawnVel.scale(exoship.MAX_SPEED)
        spawnVel.negate()
        spawnLoc.scale(brakeDist * 1f + 4000f)
        Vector2f.add(spawnLoc, parkingOrbit!!.location, spawnLoc)

        exoshipEntity.setExpired(false)
        exoshipEntity.removeTag(Tags.NON_CLICKABLE)
        exoshipEntity.removeTag(Tags.FADING_OUT_AND_EXPIRING)
        exoshipEntity.setAlwaysUseSensorFaderBrightness(null)

        if (!destinationSystem!!.getAllEntities().contains(exoshipEntity)) {
            destinationSystem!!.addEntity(exoshipEntity)
        }

        exoshipEntity.fadeOutIndicator()

        getMovementModule().movementUtil.acceleration = exoship.DECELERATION
        getMovementModule().setLocation(spawnLoc)
        getMovementModule().setVelocity(spawnVel)
        getMovementModule().setFacing(departureAngle + 180f)

        getMovementModule().moveToLocation(parkingOrbit!!.location)
        getMovementModule().setTurnThenAccelerate(true)
        getMovementModule().setFaceInOppositeDirection(true)
        exoship.longBurn = true

        RATCampaignRenderer.getFlashRenderer().spawnFlash(Vector2f(spawnLoc), destinationSystem!!,
            ExoUtils.color1, Color(130,4,189, 255), 1000f, 18000f, 0.15f, 1f, 4f)

        //Spawn a line of afterimages after the exoship to give a sense of it moving at lightspeed
        var steps = 100
        var distance = 33f
        var duration = 0.15f * steps
        var alphaReduction = 75f / steps
        var alpha = 0f
        for (step in 0 until steps) {

            alpha += alphaReduction
            alpha.coerceIn(0f, 1000f)
            duration -= 0.15f

            var afterimageColor1 = Color(248,172,44, 75)
            var afterimageColor2 = Color(130,4,189, 0)
            var afterimageLoc = MathUtils.getPointOnCircumference(spawnLoc, distance * step, exoshipEntity.facing)

            RATCampaignRenderer.getAfterimageRenderer().addAfterimage(CampaignEngineLayers.BELOW_STATIONS, destinationSystem!!, exoshipEntity,
                afterimageColor1.setAlpha(alpha.toInt()) ,afterimageColor2, duration, 0f, location = afterimageLoc)
        }

        if (Global.getSector().playerFleet.containingLocation == exoshipEntity.containingLocation) {
            var angle = Misc.getAngleInDegrees(Global.getSector().playerFleet.location, spawnLoc)
            var soundLoc = MathUtils.getPointOnCircumference(Global.getSector().playerFleet.location, 500f, angle)
            Global.getSoundPlayer().playSound("exoship_warp", 1f, 1f, soundLoc, Vector2f())
        }

        state = State.Arrival
    }

    fun handleArrival(amount: Float) {

        var days = Misc.getDays(amount)
        daysInArrival += days

        getMovementModule().moveToLocation(parkingOrbit!!.location)
        val speed: Float = exoshipEntity.getVelocity().length()
        val dist = Misc.getDistance(parkingOrbit, exoshipEntity)

        val overshot = Misc.isInArc(exoshipEntity.getFacing(), 270f, exoshipEntity.getLocation(), parkingOrbit!!.location)
        if (overshot || dist < 700f) {
            getMovementModule().setTurnThenAccelerate(false)
            getMovementModule().setFaceInOppositeDirection(false)
        }
        var closeEnough = speed < 25f && dist < 100f + parkingOrbit!!.radius + exoshipEntity.getRadius()
        if (dist < 200f + parkingOrbit!!.radius + exoshipEntity.getRadius() && daysInArrival > 30f) {
            closeEnough = true
        }
        if (closeEnough) {

            destinationSystem = null
            exoshipEntity.fadeInIndicator()
            getMovementModule().setFaceInOppositeDirection(false)
            exoship.longBurn = false
            val orbitAngle = Misc.getAngleInDegrees(parkingOrbit!!.location, exoshipEntity.getLocation())
            val orbitDays = 1000000f
            exoshipEntity.setCircularOrbit(parkingOrbit, orbitAngle, dist, orbitDays)
            if (!exoshipEntity.isInCurrentLocation) {
                for (i in 0..9) {
                    exoship.engineGlow.showIdling()
                    exoship.engineGlow.advance(1f)
                }
            }
            exoship.isInTransit = false
            daysInArrival = 0f

            state = State.Inactive
        }
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