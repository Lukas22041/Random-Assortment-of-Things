package assortment_of_things.campaign.interactions

import assortment_of_things.campaign.plugins.entities.DimensionalGate
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global

class NonChiralStationInteraction : RATInteractionPlugin()
{

    override fun init() {
        textPanel.addPara("Your fleet arrives at a Station. While there is no sign of current activity, it doesnt seem to have been un-inhabited for long.\n\n" +
                "As the crew descends further in to the vessel, they find the stations control room. After fueling the stations generators, the control room lights back up.\n\n" +
                "" +
                "The crew sends over the recovered station logs. Upon analysing them, its revealed that the Station has been operating under Tri-Tachyon ownership, but the logs suddenly stop one day. While not all logs were able to be " +
                "decrypted, the latest entry mentions an expedition towards this systems gate.\n\n" +
                "" +
                "After reading through the logs, one of your officers reports that since re-activating the station, its antenna have been sending signals towards the gate.")

        var gates = interactionTarget.starSystem.customEntities.filter { it.customEntitySpec.id == "rat_dimensional_gate" }
        for (gate in gates)
        {
            var plugin = gate.customPlugin as DimensionalGate
            plugin.active = true
            (plugin.teleportLocation!!.customPlugin as DimensionalGate).active = true
        }

        addLeaveOption()
    }
}