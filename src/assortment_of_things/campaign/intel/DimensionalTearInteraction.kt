package assortment_of_things.campaign.intel

import assortment_of_things.campaign.plugins.entities.DimensionalTearEntity
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI

class DimensionalTearInteraction : RATInteractionPlugin() {
    override fun init() {

        textPanel.addPara("You close in to an anomalyous object. It radiates off a similar signature as to that of a Jump-point, but to the fleets optical sensors, it looks almost like a mirror... ")

        createOption("Attempt to pass through.") {
            var plugin = interactionTarget.customPlugin as DimensionalTearEntity
            Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, interactionTarget, JumpPointAPI.JumpDestination(plugin.teleportLocation, ""), 2f)
            closeDialog()
        }

        addLeaveOption()
    }
}