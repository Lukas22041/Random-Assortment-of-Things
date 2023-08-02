package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin
import exerelin.utilities.NexConfig
import exerelin.utilities.NexFactionConfig
import java.util.*

object RATNexManager {
    fun addStartingFleets() {
        var config = NexConfig.getFactionConfig("rat_abyssals")
        config.startingFaction = true

        var type = NexFactionConfig.StartFleetType.COMBAT_SMALL
        var fleets = NexFactionConfig.StartFleetSet(type)



        fleets.addFleet(listOf("rat_chuul_Hull"))


        config.startShips.put(type, fleets)
    }

}