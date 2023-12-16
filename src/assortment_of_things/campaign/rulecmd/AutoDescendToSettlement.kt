package assortment_of_things.campaign.rulecmd

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.interactions.SettlementInteraction
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest
import com.fs.starfarer.api.util.Misc

class AutoDescendToSettlement() : BaseCommandPlugin() {

    override fun execute(ruleId: String, dialog: InteractionDialogAPI, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {

        var data = FrontiersUtils.getSettlementData()
        if (data.autoDescend && dialog.interactionTarget.market?.isPlanetConditionMarketOnly == true) {
            dialog.textPanel.clear()
            DelegateToSettlementDialog().execute(ruleId, dialog, params, memoryMap)
        }

        return true
    }
}