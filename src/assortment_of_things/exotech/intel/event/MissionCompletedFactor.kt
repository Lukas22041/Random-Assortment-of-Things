package assortment_of_things.exotech.intel.event

import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class MissionCompletedFactor(var points: Int, var dialog: InteractionDialogAPI, var missionName: String) : BaseOneTimeFactor(points) {

    init {
        ExotechEventIntel.addFactorCreateIfNecessary(this, dialog)
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        return "Completed \"$missionName\""
    }

    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {
                tooltip.addPara("Completing the \"$missionName\" mission improved Amelies influence over the faction.",
                    0f,
                    Misc.getHighlightColor(),
                    "")
            }
        }
    }

}