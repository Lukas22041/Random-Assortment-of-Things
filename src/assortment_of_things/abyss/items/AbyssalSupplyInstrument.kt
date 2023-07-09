package assortment_of_things.abyss.items

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.campaign.listeners.ListenerUtil
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.isPlayerInHyperspace
import java.awt.Color

class AbyssalSupplyInstrument : BaseSpecialItemPlugin() {


    override fun init(stack: CargoStackAPI) {
        super.init(stack)

    }

    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return 5000
    }

    override fun getName(): String? {
        return "Abyssal Supply Instrument"
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
        tooltip.addPara("An autonomous device that, when used, activates a miniature nanoforge that creates a one-time stock of supplies. Included in it is: \n\n" +
                "50x Supplies\n" +
                "50x Fuel\n" +
                "\n" +
                "Does not go over the fleets cargo limits, any excess is wasted.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "used", "stock of supplies.", "50x Supplies", "50x Fuel")
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
        var supplies = 50f
        var fuel = 50f

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