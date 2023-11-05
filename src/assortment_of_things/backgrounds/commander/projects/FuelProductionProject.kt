package assortment_of_things.backgrounds.commander.projects

import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class FuelProductionProject : BaseCommanderProject() {

    override fun getID(): String {
        return "fuel_production"
    }

    override fun getName(): String {
        return "Fuel Production"
    }

    override fun getIcon(): String {
        return "graphics/icons/cargo/fuel.png"
    }

    override fun getOrder() : Int {
       return 21
    }

    override fun addDescription(tooltip: TooltipMakerAPI) {
        tooltip.addPara("Enables the production of fuel. Produces up to 250 units per month.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "250")
    }

    override fun addLongDescription(tooltip: TooltipMakerAPI) {

    }

    override fun getCost() : Int {
        return 40000
    }

    override fun getIncome(): Int {
        return 5000
    }

    override fun addToMonthlyCargo(): HashMap<String, Float> {
        return hashMapOf(Commodities.FUEL to 250f)
    }

}