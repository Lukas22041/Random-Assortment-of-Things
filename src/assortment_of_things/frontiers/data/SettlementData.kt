package assortment_of_things.frontiers.data

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.lwjgl.util.vector.Vector2f

class SettlementData(var primaryPlanet: PlanetAPI, var delegateEntity: SectorEntityToken) {

    var name = ""
    var description = ""
    var settlementStats = SettlementStats()
    var modifiers = ArrayList<String>()
    var location = Vector2f()

}