package assortment_of_things.campaign.rulecmd

import assortment_of_things.frontiers.FrontiersUtils
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class ShouldShowSettlementOption : BaseCommandPlugin() {
    override fun execute(ruleId: String?, dialog: InteractionDialogAPI, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {

        var target = dialog.interactionTarget
        if (target !is PlanetAPI) return false

        if (FrontiersUtils.hasSettlement()) {
            if (FrontiersUtils.getSettlementData().primaryPlanet == dialog.interactionTarget) {
                return true
            }
        }

        return false
    }

}