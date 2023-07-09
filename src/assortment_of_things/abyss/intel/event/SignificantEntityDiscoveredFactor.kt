package assortment_of_things.abyss.intel.event

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SignificantEntityDiscoveredFactor(var points: Int, var entity: SectorEntityToken) : BaseOneTimeFactor(points), EveryFrameScript {

    var done = false

    override fun isDone(): Boolean {
        return done
    }

    override fun runWhilePaused(): Boolean {
       return true
    }

    override fun advance(amount: Float) {
        if (!entity.isDiscoverable)
        {
            done = true
            AbyssalDepthsEventIntel.addFactorCreateIfNecessary(this, null)
        }
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "Discovered a major entity"
    }

    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                tooltip.addPara("While navigating the abyssal landscape, the fleet discovered a major entity.",
                    0f,
                    Misc.getHighlightColor(),
                    "")
            }
        }
    }

}