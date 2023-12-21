package assortment_of_things.frontiers

import assortment_of_things.frontiers.intel.SettlementIntel
import assortment_of_things.frontiers.plugins.modifiers.BaseSettlementModifier
import assortment_of_things.frontiers.data.SettlementFacilitySlot
import assortment_of_things.frontiers.data.SettlementStats
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import org.lwjgl.util.vector.Vector2f

class SettlementData(var primaryPlanet: PlanetAPI, var settlementEntity: SectorEntityToken) {

    data class ProductionData(var cargo: CargoAPI, var timestamp: Long, var days: Float)

    var name = ""
    var description = "You take a trip down towards the surface, as your dropship closes on to the ground, the settlement starts becoming visible on the ground below. " +
            "At first it appears as a tiny, unidentifeable dot, until just moments later this blob has turned in to an outpost spanning the visible surroundings. " +
            "\n\nThe moment you leave your pod, you are welcomed to a humble but busy enviroment, with settlers making sure that operations can keep running on schedule, and the occasional launch and landings of tradeships from the landing pad."

    var stats = SettlementStats()
    var modifiers = ArrayList<BaseSettlementModifier>()
    var location = Vector2f()
    var angleFromCenter = 0f
    var distanceFromCenteer = 0f
    var facilitySlots = ArrayList<SettlementFacilitySlot>()
    var previousMonthsProduction = Global.getFactory().createCargo(true).apply {
        initMothballedShips(Factions.PLAYER)
    }
    var nextMonthsProduction = Global.getFactory().createCargo(true).apply {
        initMothballedShips(Factions.PLAYER)
    }
    lateinit var mananger: SettlementManager
    lateinit var intel: SettlementIntel
    var productionOrders = ArrayList<ProductionData>()

    var currentProductionBudget = 0f

    var autoDescend = false

    fun isAvailable() : Boolean {
        return !primaryPlanet.faction.isHostileTo(Factions.PLAYER)
    }

    fun getFunctionalSlots() : List<SettlementFacilitySlot> {
        return facilitySlots.filter { it.isFunctional() }
    }

    fun hasFacility(id: String) : Boolean {
        return facilitySlots.any { it.facilityID == id && it.isFunctional()  }
    }

}