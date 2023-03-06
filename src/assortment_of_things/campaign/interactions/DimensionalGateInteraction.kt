package assortment_of_things.campaign.interactions

import assortment_of_things.campaign.plugins.entities.DimensionalGate
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc

class DimensionalGateInteraction : RATInteractionPlugin() {
    override fun init() {

        var plugin = interactionTarget.customPlugin as DimensionalGate
        if (!plugin.active)
        {
            var planet: SectorEntityToken? = null
            var stations = interactionTarget.starSystem.customEntities.filter { it.customEntitySpec.id == "rat_chiral_station1" }
            for (station in stations)
            {
                planet = Misc.findNearestPlanetTo(station, false, false)
            }

            textPanel.addPara("You close on to what appears to be a gate, but its specifications seem different to that of Domain Built ones. There is no sign of it being active.")

            if (planet != null)
            {
                textPanel.addPara("Despite that, there seem to be solar-powerd antenna attempting to receive signals from somewhere close to the ${planet.name} planet.").apply {
                    setHighlight("${planet.name}")
                    setHighlightColor(Misc.getHighlightColor())
                }
            }
        }
        else
        {
            textPanel.addPara("You close on to what appears to be a gate, but its specifications seem different to that of Domain Built ones. After activating the station, the gate seems now be active. However it does not seem to be connected to the rest of the gate network.")

            createOption("Attempt to pass through.") {
                var plugin = interactionTarget.customPlugin as DimensionalGate
                plugin.showBeingUsed(15f)
                (plugin.teleportLocation!!.customPlugin as DimensionalGate).showBeingUsed(15f)
                Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(plugin.teleportLocation, ""), 2f)
                closeDialog()
            }
        }


        addLeaveOption()
    }
}