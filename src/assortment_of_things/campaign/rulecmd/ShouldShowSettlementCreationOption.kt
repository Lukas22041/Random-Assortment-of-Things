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

class ShouldShowSettlementCreationOption : BaseCommandPlugin() {
    override fun execute(ruleId: String?, dialog: InteractionDialogAPI, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {

        var target = dialog.interactionTarget
        if (target !is PlanetAPI) return false

        if (!FrontiersUtils.isFrontiersActive()) return false
        if (FrontiersUtils.hasSettlement()) return false

        if (target.faction?.relToPlayer?.isAtWorst(RepLevel.INHOSPITABLE) != true) return false
        //if (target.faction?.isPlayerFaction == true) return false
        if (target.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) return false
        if (target.isGasGiant) return false
        if (target.tags.contains("gas_giant")) return false
        if (target.isStar) return false
        var ship: ShipAPI

        return true
    }

}