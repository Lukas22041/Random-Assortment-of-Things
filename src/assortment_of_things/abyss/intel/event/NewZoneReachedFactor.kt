package assortment_of_things.abyss.intel.event

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class NewZoneReachedFactor(var points: Int, var system: StarSystemAPI) : BaseOneTimeFactor(points), EveryFrameScript {

    var done = false

    override fun isDone(): Boolean {
        return done
    }

    override fun runWhilePaused(): Boolean {
       return true
    }

    override fun advance(amount: Float) {
        if (Global.getSector().playerFleet.containingLocation == system)
        {
            done = true
            AbyssalDepthsEventIntel.addFactorCreateIfNecessary(this, null)
        }
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "Reached new location"
    }

    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                tooltip.addPara("The fleet ventured in to another zone, gaining more knowledge about the abyss.",
                    0f,
                    Misc.getHighlightColor(),
                    "")
            }
        }
    }

}