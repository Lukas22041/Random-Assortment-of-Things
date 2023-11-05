package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class WeaponProductionProject : BaseCommanderProject() {

    override fun getID(): String {
        return "weapon_production"
    }

    override fun getName(): String {
        return "Weapon & Fighter Production"
    }

    override fun getIcon(): String {
        return "graphics/icons/markets/arms_tradeshow.png"
    }

    override fun getOrder() : Int {
       return 6
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("The required facilities to receive custom production orders for weapons and fighters.", 0f)
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Orders will be delivered to this station by the end of the month. Increases the maximum allocation of construction budget by 50000.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "50000")
        tooltip.addSpacer(10f)
    }

    override fun getCost() : Int {
        return 50000
    }

    override fun getIncome(): Int {
        return 5000
    }

    override fun getCustomProductionBudget() : Float {
        return 50000f
    }



}