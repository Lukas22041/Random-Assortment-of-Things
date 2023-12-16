package assortment_of_things.frontiers.data

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.frontiers.plugins.facilities.BaseSettlementFacility
import assortment_of_things.misc.RATSettings
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

    private var facilityPlugin: BaseSettlementFacility? = null

    fun getPlugin() : BaseSettlementFacility? {
        if (facilityID == "") return null
        if (facilityPlugin == null) {
           updatePlugin()
        }
        return facilityPlugin
    }

    fun isFunctional() : Boolean {
        return !isBuilding && facilityID != ""
    }


    fun updatePlugin() {
        facilityPlugin = FrontiersUtils.getFacilityPlugin(FrontiersUtils.getFacilityByID(facilityID))
        facilityPlugin?.facilitySlot = this
        facilityPlugin?.settlement = data
    }

    fun getFacilitySpec() : SettlementFacilitySpec? {
        if (facilityID == "") return null
        return FrontiersUtils.getFacilityByID(facilityID)
    }

    fun installNewFacility(id: String) {

        removeCurrentFacility()
        facilityID = id
        updatePlugin()

        isBuilding = true
        facilityPlugin?.setBuilding(true)

        buildingTimestamp = Global.getSector().clock.timestamp

        updateDays()

        if (Global.getSettings().isDevMode) {
            finishConstruction()
        }
    }

    fun finishConstruction() {
        isBuilding = false
        facilityPlugin?.setBuilding(false)
        daysRemaining = 0f

        var plugin = getPlugin()
        plugin!!.onBuildingFinished()
        plugin!!.apply()

        Global.getSector().campaignUI.addMessage(object : BaseIntelPlugin() {
            override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {
                info!!.addPara( "Finished constructing the \"${plugin.getName()}\" facility in the settlement on ${data.primaryPlanet.name}.",
                    0f, Misc.getTextColor(), Misc.getHighlightColor(), "${plugin.getName()}")
            }

            override fun getIcon(): String {
                return plugin.getIcon()
            }
        })
    }

    fun updateDays() {
        var plugin = getPlugin() ?: return
        var daysRequired = plugin.getBuildTime()
        daysRemaining = daysRequired - Global.getSector().clock.getElapsedDaysSince(buildingTimestamp)
        daysRemaining = MathUtils.clamp(daysRemaining, 0f, plugin.getBuildTime().toFloat())
    }

    fun removeCurrentFacility() {
        if (facilityID == "") return

        var plugin = getPlugin()

        if (isBuilding) {
            var cost = plugin!!.getCost() * RATSettings.frontiersCostMult!!

            Global.getSector().playerFleet.cargo.credits.add(cost)
            var formated = Misc.getDGSCredits(cost)
            Global.getSector().campaignUI.messageDisplay.addMessage("Refunded $formated credits", Misc.getBasePlayerColor(), "$formated", Misc.getHighlightColor())
        }
        else {
            plugin!!.unapply()
        }

        isBuilding = false

        facilityID = ""
    }

}