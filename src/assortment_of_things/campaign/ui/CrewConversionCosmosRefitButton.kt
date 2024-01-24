package assortment_of_things.campaign.ui

import assortment_of_things.misc.addNegativePara
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaRefit.BaseRefitButton
import org.magiclib.kotlin.getStorage

class CrewConversionCosmosRefitButton : BaseRefitButton() {

    override fun getButtonName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "Cosmos Core Integration"
    }

    override fun getIconName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "graphics/hullmods/rat_chosmos_conversion.png"
    }

    override fun getOrder(member: FleetMemberAPI?, variant: ShipVariantAPI?): Int {
        return 91
    }

    override fun shouldShow(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return variant!!.hasHullMod("rat_abyssal_conversion")
    }

    override fun hasTooltip(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun addTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {


        var label = tooltip!!.addPara("Double-Clicking this button installs a cosmos core in to the crew-converted abyssal hull.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "Double-Clicking", "cosmos core")

        tooltip.addSpacer(5f)

        tooltip.addPara("This allows the ship to use the cosmos shipsystem and provides its skill." +
                "", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "shipsystem", "permanently")

        if (getCosmosStack(market) == null) {
            tooltip.addSpacer(5f)
            tooltip.addNegativePara("No Cosmos Core in inventory.")
        }
    }

    override fun isClickable(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return getCosmosStack(market) != null
    }

    override fun onClick(member: FleetMemberAPI?, variant: ShipVariantAPI?, event: InputEventAPI?, market: MarketAPI?) {
        super.onClick(member, variant, event, market)

        var cosmosStack = getCosmosStack(market)

        if (event!!.isLMBEvent && event.isDoubleClick && cosmosStack != null) {
            variant!!.removeMod("rat_abyssal_conversion")
            variant!!.addMod("rat_cosmos_conversion")

            Global.getSoundPlayer().playUISound("ui_char_spent_story_point", 1f, 1f)

            reduceOrRemoveStack(cosmosStack)
            refreshVariant()
            refreshButtonList()
        }
        else if (event.isLMBEvent && event.isDoubleClick) {
            Global.getSoundPlayer().playUISound("ui_button_disabled_pressed", 1f, 1f)
        }
    }

    fun getCosmosStack(market: MarketAPI?) : CargoStackAPI? {
        var stack = Global.getSector().playerFleet.cargo.stacksCopy.find { it.isCommodityStack && it.commodityId == RATItems.COSMOS_CORE }
        if (stack != null) return stack

        if (market != null && market.getStorage() != null) {
            stack = market.getStorage().cargo.stacksCopy.find { it.isCommodityStack && it.commodityId == RATItems.COSMOS_CORE }
        }
        return stack
    }

    fun reduceOrRemoveStack(stack: CargoStackAPI) {
        stack.subtract(1f)
        if (stack.size < 0.1f) {
            stack.cargo.removeStack(stack)
        }
    }

}