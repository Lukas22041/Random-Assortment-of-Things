package assortment_of_things.campaign.ui

import assortment_of_things.misc.addNegativePara
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

class CrewConversionRemoveIntegratedRefitButton : BaseRefitButton() {

    override fun getButtonName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "Remove Integrated Core"
    }

    override fun getIconName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "graphics/hullmods/rat_crew_conversion.png"
    }

    override fun getOrder(member: FleetMemberAPI?, variant: ShipVariantAPI?): Int {
        return 90
    }

    override fun shouldShow(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return !variant!!.hasHullMod("rat_abyssal_conversion") &&
                (variant.hasHullMod("rat_chronos_conversion") || variant.hasHullMod("rat_cosmos_conversion") || variant.hasHullMod("rat_seraph_conversion") || variant.hasHullMod("rat_primordial_conversion"))
    }

    override fun hasTooltip(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun addTooltip(tooltip: TooltipMakerAPI?,member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {

        var label = tooltip!!.addPara("Double-Clicking this button uninstalls the currently integrated core. The Removed Core is recovered in the process.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "Double-Clicking", "uninstalls")

    }

    override fun isClickable(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun onClick(member: FleetMemberAPI?, variant: ShipVariantAPI?, event: InputEventAPI?, market: MarketAPI?) {
        super.onClick(member, variant, event, market)

        if (event!!.isLMBEvent && event.isDoubleClick) {

            if (variant!!.hasHullMod("rat_chronos_conversion")) {
                variant!!.removeMod("rat_chronos_conversion")
                Global.getSector().playerFleet.cargo.addCommodity(RATItems.CHRONOS_CORE, 1f)
            }

            if (variant.hasHullMod("rat_cosmos_conversion")) {
                variant!!.removeMod("rat_cosmos_conversion")
                Global.getSector().playerFleet.cargo.addCommodity(RATItems.COSMOS_CORE, 1f)
            }

            if (variant.hasHullMod("rat_seraph_conversion")) {
                variant!!.removeMod("rat_seraph_conversion")
                Global.getSector().playerFleet.cargo.addCommodity(RATItems.SERAPH_CORE, 1f)
            }

            if (variant.hasHullMod("rat_primordial_conversion")) {
                variant!!.removeMod("rat_primordial_conversion")
                Global.getSector().playerFleet.cargo.addCommodity(RATItems.PRIMORDIAL, 1f)
            }

            variant!!.addMod("rat_abyssal_conversion")

            Global.getSoundPlayer().playUISound("ui_char_spent_story_point", 1f, 1f)

            refreshVariant()
            refreshButtonList()
        }

    }

}