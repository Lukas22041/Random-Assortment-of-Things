package assortment_of_things.abyss.items

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.isPlayerInHyperspace
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getDistance
import java.awt.Color

class AbyssalHostilityInstrument : BaseSpecialItemPlugin() {


    override fun init(stack: CargoStackAPI) {
        super.init(stack)

    }


    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return 5000
    }

    override fun getName(): String? {
        return "Abyssal Hostility Instrument"
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
        tooltip.addPara("An autonomous device that if deployed, scans the system for hostile fleets. Using it reveals hostile fleets on the map and radar. After a few days it will lose contact of its targets", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "deployed", "reveals")
        tooltip.addSpacer(5f)


        if (Global.getSector().isPlayerInHyperspace())
        {
            tooltip.addPara("Can not be used in Hyperspace.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else
        {
            tooltip.addPara("Rightclick to use", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        }


        addCostLabel(tooltip, opad, transferHandler, stackSource)

    }

    override fun hasRightClickAction(): Boolean {
        if (Global.getSector().isPlayerInHyperspace()) return false
        return true
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {

        var fleet = Global.getSector().playerFleet
        var entities = fleet.containingLocation.fleets

        var count = 0

        for (entity in entities)
        {
            if(entity.isHostileTo(fleet))
            {
                count++

                if (entity.containingLocation.customEntities.any { it.id ==  "${entity.id}_hostile_icon"} ) continue

                var icon = entity.containingLocation.addCustomEntity("${entity.id}_hostile_icon", "Hostile Fleet", "rat_abyss_hostile_icon", Factions.NEUTRAL)

                icon.location.set(entity.location)
                icon.setCircularOrbit(entity, 0f, 0f, 0f)
                icon.addTag(AbyssTags.HOSTILE_ICON)
            }
        }

        if (count == 0)
        {
            Global.getSector().campaignUI.addMessage("No hostile fleets have been discovered.")
        }
        else if (count == 1)
        {
            Global.getSector().campaignUI.addMessage("Discovered $count hostile fleet in the system.")
        }
        else
        {
            Global.getSector().campaignUI.addMessage("Discovered $count hostile fleets in the system.")
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

        Global.getSector().addPing(fleet, custom)

        Global.getSoundPlayer().playSound("lidar_ping", 0.6f, 1f, fleet.location, fleet.velocity)

    }
}