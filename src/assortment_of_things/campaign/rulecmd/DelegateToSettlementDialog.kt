package assortment_of_things.campaign.rulecmd

import assortment_of_things.frontiers.FrontiersUtils
import assortment_of_things.frontiers.interactions.SettlementInteraction
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class DelegateToSettlementDialog() : BaseCommandPlugin() {

    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {

        var previousPlugin = dialog!!.plugin
        //var previousTarget = dialog.interactionTarget

        //dialog.textPanel.clear()
        dialog.optionPanel.clearOptions()
        dialog.hideVisualPanel()

        var data = FrontiersUtils.getSettlementData()
        var newPlugin = SettlementInteraction(data)
        newPlugin.previousPlugin = dialog.plugin
        dialog.plugin = newPlugin
        dialog.interactionTarget = data.settlementEntity
        newPlugin.init(dialog)

        return true
    }
}