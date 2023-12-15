package assortment_of_things.frontiers.data

import assortment_of_things.frontiers.plugins.modifiers.BaseSettlementModifier
import assortment_of_things.frontiers.scripts.SettlementManager
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.lwjgl.util.vector.Vector2f

class SettlementData(var primaryPlanet: PlanetAPI, var delegateEntity: SectorEntityToken) {

    var name = ""
    var description = ""
    var stats = SettlementStats()
    var modifiers = ArrayList<BaseSettlementModifier>()
    var location = Vector2f()
    var angleFromCenter = 0f
    var distanceFromCenteer = 0f
    var facilitySlots = ArrayList<SettlementFacilitySlot>()
    lateinit var mananger: SettlementManager

}