package assortment_of_things.frontiers.data

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.plugins.facilities.BaseSettlementFacility

class SettlementFacilitySlot(var data: SettlementData) {

    var facilityID: String = ""

    fun getFacilityPlugin() : BaseSettlementFacility? {
        if (facilityID == "") return null
        return FrontiersUtils.getFacilityPlugin(FrontiersUtils.getFacilityByID(facilityID))
    }

    fun getFacilitySpecn() : SettlementFacilitySpec? {
        if (facilityID == "") return null
        return FrontiersUtils.getFacilityByID(facilityID)
    }

    fun installNewFacility(id: String) {
        removeCurrentFacility()
        facilityID = id

        var plugin = FrontiersUtils.getFacilityPlugin(FrontiersUtils.getFacilityByID(facilityID))
        plugin.apply(data)
    }

    fun removeCurrentFacility() {
        if (facilityID == "") return
        var plugin = FrontiersUtils.getFacilityPlugin(FrontiersUtils.getFacilityByID(facilityID))
        plugin.unapply(data)
        facilityID = ""
    }

}