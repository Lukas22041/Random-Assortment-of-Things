package assortment_of_things.campaign.rulecmd

import assortment_of_things.frontiers.interactions.CreateSettlementInteraction
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class DelegateToSettlementCreationDialog() : BaseCommandPlugin() {
    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {

        var previousPlugin = dialog!!.plugin
        //var previousTarget = dialog.interactionTarget

        //dialog.textPanel.clear()
        dialog.optionPanel.clearOptions()
        dialog.hideVisualPanel()

        var newPlugin = CreateSettlementInteraction()
        newPlugin.previousPlugin = dialog.plugin
        dialog.plugin = newPlugin
        //dialog.interactionTarget = dialog.interactionTarget.starSystem.createToken(Vector2f())
        newPlugin.init(dialog)

        return true
    }
}