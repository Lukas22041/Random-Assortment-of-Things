package assortment_of_things.exotech.intel.event

import assortment_of_things.abyss.entities.AbyssalBeacon
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

class TestFactor(var points: Int) : BaseOneTimeFactor(points), EveryFrameScript {

    var done = false

    init {
        ExotechEventIntel.addFactorCreateIfNecessary(this, null)
    }

    override fun isDone(): Boolean {
        return done
    }

    override fun runWhilePaused(): Boolean {
       return true
    }

    override fun advance(amount: Float) {

    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "Test"
    }

    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                tooltip.addPara("Yup.",
                    0f,
                    Misc.getHighlightColor(),
                    "")
            }
        }
    }

}