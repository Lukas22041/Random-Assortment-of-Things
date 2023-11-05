package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SupplyProductionProject : BaseCommanderProject() {

    override fun getID(): String {
        return "supply_production"
    }

    override fun getName(): String {
        return "Supply Production"
    }

    override fun getIcon(): String {
        return "graphics/icons/cargo/supplies.png"
    }

    override fun getOrder() : Int {
       return 20
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Enables the production of supplies. Produces up to 100 units per month.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "100")
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {

    }

    override fun getCost() : Int {
        return 50000
    }

    override fun getIncome(): Int {
        return 10000
    }

    override fun addToMonthlyCargo(): HashMap<String, Float> {
        return hashMapOf(Commodities.SUPPLIES to 100f)
    }

}