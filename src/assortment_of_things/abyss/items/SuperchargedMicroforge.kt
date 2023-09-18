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

class SuperchargedMicroforge : BaseSpecialItemPlugin() {


    override fun init(stack: CargoStackAPI) {
        super.init(stack)

    }


    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return 30000
    }

    override fun getName(): String? {
        return "Supercharged Microforge"
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
        tooltip.addPara("A microforge that uses charged abyssal matter to create a one-time stock of supplies. On activation it produces: \n\n" +
                "100x Supplies\n" +
                "100x Fuel\n" +
                "\n" +
                "Does not produce over the fleets cargo limits, any excess is wasted.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "microforge", "stock of supplies.", "100x Supplies", "100x Fuel")
        tooltip.addSpacer(5f)

        tooltip.addPara("Rightclick to activate", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {
        var fleet = Global.getSector().playerFleet

        var cargo = fleet.cargo
        var supplies = 100f
        var fuel = 100f

        if (supplies > cargo.spaceLeft)
        {
            cargo.addSupplies(cargo.spaceLeft)
        }
        else
        {
            cargo.addSupplies(supplies)
        }

        if (fuel > cargo.freeFuelSpace)
        {
            cargo.addFuel(cargo.freeFuelSpace.toFloat())
        }
        else
        {
            cargo.addFuel(fuel)
        }

        Global.getSoundPlayer().playSound("ui_chip_pickup", 0.6f, 1f, fleet.location, fleet.velocity)

    }
}