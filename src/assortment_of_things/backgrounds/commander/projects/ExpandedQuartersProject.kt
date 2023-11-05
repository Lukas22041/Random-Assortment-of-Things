package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.ui.TooltipMakerAPI

class ExpandedQuartersProject : BaseCommanderProject() {

    override fun getID(): String {
        return "quarters"
    }

    override fun getName(): String {
        return "Expanded Quarters"
    }

    override fun getIcon(): String {
        return "graphics/icons/markets/urbanized_polity.png"
    }

    override fun getOrder() : Int {
       return 4
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Increases the available living space in the station.", 0f)
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {

    }

    override fun getCost() : Int {
        return 50000
    }

    override fun getIncome(): Int {
        return 10000
    }

}