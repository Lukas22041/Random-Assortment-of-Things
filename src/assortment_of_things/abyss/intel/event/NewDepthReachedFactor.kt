package assortment_of_things.abyss.intel.event

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.entities.H
import lunalib.lunaExtensions.isNull

class NewDepthReachedFactor(var system: StarSystemAPI) : BaseOneTimeFactor(0), EveryFrameScript {

    var done = false

    var lowKey = "\$rat_abyss_reachedLow"
    var midKey = "\$rat_abyss_reachedMid"
    var highKey = "\$rat_abyss_reachedHigh"

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

            var memory = Global.getSector().memoryWithoutUpdate
            if (AbyssUtils.getTier(system) == AbyssProcgen.Tier.Low)
            {
                if (memory.isNull(lowKey))
                {
                    points = 10
                    memory.set(lowKey, true)
                    AbyssalDepthsEventIntel.addFactorCreateIfNecessary(this, null)
                }
            }
            if (AbyssUtils.getTier(system) == AbyssProcgen.Tier.Mid)
            {
                if (memory.isNull(midKey))
                {
                    points = 30
                    memory.set(midKey, true)
                    AbyssalDepthsEventIntel.addFactorCreateIfNecessary(this, null)
                }
            }
            if (AbyssUtils.getTier(system) == AbyssProcgen.Tier.High)
            {
                if (memory.isNull(highKey))
                {
                    points = 50
                    memory.set(highKey, true)
                    AbyssalDepthsEventIntel.addFactorCreateIfNecessary(this, null)
                }
            }

        }
    }

    override fun getDesc(intel: BaseEventIntel?): String {
        if(AbyssUtils.getTier(system) == AbyssProcgen.Tier.Low) return "Discovered the abyss"
        return "Reached a deeper part of the abyss."
    }

    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean, tooltipParam: Any) {

                if(AbyssUtils.getTier(system) == AbyssProcgen.Tier.Low) {
                    tooltip.addPara("The fleet discovered the abyss.",
                        0f,
                        Misc.getHighlightColor(),
                        "")
                }
                else
                {
                    tooltip.addPara("The fleet ventured in to another layer of the abyss, learning more of its mysteries.",
                        0f,
                        Misc.getHighlightColor(),
                        "")
                }
            }
        }
    }

}