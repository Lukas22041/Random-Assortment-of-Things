package assortment_of_things.exotech

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import org.lazywizard.lazylib.MathUtils

class ExoShipData {

    var name: String = ""
    var cargo: CargoAPI = Global.getFactory().createCargo(true)

    var lastMoveTimestamp = Global.getSector().clock.timestamp
    var daysBetweenMoves = MathUtils.getRandomNumberInRange(90f, 160f)

    var fleets = ArrayList<CampaignFleetAPI>()

    fun getTimeTilNextMove() : Float {
        var daysSince = Global.getSector().clock.getElapsedDaysSince(lastMoveTimestamp)
        var days = daysBetweenMoves - daysSince
        days = MathUtils.clamp(days, 0f, 1000f)
        return days
    }



}