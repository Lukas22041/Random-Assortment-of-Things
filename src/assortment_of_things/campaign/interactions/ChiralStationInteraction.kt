package assortment_of_things.campaign.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.util.Misc

class ChiralStationInteraction : RATInteractionPlugin() {

    override fun init() {

        var defenderFleet = targetMemory.get("\$defenderFleet")
        if (defenderFleet != null && defenderFleet is CampaignFleetAPI && !defenderFleet.isEmpty )
        {
            textPanel.addPara("You arrive at a station floating through this odd system. Its location seemingly mirrored with another one. However, unlike the other station, this one shows some level of activity on the sensors.")
            textPanel.addPara("As your fleet prepares to dock, the signature of a fleet starts appearing.")

            triggerDefenders()
        }
        else
        {

            textPanel.addPara("With the defending fleet gone, your crew was able to dock and land at the station. They discovered multiple ships within the stations hangar, seemingly still operable. " +
                    "There is something odd about those ships, but they seem to be crewable by our teams if we were to transfer them to our fleet.")

            createOption("Recover the ships in the hangar") {
                dialog.visualPanel.showCore(CoreUITabId.CARGO, interactionTarget, null);
            }

            addLeaveOption()
        }
    }

    override fun defeatedDefenders() {
        clear()
        init()
    }

}