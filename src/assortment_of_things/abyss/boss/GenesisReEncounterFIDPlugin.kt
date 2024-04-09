package assortment_of_things.abyss.boss

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl

class GenesisReEncounterFIDPlugin(config: FIDConfig, var originalPlugin: RATInteractionPlugin) : FleetInteractionDialogPluginImpl(config) {


    init {
       context = GenesisReEncounterFEContext()
    }

    override fun backFromEngagement(result: EngagementResultAPI?) {

        super.backFromEngagement(result)


        dialog.plugin = originalPlugin
        originalPlugin.defeatedDefenders()

    }

    /*override fun backFromEngagement(result: EngagementResultAPI?) {

       // super.backFromEngagement(result)
    }*/
}