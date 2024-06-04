package assortment_of_things.exotech.entities

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI

class ExoshipPlayerModule {

    var selectedDestination: SectorEntityToken? = null
    var playerJoinsWarp = false

    var isPlayerOwned = true

    var fuel = 0f
    var fuelProductionMult = 0

}