package assortment_of_things.exotech.interactions.exoship

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.fleet.CampaignFleet
import com.fs.state.AppDriver
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

class ExoshipMoveScript(var exoship: SectorEntityToken, var destination: SectorEntityToken) : EveryFrameScript {

    enum class ExoshipState {
        Travelling, Exospace, Arriving, Arrived
    }

    var state = ExoshipState.Travelling
    var startingPoint = Vector2f()
    var timestamp = Global.getSector().clock.timestamp
    var daysSince = 0f
    var finalDestination = Vector2f()
    var previousVelocity = Vector2f()
    var maxLevel = 1f


    init {
        startingPoint = exoship.location
    }

    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }


    override fun advance(amount: Float) {
        daysSince = Global.getSector().clock.getElapsedDaysSince(timestamp)

        exoship.memoryWithoutUpdate.set("\$exoship_state", state)
        if (state == ExoshipState.Travelling) {
            travelling(amount)
        }
        if (state == ExoshipState.Exospace) {
            exospace(amount)
        }
        if (state == ExoshipState.Arriving) {
            arriving(amount)
        }
    }

    fun travelling(amount: Float) {
        (AppDriver.getInstance().currentState as CampaignState).isHideUI = true


        var playerFleet = Global.getSector().playerFleet

        var direction = MathUtils.getPointOnCircumference(Vector2f(0f, 0f), 500 * amount, exoship.facing)
        exoship.velocity.set(exoship.velocity.plus(direction))

        playerFleet.setLocation(exoship.location.x, exoship.location.y)
        playerFleet.setVelocity(0f, 0f)

        (playerFleet as CampaignFleet).setInJumpTransition(true)
        playerFleet.setNoEngaging(5f)

        playerFleet.stats.addTemporaryModMult(0.05f, "", "", 0f, playerFleet.stats.fleetwideMaxBurnMod)

        if (daysSince > 0.75) {
            state = ExoshipState.Exospace
            CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

            var destinationSystem = Global.getSector().getStarSystem("Exospace")
            var currentLocation = playerFleet.containingLocation

            currentLocation.removeEntity(exoship)
            destinationSystem.addEntity(exoship)
            exoship.setLocation(0f, 0f)

            previousVelocity = Vector2f(exoship.velocity)

            currentLocation.removeEntity(playerFleet)
            destinationSystem.addEntity(playerFleet)
            Global.getSector().setCurrentLocation(destinationSystem)
            playerFleet.location.set(exoship.location)

        }
    }


    fun exospace(amount: Float) {
        (AppDriver.getInstance().currentState as CampaignState).isHideUI = true

        var playerFleet = Global.getSector().playerFleet

        playerFleet.setLocation(exoship.location.x, exoship.location.y)
        playerFleet.setVelocity(0f, 0f)

        (playerFleet as CampaignFleet).setInJumpTransition(true)
        playerFleet.setNoEngaging(5f)

        playerFleet.stats.addTemporaryModMult(0.05f, "", "", 0f, playerFleet.stats.fleetwideMaxBurnMod)

        if (daysSince > 1.5) {
            state = ExoshipState.Arriving
            CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

            var destinationSystem = destination.containingLocation
            var currentLocation = playerFleet.containingLocation

            currentLocation.removeEntity(exoship)
            destinationSystem.addEntity(exoship)

            var point = MathUtils.getPointOnCircumference(destination.location, 20000f, exoship.facing + 180)
            exoship.location.set(point)
            var angle = Misc.getAngleInDegrees(destination.location, exoship.location) + 90
            var pointInOrbit = MathUtils.getPointOnCircumference(destination.location, destination.radius + 200f, angle)
            var destinationPoint = MathUtils.getPointOnCircumference(pointInOrbit, 20000f, exoship.facing + 180)
            exoship.location.set(destinationPoint)

            finalDestination = pointInOrbit
            previousVelocity = Vector2f(exoship.velocity)

            currentLocation.removeEntity(playerFleet)
            destinationSystem.addEntity(playerFleet)
            Global.getSector().setCurrentLocation(destinationSystem)
            playerFleet.location.set(destinationPoint)

        }
    }

    fun arriving(amount: Float) {
        (AppDriver.getInstance().currentState as CampaignState).isHideUI = true


        var playerFleet = Global.getSector().playerFleet

        (playerFleet as CampaignFleet).setInJumpTransition(true)

        var distance = MathUtils.getDistance(exoship.location, finalDestination)
        var level = (distance - 0) / (20000f - 0)
        level = MathUtils.clamp(level, 0f, 1f)

        exoship.velocity.set(Vector2f(previousVelocity.x * level, previousVelocity.y * level))
        playerFleet.setLocation(exoship.location.x, exoship.location.y)
        playerFleet.setVelocity(0f, 0f)
        playerFleet.stats.addTemporaryModMult(0.05f, "", "", 0f, playerFleet.stats.fleetwideMaxBurnMod)

        if (level > maxLevel) {
            level = maxLevel
        } else {
            maxLevel = level
        }

        playerFleet.setNoEngaging(5f)


        if (level <= 0.02 || daysSince > 4) {

            var angle = Misc.getAngleInDegrees(exoship.location, destination.location) + 180
            exoship.setCircularOrbit(destination, angle, MathUtils.getDistance(exoship.location, destination.location), 120f)
            state = ExoshipState.Arrived
            exoship.memoryWithoutUpdate.set("\$exoship_state", state)

            (playerFleet as CampaignFleet).setInJumpTransition(false)

            playerFleet.setLocation(exoship.location.x, exoship.location.y)
            playerFleet.setVelocity(0f, 0f)

            (AppDriver.getInstance().currentState as CampaignState).isHideUI = false

            exoship.removeTag(Tags.NON_CLICKABLE)
            Global.getSector().removeScript(this)

            playerFleet.setMoveDestinationOverride(exoship.location.x, exoship.location.y)
            playerFleet.setNoEngaging(1.0f)

        }
    }
}