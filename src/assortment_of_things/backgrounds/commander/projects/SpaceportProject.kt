package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.ui.TooltipMakerAPI

class SpaceportProject : BaseCommanderProject() {

    override fun getID(): String {
        return "spaceport"
    }

    override fun getName(): String {
        return "Spaceport"
    }

    override fun getIcon(): String {
        return "graphics/icons/markets/spaceport.png"
    }

    override fun getOrder() : Int {
       return 0
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The basic facilities for allowing interstellar trade.", 0f)
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {

    }

    override fun getCost() : Int {
        return 0
    }

    override fun getIncome(): Int {
        return 20000
    }

}