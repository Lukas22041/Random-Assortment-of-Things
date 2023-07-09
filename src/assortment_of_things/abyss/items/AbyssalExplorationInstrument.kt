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

class AbyssalExplorationInstrument : BaseSpecialItemPlugin() {


    override fun init(stack: CargoStackAPI) {
        super.init(stack)

    }


    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return 5000
    }

    override fun getName(): String? {
        return "Abyssal Exploration Instrument"
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
        tooltip.addPara("An autonomous device that, when deployed, scans and reveals points of interests within the system. ", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "deployed", "scans", "reveals")
        tooltip.addSpacer(5f)


        if (Global.getSector().isPlayerInHyperspace())
        {
            tooltip.addPara("Can not be used in Hyperspace.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else if (Global.getSector().playerFleet.containingLocation.memoryWithoutUpdate.get("\$rat_abyss_alreadyScanned") != null)
        {
            tooltip.addPara("This system has already been scanned.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else
        {
            tooltip.addPara("Rightclick to deploy", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        }


        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        if (Global.getSector().isPlayerInHyperspace()) return false
        if (Global.getSector().playerFleet.containingLocation.memoryWithoutUpdate.get("\$rat_abyss_alreadyScanned") != null) return false
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {
        Global.getSector().playerFleet.containingLocation.memoryWithoutUpdate.set("\$rat_abyss_alreadyScanned", true)

        var fleet = Global.getSector().playerFleet
        var entities = fleet.containingLocation.customEntities

        var count = 0

        for (entity in entities)
        {
            if (entity.isDiscoverable)
            {
                count++
                entity.setDiscoverable(null)
                entity.setSensorProfile(null);
                ListenerUtil.reportEntityDiscovered(entity)
            }
        }

        if (count == 0)
        {
            Global.getSector().campaignUI.addMessage("No new objects have been found.")
        }
        else if (count == 1)
        {
            Global.getSector().campaignUI.addMessage("Discovered $count object within the system.")
        }
        else
        {
            Global.getSector().campaignUI.addMessage("Discovered $count objects within the system.")
        }

        val custom = CampaignPingSpec()
        custom.color = AbyssUtils.ABYSS_COLOR
        custom.width = 10f
        custom.minRange = fleet.radius
        custom.range = fleet.radius + 1000
        custom.duration = 3f
        custom.alphaMult = 1f
        custom.inFraction = 0.1f
        custom.num = 1

        Global.getSoundPlayer().playSound("lidar_ping", 0.6f, 1f, fleet.location, fleet.velocity)

        Global.getSector().addPing(fleet, custom)
    }
}