package assortment_of_things.frontiers.data

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.plugins.facilities.BaseSettlementFacility
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class SettlementFacilitySlot(var data: SettlementData) {

    var facilityID: String = ""
    var isBuilding = false
    var buildingTimestamp = Global.getSector().clock.timestamp
    var daysRemaining = 0f

    @Transient
    private var temporaryPlugin: BaseSettlementFacility? = null

    fun getFacilityPlugin() : BaseSettlementFacility? {
        if (facilityID == "") return null
        if (temporaryPlugin == null) {
           updatePlugin()
        }
        return temporaryPlugin
    }

    fun updatePlugin() {
        temporaryPlugin = FrontiersUtils.getFacilityPlugin(FrontiersUtils.getFacilityByID(facilityID))
    }

    fun getFacilitySpecn() : SettlementFacilitySpec? {
        if (facilityID == "") return null
        return FrontiersUtils.getFacilityByID(facilityID)
    }

    fun installNewFacility(id: String) {



        removeCurrentFacility()
        facilityID = id
        updatePlugin()

        isBuilding = true
        buildingTimestamp = Global.getSector().clock.timestamp
        updateDays()
    }

    fun finishConstruction() {
        isBuilding = false
        daysRemaining = 0f

        var plugin = getFacilityPlugin()
        plugin!!.apply(data)

        Global.getSector().campaignUI.addMessage(object : BaseIntelPlugin() {
            override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {
                info!!.addPara( "Finished constructing the \"${plugin.getName()}\" facility in the settlement",
                    0f, Misc.getTextColor(), Misc.getHighlightColor(), "${plugin.getName()}")
            }

            override fun getIcon(): String {
                return plugin.getIcon()
            }
        })
    }

    fun updateDays() {
        var plugin = getFacilityPlugin() ?: return
        var daysRequired = plugin.getBuildTime(data)
        if (Global.getSettings().isDevMode) daysRequired = 5
        daysRemaining = daysRequired - Global.getSector().clock.getElapsedDaysSince(buildingTimestamp)
        daysRemaining = MathUtils.clamp(daysRemaining, 0f, plugin.getBuildTime(data).toFloat())
    }

    fun removeCurrentFacility() {
        if (facilityID == "") return

        var plugin = getFacilityPlugin()

        if (isBuilding) {
            var cost = plugin!!.getCost(data)

            Global.getSector().playerFleet.cargo.credits.add(cost)
            var formated = Misc.getDGSCredits(cost)
            Global.getSector().campaignUI.messageDisplay.addMessage("Refunded $formated credits", Misc.getBasePlayerColor(), "$formated", Misc.getHighlightColor())
        }

        isBuilding = false

        plugin!!.unapply(data)
        facilityID = ""
    }

}