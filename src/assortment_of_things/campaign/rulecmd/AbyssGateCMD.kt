package assortment_of_things.campaign.rulecmd

import assortment_of_things.misc.addNegativePara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.OptionPanelAPI.OptionTooltipCreator
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD
import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class AbyssGateCMD : BaseCommandPlugin() {
    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {

        val command = params!![0].getString(memoryMap)


        if (command == "CanBeAdded") {
            var data = GateEntityPlugin.getGateData()
            var gate = data.scanned.find { it.hasTag("rat_abyss_gate") }
            return gate != null && dialog!!.interactionTarget != gate
        }

        if (command == "Highlight") {
            var data = GateEntityPlugin.getGateData()
            var gate = data.scanned.find { it.hasTag("rat_abyss_gate") }
            if (gate != null && dialog!!.interactionTarget != gate) {
                var requiredFuel = GateCMD.computeFuelCost(gate)
             //   dialog.optionPanel.addOption("Travel towards Abyssal Gate.", "ABYSSAL_GATE")

                var enoughFuel = requiredFuel <= Global.getSector().playerFleet.cargo.fuel

                if (!enoughFuel) {
                    dialog.optionPanel.setEnabled("ABYSSAL_GATE", false)
                }

                dialog.optionPanel.addOptionTooltipAppender("ABYSSAL_GATE") { tooltip, hadOtherText ->
                    tooltip!!.addPara("Travel towards the Abyssal Gate. Requires ${requiredFuel.toInt()} Fuel.",
                        0f, Misc.getTextColor(), Misc.getHighlightColor(), "$requiredFuel")

                    if (!enoughFuel) {
                        tooltip.addSpacer(5f)
                        tooltip.addNegativePara("The fleet does not have enough fuel.")
                    }

                    tooltip.addSpacer(2f)
                }
            }
        }

        if (command == "Traverse") {

            var data = GateEntityPlugin.getGateData()
            var gate = data.scanned.find { it.hasTag("rat_abyss_gate") }

            var requiredFuel = GateCMD.computeFuelCost(gate)
            Global.getSector().playerFleet.cargo.removeFuel(requiredFuel.toFloat())

            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, dialog!!.interactionTarget, JumpPointAPI.JumpDestination(gate, ""))
            dialog.dismiss()
        }


        return false
    }

}