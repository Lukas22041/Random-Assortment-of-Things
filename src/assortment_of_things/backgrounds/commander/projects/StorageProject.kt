package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.ui.TooltipMakerAPI

class StorageProject : BaseCommanderProject() {

    override fun getID(): String {
        return "storage"
    }

    override fun getName(): String {
        return "Storage & Docks"
    }

    override fun getIcon(): String {
        return "graphics/icons/markets/orbital_station.png"
    }

    override fun getOrder() : Int {
       return 0
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Allows for the storage of commodities and for ships to remain docked for extended amounts of time.", 0f)
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {

    }

    override fun getCost() : Int {
        return 0
    }

    override fun getIncome(): Int {
        return 5000
    }

}