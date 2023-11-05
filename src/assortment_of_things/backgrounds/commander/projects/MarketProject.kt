package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.ui.TooltipMakerAPI

class MarketProject : BaseCommanderProject() {

    override fun getID(): String {
        return "market"
    }

    override fun getName(): String {
        return "Interstellar Market"
    }

    override fun getIcon(): String {
        return "graphics/icons/markets/free_port.png"
    }

    override fun getOrder() : Int {
       return 3
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Establishes a larger market that allows for expanded trade.", 0f)
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {

    }

    override fun getCost() : Int {
        return 100000
    }

    override fun getIncome(): Int {
        return 25000
    }

}