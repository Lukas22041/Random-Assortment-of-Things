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
    var doNotHidePlayer = false
    var skipPreperation = false
    var parkingOrbit: SectorEntityToken? = null
    var departureAngle: Float = 0f
    var state = State.Inactive

    @Transient var chargeupSound: SoundAPI? = null
    var startedPlayingSound = false

    var daysInArrival = 0f

    fun getMovementModule() = exoship.movement

    var warpListener: (() -> Unit) = @JvmSerializableLambda {}

    var playWarpSoundNextFrame = false
    var framesTilWarpSound = 10

    //warp to specific entity to orbit
    fun warp(entity: SectorEntityToken, withPlayer: Boolean = false, doNotHidePlayer: Boolean = false, listener: () -> Unit = {}) {

        val loc = MathUtils.getRandomPointOnCircumference(entity.location, entity.radius + 400f)
        var orbit = entity.containingLocation.createToken(Vector2f())

        val orbitRadius = entity.radius + 250f
        val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f) + 20
        orbit.setCircularOrbit(entity, Misc.random.nextFloat() * 360f, orbitRadius, orbitDays)
        entity.containingLocation.addEntity(orbit)

        warp(orbit, entity.starSystem, withPlayer, doNotHidePlayer, false, listener)
    }

    //Warps to starsystem, selects random orbit
    fun warp(starSystem: StarSystemAPI, withPlayer: Boolean = false, doNotHidePlayer: Boolean = false, listener: () -> Unit = {}) {
        var orbit = findParkingOrbit(starSystem)
        warp(orbit, starSystem, withPlayer, doNotHidePlayer, false, listener)
    }

    fun doQuestlineWarp(entity: SectorEntityToken, listener: () -> Unit = {}) {

        val loc = MathUtils.getRandomPointOnCircumference(entity.location, entity.radius + 400f)
        var orbit = entity.containingLocation.createToken(Vector2f())

        val orbitRadius = entity.radius + 450f
        val orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f)
        orbit.setCircularOrbit(entity, Misc.random.nextFloat() * 360f, orbitRadius, orbitDays)
        entity.containingLocation.addEntity(orbit)

        warp(orbit, entity.starSystem, false, false, true, listener)
    }

    private fun warp(orbit: SectorEntityToken?, starSystem: StarSystemAPI?, withPlayer: Boolean, doNotHidePlayer: Boolean = false, skipPreperation: Boolean = false, listener: () -> Unit) {
        if (starSystem == null || starSystem == exoshipEntity.containingLocation || orbit == null) return
        if (state != State.Inactive) return

        if (parkingOrbit != null) {
            exoshipEntity.containingLocation.removeEntity(parkingOrbit)
            parkingOrbit = null
        }

        this.playerJoined = withPlayer
        this.doNotHidePlayer = doNotHidePlayer
        this.skipPreperation = skipPreperation
        warpListener = listener
        if (listener == null) warpListener = { }

        destinationSystem = starSystem

        parkingOrbit = orbit
        departureAngle = Misc.getAngleInDegrees(exoshipEntity.locationInHyperspace, starSystem.location)

        if (skipPreperation) {
            exoshipEntity.facing = departureAngle
        }

        var extraAcceleration = 0
        if (skipPreperation) {
            extraAcceleration = 20
        }

        getMovementModule().movementUtil.acceleration = exoship.ACCELERATION + extraAcceleration
        getMovementModule().moveInDirection(departureAngle)
        getMovementModule().setFaceInOppositeDirection(false)
        getMovementModule().setTurnThenAccelerate(true)
        exoship.longBurn = true
        exoship.isInTransit = true

        exoshipEntity.fadeOutIndicator()
        exoshipEntity.addTag(Tags.NON_CLICKABLE)

        //Otherwise the exoship can still be interacted with if started from the ability
        if (playerJoined) {
            Global.getSector().campaignUI.clearLaidInCourse()
        }


        if (playerJoined) {
            fixateViewportAndFadeStars()

            if (!doNotHidePlayer) {
                Global.getSector().playerFleet.setLocation(300000f, 300000f)
            }
        }

        state = State.Departure
    }

    fun fixateViewportAndFadeStars() {
        var viewport = Global.getSector().viewport

        var zoom = Global.getSector().campaignUI.zoomFactor

        var width = Global.getSettings().screenWidth * zoom
        var height = Global.getSettings().screenHeight * zoom

        var x = exoshipEntity.location.x - width / 2
        var y = exoshipEntity.location.y - height / 2

        viewport.isExternalControl = true
        viewport.set(x, y, width, height)

        exoshipEntity.containingLocation.backgroundParticleColorShifter.shift(this, Color(0, 0, 0, 0), 0.2f, 5f, 1f)
    }

    fun advance(amount: Float) {

        if (playerJoined && exoship.isInTransit) {
            var ability = Global.getSector().playerFleet.getAbility("fracture_jump")
            if (ability != null && ability.cooldownLeft <= 0.5f) {
                ability.cooldownLeft = 0.5f
            }

        }

        if (playWarpSoundNextFrame) {

            framesTilWarpSound -= 1
            if (framesTilWarpSound <= 0) {
                playWarpSoundNextFrame = false
                Global.getSoundPlayer().playSound("exoship_warp", 1f, 1f, exoshipEntity.location, Vector2f())
                framesTilWarpSound = 10
            }



        }

        if (doNotHidePlayer) {
            Global.getSector().playerFleet.setLocation(exoshipEntity.location.x, exoshipEntity.location.y)
            Global.getSector().playerFleet.facing = exoshipEntity.facing
            Global.getSector().playerFleet.stats.addTemporaryModMult(0.1f, "rat_exoship_warp", "Warp", 0f, Global.getSector().playerFleet.stats.fleetwideMaxBurnMod)
        }

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

            if (Global.getSector().playerFleet.containingLocation == exoshipEntity.containingLocation && !playerJoined) {
                Global.getSoundPlayer().playSound("exoship_warp", 1f, 1f, exoshipEntity.location, exoshipEntity.velocity)
            }

            //Transfer to new system
            exoshipEntity.containingLocation.removeEntity(exoshipEntity)

            initiateArrival()




        }
        //After it accelerated enough, activate Chargeup Sound
        else if (velocity >= 150f && chargeupSound == null) {

            if (Global.getSector().playerFleet.containingLocation == exoshipEntity.containingLocation) {
                chargeupSound = Global.getSoundPlayer().playSound("exoship_warp_chargeup", 1f, 1f, exoshipEntity.location, Vector2f())
            }

        }

        if (playerJoined) {
            fixateViewportAndFadeStars()
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
        spawnLoc.scale(brakeDist * 1f + 6000f)
        Vector2f.add(spawnLoc, parkingOrbit!!.location, spawnLoc)

        exoshipEntity.setExpired(false)
       /* exoshipEntity.removeTag(Tags.NON_CLICKABLE)
        exoshipEntity.removeTag(Tags.FADING_OUT_AND_EXPIRING)
        exoshipEntity.setAlwaysUseSensorFaderBrightness(null)*/

        if (!destinationSystem!!.getAllEntities().contains(exoshipEntity)) {
            destinationSystem!!.addEntity(exoshipEntity)
        }

        exoshipEntity.fadeOutIndicator()

        getMovementModule().movementUtil.acceleration = exoship.DECELERATION
        getMovementModule().setLocation(spawnLoc)
        getMovementModule().setVelocity(spawnVel)
        getMovementModule().setFacing(departureAngle )

        getMovementModule().moveToLocation(parkingOrbit!!.location)
        getMovementModule().setTurnThenAccelerate(false)
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
            var afterimageLoc = MathUtils.getPointOnCircumference(spawnLoc, distance * step, exoshipEntity.facing + 180)

            RATCampaignRenderer.getAfterimageRenderer().addAfterimage(CampaignEngineLayers.BELOW_STATIONS, destinationSystem!!, exoshipEntity,
                afterimageColor1.setAlpha(alpha.toInt()) ,afterimageColor2, duration, 0f, location = afterimageLoc)
        }

        if (Global.getSector().playerFleet.containingLocation == exoshipEntity.containingLocation && !playerJoined) {
            var angle = Misc.getAngleInDegrees(Global.getSector().playerFleet.location, spawnLoc)
            var soundLoc = MathUtils.getPointOnCircumference(Global.getSector().playerFleet.location, 500f, angle)
            Global.getSoundPlayer().playSound("exoship_warp", 1f, 1f, soundLoc, Vector2f())
        }

        if (playerJoined) {
            fixateViewportAndFadeStars()

            var playerFleet = Global.getSector().playerFleet
            var currentLocation = playerFleet.containingLocation
            var targetSystem = exoshipEntity.containingLocation

            currentLocation.removeEntity(playerFleet)
            targetSystem.addEntity(playerFleet)
            Global.getSector().setCurrentLocation(targetSystem)
           // playerFleet.setLocation(exoshipEntity.location.x, entity.location.y)

           // CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

            playWarpSoundNextFrame = true


        }

        state = State.Arrival
    }

    fun handleArrival(amount: Float) {

        var days = Misc.getDays(amount)
        daysInArrival += days

        getMovementModule().moveToLocation(parkingOrbit!!.location)
        val speed: Float = exoshipEntity.getVelocity().length()
        val dist = Misc.getDistance(parkingOrbit, exoshipEntity)

        /*val overshot = Misc.isInArc(exoshipEntity.getFacing(), 270f, exoshipEntity.getLocation(), parkingOrbit!!.location)
        if (overshot || dist < 700f) {
            getMovementModule().setTurnThenAccelerate(false)
            getMovementModule().setFaceInOppositeDirection(false)
        }*/
        var closeEnough = speed < 30f && dist < 100f + parkingOrbit!!.radius + exoshipEntity.getRadius()
        if (dist < 200f + parkingOrbit!!.radius + exoshipEntity.getRadius() && daysInArrival > 7f) {
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

            exoshipEntity.removeTag(Tags.NON_CLICKABLE)
            exoshipEntity.removeTag(Tags.FADING_OUT_AND_EXPIRING)
            exoshipEntity.setAlwaysUseSensorFaderBrightness(null)

            if (playerJoined) {
                Global.getSector().playerFleet.setLocation(exoshipEntity.location.x, exoshipEntity.location.y)
                Global.getSector().playerFleet.setMoveDestination(exoshipEntity.location.x, exoshipEntity.location.y)
                Global.getSector().viewport.isExternalControl = false
            }


            if (warpListener != null) warpListener!!()

            doNotHidePlayer = false
            skipPreperation = false

            warpListener = {  }


        }

        else if (playerJoined) {
            fixateViewportAndFadeStars()
        }
    }

    fun dealDamageInRangeOfFlash() {
        Global.getSector().campaignUI.addMessage("Your fleet took light damage from being hit by the exhaust of a warpdrive.")
    }

    private fun findParkingOrbit(destination: StarSystemAPI) : SectorEntityToken {
        val minDist = 4000f
        val maxDist = 8000f
        var orbit: SectorEntityToken? = null
        var found: SectorEntityToken? = null
       /* for (curr in destination.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
            val dist = curr.location.length()
            if (dist >= minDist && dist <= 8000f) {
                found = curr
                break
            }
        }*/
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