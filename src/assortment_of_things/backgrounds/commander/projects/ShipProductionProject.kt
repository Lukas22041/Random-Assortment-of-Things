package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class ShipProductionProject : BaseCommanderProject() {

    override fun getID(): String {
        return "ship_production"
    }

    override fun getName(): String {
        return "Ship Production"
    }

    override fun getIcon(): String {
        return "graphics/icons/markets/autofactory.png"
    }

    override fun getOrder() : Int {
       return 5
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The required facilities to receive custom production orders for ships.", 0f)
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Orders will be delivered to this station by the end of the month. Increases the maximum allocation of construction budget by 100000.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "100000")
        tooltip.addSpacer(10f)
    }

    override fun getCost() : Int {
        return 100000
    }

    override fun getIncome(): Int {
        return 10000
    }

    override fun getCustomProductionBudget(): Float {
        return 100000f
    }

}