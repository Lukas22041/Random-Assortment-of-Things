package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

abstract class BaseCommanderProject {

    var active = false


    abstract fun getID() : String

    abstract fun getName() : String

    abstract fun getIcon(): String

    abstract fun getOrder() : Int

    abstract fun addDescription(tooltip: TooltipMakerAPI)

    abstract fun addLongDescription(tooltip: TooltipMakerAPI)

    abstract fun getCost() : Int

    abstract fun getIncome() : Int

    open fun canBeBuild(build: ArrayList<String>) : Boolean {
        return true
    }

    open fun getCustomProductionBudget() : Float {
        return 0f
    }

    open fun addToMonthlyCargo() : HashMap<String, Float> {
        return HashMap()
    }
}