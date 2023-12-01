package assortment_of_things.campaign.industries

import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.magiclib.kotlin.getStorageCargo

abstract class BaseConsumeableIndustry() : BaseIndustry() {
    abstract override fun apply()

    override fun isAvailableToBuild(): Boolean {
        if (market.faction != Global.getSector().playerFaction) return false
        if (getStack() != null) return true
        return false
    }

    override fun showWhenUnavailable(): Boolean {
        return false
    }

    override fun startBuilding() {
        super.startBuilding()

        var stack = getStack()
        if (stack != null) {
            reduceOrRemoveStack(stack)
        }
    }

    override fun notifyBeingRemoved(mode: MarketAPI.MarketInteractionMode?, forUpgrade: Boolean) {
        super.notifyBeingRemoved(mode, forUpgrade)

        if (market.faction == Global.getSector().playerFaction) {
            if (market.getStorageCargo() != null) {
                market.getStorageCargo().addSpecial(SpecialItemData("rat_consumeable_industry", spec.id), 1f)
            }
        }
    }

    override fun addRightAfterDescriptionSection(tooltip: TooltipMakerAPI?, mode: Industry.IndustryTooltipMode?) {
        if (mode == Industry.IndustryTooltipMode.ADD_INDUSTRY) {
            tooltip!!.addSpacer(10f)
            tooltip!!.addNegativePara("Building this structure will use up the item that allows building it. Demolishing the structure returns the item in to the markets storage.")
        }
    }

    protected fun getStack() : CargoStackAPI? {

        var stack = Global.getSector().playerFleet.cargo.stacksCopy
            .find { it.isSpecialStack && (it.specialItemSpecIfSpecial.id == "rat_consumeable_industry" && it.specialDataIfSpecial.data == spec.id )}

        if (stack == null && market.getStorageCargo() != null)  {
            stack = market.getStorageCargo().stacksCopy
                .find { it.isSpecialStack && (it.specialItemSpecIfSpecial.id == "rat_consumeable_industry" && it.specialDataIfSpecial.data == spec.id )}
        }

        return stack
    }

    private fun reduceOrRemoveStack(stack: CargoStackAPI) {
        stack.subtract(1f)
        if (stack.size < 0.1f) {
            stack.cargo.removeStack(stack)
        }
    }
}