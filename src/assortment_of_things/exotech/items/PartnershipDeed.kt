package assortment_of_things.exotech.items

import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class PartnershipDeed() : BaseSpecialItemPlugin() {


    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        super.createTooltip(tooltip, expanded, transferHandler, stackSource)
        val pad = 3f
        val opad = 5f
        val small = 5f
        val h: Color = Misc.getHighlightColor()
        val g: Color = Misc.getGrayColor()
        var b: Color? = Misc.getButtonTextColor()
        b = Misc.getPositiveHighlightColor()

        tooltip.addSpacer(5f)
        tooltip.addPara("A deed signing a trade relation between Exo-Tech and your own Organisation. " +
                "Acquiring the trust of the coorperation enables the the holder to purchase ships, weapons & fighters that are unique to Exo-Techs doctrine with their tokens.\n\n" +
                "Additionaly a map with the current location of all Exoships is provided, and other options may reveal themself. This deed does not put you in to a commission with Exo-Tech.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "Exo-Tech", "ships", "weapons", "fighters", "tokens", "map")
        tooltip.addSpacer(5f)

        addCostLabel(tooltip, opad, transferHandler, stackSource)
    }

    override fun createTooltip(tooltip: TooltipMakerAPI,  expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?, useGray: Boolean) {
        val opad = 10f
        tooltip.addTitle(name, Color(217, 164, 57))

    }

    override fun hasRightClickAction(): Boolean {
        return false
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return false
    }

}