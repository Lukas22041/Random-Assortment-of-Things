package assortment_of_things.abyss.intel.event

import assortment_of_things.abyss.entities.AbyssalPhotosphere
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class DiscoveredPhotosphere(var points: Int, var entity: SectorEntityToken) : BaseOneTimeFactor(points), EveryFrameScript {

    var done = false

    override fun isDone(): Boolean {
        return done
    }

    override fun runWhilePaused(): Boolean {
       return true
    }

    override fun advance(amount: Float) {
        var plugin = entity.customPlugin as AbyssalPhotosphere
        var playerfleet = Global.getSector().playerFleet
        if (playerfleet.containingLocation == entity.containingLocation && MathUtils.getDistance(playerfleet.location, entity.location) < plugin.radius / 10)
        {
            done = true
            AbyssalDepthsEventIntel.addFactorCreateIfNecessary(this, null)
        }
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "Discovored a Photosphere"
    }

    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                tooltip.addPara("The fleet discovered a new photosphere within the darkness of the depths.",
                    0f,
                    Misc.getHighlightColor(),
                    "")
            }
        }
    }

}