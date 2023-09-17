package assortment_of_things.abyss.items

import assortment_of_things.abyss.intel.event.AbyssalDepthsEventIntel
import assortment_of_things.scripts.AtMarketListener
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class AbyssalSurveyData : BaseSpecialItemPlugin() {


    override fun init(stack: CargoStackAPI) {
        super.init(stack)

    }


    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return 30000
    }

    override fun getName(): String? {
        return "Abyssal Survey Data"
    }

    override fun getTooltipWidth(): Float {
        return super.getTooltipWidth()
    }
    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource)
        val pad = 3f
        val opad = 10f
        val small = 5f
        val h: Color = Misc.getHighlightColor()
        val g: Color = Misc.getGrayColor()
        var b: Color? = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()

        tooltip.addSpacer(5f)
        tooltip.addPara("A piece of survey data collected from within the abyss. Any scientist in the sector would crave for a glimpse in the abyss, making it sell well at most markets. \n\n" +
                "It can also be studied to receive 15 units of progress for the \"Abyssal Exploration\" Intel.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "sell", "15", "Abyssal Exploration")

        tooltip.addSpacer(5f)
        tooltip.addPara("Right Click to study", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {

        var factor = object : BaseOneTimeFactor(15) {

            override fun getDesc(intel: BaseEventIntel?): String {
                return "Studied Survey Data"
            }

            override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
                return object : BaseFactorTooltip() {
                    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                        tooltip.addPara("Used Survey Data to further the comprehension of the abyss.",
                            0f,
                            Misc.getHighlightColor(),
                            "")
                    }
                }
            }
        }
        AbyssalDepthsEventIntel.addFactorCreateIfNecessary(factor, null)
        Global.getSoundPlayer().playUISound("ui_survey_found_5", 1f, 0.8f)
    }
}