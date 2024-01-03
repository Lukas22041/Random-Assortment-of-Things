package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.SettlementData
import assortment_of_things.frontiers.data.SettlementFacilitySlot
import assortment_of_things.frontiers.data.SettlementFacilitySpec
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseSettlementFacility() {

    var specId: String = ""
    lateinit var facilitySlot: SettlementFacilitySlot
    lateinit var settlement: SettlementData
    private var isBuilding = true

    fun getSpec() : SettlementFacilitySpec {
        return FrontiersUtils.getFacilityByID(specId)
    }

    fun getID() : String {
        return getSpec().id
    }

    open fun getName() : String {
        return getSpec().name
    }

    open fun getShortDesc() : String {
        return getSpec().shortDesc
    }

    open fun getIcon(): String {
        return getSpec().icon
    }

    open fun canBeBuild() : Boolean {
        return true
    }

    open fun shouldShowInPicker() : Boolean {
        return true
    }

    open fun canNotBeBuildReason(tooltip: TooltipMakerAPI, data: SettlementData) {

    }

    open fun getCost() : Float {
        return getSpec().cost
    }

    open fun getBuildTime() : Int {
        return getSpec().buildTime
    }



    abstract fun addDescriptionToTooltip(tooltip: TooltipMakerAPI)

    open fun apply()  {

    }

    open fun onBuildingFinished() {

    }

    open fun unapply() {

    }

    open fun advance(amount: Float) {

    }

    open fun populateSettlementDialogOrder() : Int {
        return 5
    }

    open fun populateSettlementDialog(dialog: InteractionDialogAPI, plugin: RATInteractionPlugin) {

    }

    //Return true to catch the press.
    open fun optionPressDetected(optionName: String, optionData: Any?) : Boolean {


        return false
    }

    open fun addToMonthlyCargo(current: CargoAPI): CargoAPI? {
        return null
    }

    fun isFunctional() = !isBuilding

    fun setBuilding(isBuilding: Boolean) {
        this.isBuilding = isBuilding
    }

    open fun reportEconomyTick(iterIndex: Int) {

    }

    open fun reportEconomyMonthEnd() {

    }

}