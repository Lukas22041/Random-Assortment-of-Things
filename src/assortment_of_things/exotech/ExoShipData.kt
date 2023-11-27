package assortment_of_things.exotech

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import org.lazywizard.lazylib.MathUtils

class ExoShipData {

    enum class State {
        Idle, Travelling, Arriving
    }

    var state = State.Idle
    var stateLevel = 0f

    var name: String = ""
    var cargo: CargoAPI = Global.getFactory().createCargo(true)

    var lastMoveTimestamp = Global.getSector().clock.timestamp
    var daysBetweenMoves = MathUtils.getRandomNumberInRange(90f, 160f)
    var moveLevel: Float = 0f

    var fleets = ArrayList<CampaignFleetAPI>()
    var stateScript: ExoshipStateScript? = null

    fun getTimeTilNextMove() : Float {
        var daysSince = Global.getSector().clock.getElapsedDaysSince(lastMoveTimestamp)
        var days = daysBetweenMoves - daysSince
        days = MathUtils.clamp(days, 0f, 1000f)
        return days
    }



}