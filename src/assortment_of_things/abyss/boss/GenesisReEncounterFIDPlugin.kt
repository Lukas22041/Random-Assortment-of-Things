package assortment_of_things.abyss.boss

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl

class GenesisReEncounterFIDPlugin(config: FIDConfig, var originalPlugin: RATInteractionPlugin) : FleetInteractionDialogPluginImpl(config) {


    init {
       context = GenesisReEncounterFEContext()
    }

    override fun backFromEngagement(result: EngagementResultAPI?) {

        super.backFromEngagement(result)

        val b = context.battle
       /* if (b.isPlayerInvolved) {
            cleanUpBattle()
        }*/


        b.leave(Global.getSector().getPlayerFleet(), false)

        context.isAutoresolve = false
        originalPlugin.defeatedDefenders()


    /*    dialog.plugin = originalPlugin
        dialog.interactionTarget = originalPlugin.interactionTarget

        originalPlugin.defeatedDefenders()*/

    }

    /*override fun backFromEngagement(result: EngagementResultAPI?) {

       // super.backFromEngagement(result)
    }*/
}