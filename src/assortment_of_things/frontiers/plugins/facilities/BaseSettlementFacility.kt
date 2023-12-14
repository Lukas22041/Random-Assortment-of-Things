package assortment_of_things.frontiers.plugins.facilities

import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.data.SettlementFacilitySpec
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

abstract class BaseSettlementFacility {

    lateinit var spec: SettlementFacilitySpec

    fun getID() : String {
        return spec.id
    }

    open fun getName() : String {
        return spec.name
    }

    open fun getShortDesc() : String {
        return spec.shortDesc
    }

    open fun getIcon(): String {
        return spec.icon
    }

    open fun canBeBuild(data: SettlementData) : Boolean {
        return true
    }

    open fun getCost(data: SettlementData) : Float {
        return spec.cost
    }

    open fun canNotBeBuildReason(tooltip: TooltipMakerAPI, data: SettlementData) {

    }

    abstract fun addDescriptionToTooltip(tooltip: TooltipMakerAPI, data: SettlementData)

    abstract fun apply(data: SettlementData)

    abstract fun unapply(data: SettlementData)


}